// SPDX-License-Identifier: MIT
/*
 * Copyright (c) 2024, SCANOSS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.scanoss;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.scanoss.dto.*;
import com.scanoss.settings.Bom;
import com.scanoss.settings.RemoveRule;
import com.scanoss.settings.ReplaceRule;
import com.scanoss.settings.Rule;
import com.scanoss.utils.LineRange;
import com.scanoss.utils.LineRangeUtils;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Builder
public class ScannerPostProcessor {

    private Map<String, ScanFileDetails> componentIndex;

    /**
     * Processes scan results according to BOM configuration rules.
     * Applies remove, and replace rules as specified in the configuration.
     *
     * @param scanFileResults List of scan results to process
     * @param bom             Bom containing BOM rules
     * @return List of processed scan results
     */
    public List<ScanFileResult> process(@NotNull List<ScanFileResult> scanFileResults, @NotNull Bom bom) {
        log.info("Starting scan results processing with {} results", scanFileResults.size());
        log.debug("BOM configuration - Remove rules: {}, Replace rules: {}",
                bom.getRemove() != null ? bom.getRemove().size() : 0,
                bom.getReplace() != null ? bom.getReplace().size() : 0);

        createComponentIndex(scanFileResults);

        List<ScanFileResult> processedResults = new ArrayList<>(scanFileResults);

        if (bom.getRemove() != null && !bom.getRemove().isEmpty()) {
            log.info("Applying {} remove rules to scan results", bom.getRemove().size());
            processedResults = applyRemoveRules(processedResults, bom.getRemove());
        }

        if (bom.getReplace() != null && !bom.getReplace().isEmpty()) {
            log.info("Applying {} replace rules to scan results", bom.getReplace().size());
            processedResults = applyReplaceRules(processedResults, bom.getReplace());
        }

        log.info("Scan results processing completed. Original results: {}, Processed results: {}",
                scanFileResults.size(), processedResults.size());
        return processedResults;
    }

    /**
     * Creates a map of PURL (Package URL) to ScanFileDetails from a list of scan results.
     *
     * @param scanFileResults List of scan results to process
     */
    private void createComponentIndex(List<ScanFileResult> scanFileResults) {
        log.debug("Creating component index from scan results");

        if (scanFileResults == null) {
            log.warn("Received null scan results, creating empty component index");
            this.componentIndex = new HashMap<>();
            return;
        }

        this.componentIndex = scanFileResults.stream()
                .filter(result -> result != null && result.getFileDetails() != null)
                .flatMap(result -> result.getFileDetails().stream())
                .filter(details -> details != null && details.getPurls() != null)
                .flatMap(details -> Arrays.stream(details.getPurls())
                        .filter(purl -> purl != null && !purl.trim().isEmpty())
                        .map(purl -> Map.entry(purl.trim(), details)))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        HashMap::new
                ));
        log.debug("Component index created with {} entries", componentIndex.size());
    }


    /**
     * Applies replacement rules to scan results, updating their PURLs (Package URLs) based on matching rules.
     * If a cached component exists for a replacement PURL, it will be used instead of creating a new one.
     *
     * @param results The original list of scan results to process
     * @param rules   The list of replacement rules to apply
     * @return A new list containing the processed scan results with updated PURLs
     */
    private List<ScanFileResult> applyReplaceRules(@NotNull List<ScanFileResult> results, @NotNull List<ReplaceRule> rules) {
        log.debug("Starting replace rules application");
        List<ScanFileResult> resultsList = new ArrayList<>(results);

        for (ScanFileResult result : resultsList) {
            for (ReplaceRule rule : this.findMatchingRules(result, rules)) {
                log.debug("Applying replace rule: {} to file: {}", rule, result.getFilePath());

                PackageURL newPurl;
                try {
                    newPurl = new PackageURL(rule.getReplaceWith());
                } catch (MalformedPackageURLException e) {
                    log.warn("Failed to parse PURL from replace rule: {}. Skipping", rule);
                    continue;
                }

                LicenseDetails[] licenseDetails = new LicenseDetails[]{LicenseDetails.builder().name(rule.getLicense()).build()};

                ScanFileDetails cachedFileDetails = this.componentIndex.get(newPurl.toString());
                ScanFileDetails currentFileDetails = result.getFileDetails().get(0);
                ScanFileDetails newFileDetails;

                log.trace("Processing replacement - Cached details found: {}, Current PURL: {}, New PURL: {}",
                        cachedFileDetails != null,
                        currentFileDetails.getPurls()[0],
                        newPurl);

                if (cachedFileDetails != null) {
                    log.debug("Using cached component details for PURL: {}", newPurl);
                    newFileDetails = ScanFileDetails.builder()
                            .matchType(currentFileDetails.getMatchType())
                            .file(currentFileDetails.getFile())
                            .fileHash(currentFileDetails.getFileHash())
                            .fileUrl(currentFileDetails.getFileUrl())
                            .lines(currentFileDetails.getLines())
                            .matched(currentFileDetails.getMatched())
                            .licenseDetails(licenseDetails)
                            .component(cachedFileDetails.getComponent())
                            .vendor(cachedFileDetails.getVendor())
                            .url(cachedFileDetails.getUrl())
                            .purls(new String[]{newPurl.toString()})
                            .build();


                    result.getFileDetails().set(0, cachedFileDetails);
                } else {
                    log.debug("Creating new component details for PURL: {}", newPurl);
                    newFileDetails = currentFileDetails.toBuilder()
                            .copyrightDetails(new CopyrightDetails[]{})
                            .licenseDetails(new LicenseDetails[]{})
                            .vulnerabilityDetails(new VulnerabilityDetails[]{})
                            .purls(new String[]{newPurl.toString()})
                            .url("")
                            .component(newPurl.getName())
                            .vendor(newPurl.getNamespace())
                            .build();
                }

                result.getFileDetails().set(0, newFileDetails);
            }
        }
        return resultsList;
    }


    /**
     * Applies remove rules to scan results, filtering out matches based on certain criteria.
     * <p>
     * First, matches are found based on path and/or purl:
     * - Rule must match either both path and purl, just the path, or just the purl
     * <p>
     * Then, for each matched result:
     * 1. If none of the matching rules define line ranges -> Remove the result
     * 2. If any matching rules define line ranges -> Only remove if the result's lines overlap with any rule's line range
     *
     * @param results The list of scan results to process
     * @param rules   The list of remove rules to apply
     * @return A filtered list with matching results removed based on the above criteria
     */
    private List<ScanFileResult> applyRemoveRules(@NotNull List<ScanFileResult> results, @NotNull List<RemoveRule> rules) {
        log.debug("Starting remove rules application to {} results", results.size());
        List<ScanFileResult> resultsList = new ArrayList<>(results);

        resultsList.removeIf(result -> {
            List<RemoveRule> matchingRules = findMatchingRules(result, rules);
            log.trace("Found {} matching remove rules for file: {}", matchingRules.size(), result.getFilePath());
            if (matchingRules.isEmpty()) {
                return false;
            }

            // Check if any matching rules have line ranges defined
            List<RemoveRule> rulesWithLineRanges = matchingRules.stream()
                    .filter(rule -> rule.getStartLine() != null && rule.getEndLine() != null)
                    .collect(Collectors.toList());

            // If no rules have line ranges, remove the result
            if (rulesWithLineRanges.isEmpty()) {
                log.debug("Removing entire file - no line ranges specified in rules for file {}", result.getFilePath());
                return true;
            }

            // If we have line ranges, check for overlaps
            String resultLineRangesString = result.getFileDetails().get(0).getLines();
            List<LineRange> resultLineRanges = LineRangeUtils.parseLineRanges(resultLineRangesString);

            boolean shouldRemove = rulesWithLineRanges.stream()
                    .map(rule -> new LineRange(rule.getStartLine(), rule.getEndLine()))
                    .anyMatch(ruleLineRange -> LineRangeUtils.hasOverlappingRanges(resultLineRanges, ruleLineRange));

            if (shouldRemove) {
                log.debug("Removing file {} due to overlapping line ranges", result.getFilePath());
            }

            return shouldRemove;
        });

        log.debug("Remove rules application completed. Results remaining: {}", resultsList.size());
        return resultsList;
    }

    /**
     * Finds and returns a list of matching rules for a scan result.
     * A rule matches if:
     * 1. It has both path and purl, and both match the result
     * 2. It has only a purl (no path), and the purl matches the result
     * 3. It has only a path (no purl), and the path matches the result
     *
     * @param <T>    The rule type. Must extend Rule class
     * @param result The scan result to check
     * @param rules  List of rules to check against
     * @return List of matching rules, empty list if no matches found
     */
    private <T extends Rule> List<T> findMatchingRules(@NotNull ScanFileResult result, @NotNull List<T> rules) {
        log.trace("Finding matching rules for file: {}", result.getFilePath());
        return rules.stream()
                .filter(rule -> {
                    boolean hasPath = rule.getPath() != null && !rule.getPath().isEmpty();
                    boolean hasPurl = rule.getPurl() != null && !rule.getPurl().isEmpty();

                    if (hasPath && hasPurl) {
                        return isPathAndPurlMatch(rule, result);
                    } else if (hasPath) {
                        return isPathOnlyMatch(rule, result);
                    } else if (hasPurl) {
                        return isPurlOnlyMatch(rule, result);
                    }

                    log.warn("Rule {} has neither path nor PURL specified", rule);
                    return false; // Neither path nor purl specified
                }).collect(Collectors.toList());
    }


    /**
     * Checks if both path and purl of the rule match the result
     */
    private boolean isPathAndPurlMatch(Rule rule, ScanFileResult result) {
        return Objects.equals(rule.getPath(), result.getFilePath()) &&
                isPurlMatch(rule.getPurl(), result.getFileDetails().get(0).getPurls());
    }


    /**
     * Checks if the rule's path matches the result (ignoring purl)
     */
    private boolean isPathOnlyMatch(Rule rule, ScanFileResult result) {
        return Objects.equals(rule.getPath(), result.getFilePath());
    }

    /**
     * Checks if the rule's purl matches the result (ignoring path)
     */
    private boolean isPurlOnlyMatch(Rule rule, ScanFileResult result) {
        return isPurlMatch(rule.getPurl(), result.getFileDetails().get(0).getPurls());
    }

    /**
     * Checks if a specific purl exists in an array of purls
     */
    private boolean isPurlMatch(String rulePurl, String[] resultPurls) {
        if (rulePurl == null || resultPurls == null) {
            return false;
        }

        for (String resultPurl : resultPurls) {
            if (Objects.equals(rulePurl, resultPurl)) {
                return true;
            }
        }
        return false;
    }

}
