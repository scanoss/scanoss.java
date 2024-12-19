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

/**
 * Post-processor for SCANOSS scanner results that applies BOM (Bill of Materials) rules
 * to modify scan results after the scanning process. This processor handles two main
 * operations:
 * <p>
 * 1. Remove rules
 * 2. Replace rules
 * </p>
 *
 * The processor maintains an internal Purl2ComponentMap for efficient lookup and
 * transformation of components during the post-processing phase.
 * @see Bom
 * @see ScanFileResult
 * @see ReplaceRule
 */
@Slf4j
@Builder
public class ScannerPostProcessor {

    /**
     * Maps purl to Component (ScanFileDetail)
     */
    private Map<String, ScanFileDetails> purl2ComponentDetailsMap;

    /**
     * Processes scan results according to BOM configuration rules.
     * Applies remove, and replace rules as specified in the configuration.
     *
     * @param scanFileResults List of scan results to process
     * @param bom             Bom containing BOM rules
     * @return List of processed scan results
     */
    public List<ScanFileResult> process(@NotNull List<ScanFileResult> scanFileResults, @NotNull Bom bom) {
        List<ScanFileResult> processedResults = new ArrayList<>(scanFileResults);

        log.info("Starting scan results processing with {} results", scanFileResults.size());
        log.debug("BOM configuration - Remove rules: {}, Replace rules: {}",
                bom.getRemove() != null ? bom.getRemove().size() : 0,
                bom.getReplace() != null ? bom.getReplace().size() : 0);


        purl2ComponentDetailsMap = buildPurl2ComponentDetailsMap(scanFileResults);


        if (bom.getRemove() != null && !bom.getRemove().isEmpty()) {
            log.info("Applying {} remove rules to scan results", bom.getRemove().size());
            processedResults = applyRemoveRules(processedResults, bom.getRemove());
        }

        if (bom.getReplace() != null && !bom.getReplace().isEmpty()) {
            log.info("Applying {} replace rules to scan results", bom.getReplace().size());
            processedResults = applyReplaceRules(processedResults, bom.getReplaceRulesByPriority());
        }

        log.info("Scan results processing completed. Original results: {}, Processed results: {}",
                scanFileResults.size(), processedResults.size());
        return processedResults;
    }

    /**
     * Creates a lookup map that links PURLs to their corresponding component details.
     * This map enables efficient component lookup during the replacement process.
     *
     * @param scanFileResults List of scan results to process
     * @return Map where keys are PURLs and values are their associated component details
     */
    private Map<String, ScanFileDetails> buildPurl2ComponentDetailsMap(@NotNull List<ScanFileResult> scanFileResults) {
        log.debug("Creating Purl Component Map from scan results");

        Map<String, ScanFileDetails> index = new HashMap<>();

        for (ScanFileResult result : scanFileResults) {
            if (result == null || result.getFileDetails() == null) {
                continue;
            }

            // Iterate through file details
            for (ScanFileDetails details : result.getFileDetails()) {
                if (details == null || details.getPurls() == null) {
                    continue;
                }

                // Iterate through purls for each detail
                for (String purl : details.getPurls()) {
                    if (purl == null || purl.trim().isEmpty()) {
                        continue;
                    }

                    // Only store if purl not already in map
                    String trimmedPurl = purl.trim();
                    if (!index.containsKey(trimmedPurl)) {
                        index.put(trimmedPurl, details);
                    }
                }
            }
        }

        log.debug("Purl Component Map created with {} entries", index.size());
        return index;
    }


    /**
     * Applies replacement rules to scan results, updating their PURLs (Package URLs) based on matching rules.
     * If a cached component exists for a replacement PURL, it will be used instead of creating a new one.
     *
     * @param results The list of scan results to process and modify
     * @param rules   The list of replacement rules to apply
     * @return The modified input list of scan results with updated PURLs
     */
    private List<ScanFileResult> applyReplaceRules(@NotNull List<ScanFileResult> results, @NotNull List<ReplaceRule> rules) {
        log.debug("Starting replace rules application for {} results with {} rules", results.size(), rules.size());
        results.forEach(result -> applyReplaceRulesOnResult(result, rules));
        return results;
    }



    /**
     * Processes a single scan result against all replacement rules.
     * Applies the first matching rule found to update the result's package information.
     *
     * @param result The scan result to process
     * @param rules List of replacement rules to check against
     */
    private void applyReplaceRulesOnResult(@NotNull ScanFileResult result, @NotNull List<ReplaceRule> rules) {
        if (!isValidScanResult(result)) {
            log.warn("Invalid scan result structure for file: {}", result.getFilePath());
            return;
        }


        // Find the first matching rule and apply its replacement
        // This ensures only one rule is applied per result, maintaining consistency
        rules.stream()
                .filter(rule -> isMatchingRule(result, rule))
                .findFirst()
                .ifPresent(rule -> updateResultWithReplaceRule(result, rule));
    }

    /**
     * Updates a scan result using the specified replacement rule.
     * Creates a new package URL from the rule and updates all component details
     * within the scan result to use the new package information.
     *
     * @param result The scan result to update
     * @param rule The replacement rule containing the new package URL
     */
    private void updateResultWithReplaceRule(@NotNull ScanFileResult result, @NotNull ReplaceRule rule) {
        PackageURL newPurl = createPackageUrl(rule);
        if (newPurl == null) return;


        List<ScanFileDetails> componentDetails = result.getFileDetails();
        for (ScanFileDetails componentDetail : componentDetails ) {

            if (componentDetail == null) {
                continue;
            }

            ScanFileDetails newFileDetails = createUpdatedResultDetails(componentDetail, newPurl);
            result.getFileDetails().set(0, newFileDetails);

            log.debug("Updated package URL from {} to {} for file: {}",
                    componentDetail.getPurls()[0],
                    newPurl,
                    result.getFilePath());
        }


    }


    /**
     * Creates a PackageURL object from a replacement rule's target URL.
     *
     * @param rule The replacement rule containing the new package URL
     * @return A new PackageURL object, or null if the URL is malformed
     */
    private PackageURL createPackageUrl(ReplaceRule rule) {
        try {
            return new PackageURL(rule.getReplaceWith());
        } catch (MalformedPackageURLException e) {
            log.warn("Failed to parse PURL from replace rule: {}. Skipping", rule);
            return null;
        }
    }


    /**
     * Updates the component details with new package information while preserving existing metadata.
     * Takes the existing component as the base and only overrides specific fields (component name,
     * vendor, PURLs) based on the new package URL. License details will be updated if specified
     * in the replacement rule.
     *
     * @param existingComponent The current component details to use as a base
     * @param newPurl The new package URL containing updated package information
     * @return Updated component details with specific fields overridden
     */
    private ScanFileDetails createUpdatedResultDetails(ScanFileDetails existingComponent,
                                                     PackageURL newPurl) {

        // Check for cached component
        ScanFileDetails cached = purl2ComponentDetailsMap.get(newPurl.toString());

        if (cached != null) {
            //TODO: Clarification on copyright, Vulns, etc
            //currentComponent.toBuilder().component().vendor().purls().licenseDetails()


             //Version if we have a package url with version
            //pkg:github/scanoss@1.0.0


            return cached.toBuilder()
                    .file(existingComponent.getFile())
                    .fileHash(existingComponent.getFileHash())
                    .fileUrl(existingComponent.getFileUrl())
                    .purls(new String[]{newPurl.toString()})
                    .component(newPurl.getName())
                    .vendor(newPurl.getNamespace())
                    .build();
        }

        // If no cached info, create minimal version
        return existingComponent.toBuilder()
                .copyrightDetails(new CopyrightDetails[]{})     //TODO: Check if we need the empty Object
                .licenseDetails(new LicenseDetails[]{})
                .vulnerabilityDetails(new VulnerabilityDetails[]{})
                .purls(new String[]{newPurl.toString()})
                .url("")  // TODO: Implement purl2Url in PackageURL upstream library
                .component(newPurl.getName())
                .vendor(newPurl.getNamespace())
                .build();
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
    public List<ScanFileResult> applyRemoveRules(@NotNull List<ScanFileResult> results, @NotNull List<RemoveRule> rules) {
        log.debug("Starting remove rules application to {} results", results.size());
        List<ScanFileResult> resultsList = new ArrayList<>(results);
        resultsList.removeIf(result -> matchesRemovalCriteria(result, rules));
        log.debug("Remove rules application completed. Results remaining: {}", resultsList.size());
        return resultsList;
    }


    /**
     * Determines if a scan result should be excluded based on the removal rules.
     * <p>
     * For each rule, checks:
     * 1. If the result matches the rule's path/purl patterns
     * 2. Then applies line range logic:
     *    - If rule has no line range specified: Result is removed
     *    - If rule has line range specified: Result is only removed if its lines overlap with the rule's range
     * </p>
     * @param result The scan result to evaluate
     * @param rules List of removal rules to check against
     * @return true if the result should be removed, false otherwise
     */
    private Boolean matchesRemovalCriteria(@NotNull ScanFileResult result, @NotNull List<RemoveRule> rules) {

        if (!isValidScanResult(result)) {
            log.warn("Invalid scan result structure for file: {}", result.getFilePath());
            return false;
        }

        return rules.stream()
                .filter(rule -> isMatchingRule(result, rule))
                .anyMatch(rule -> {
                    // Process line range conditions:
                    // - returns true if rule has no line range specified
                    // - returns true if rule has line range AND result overlaps with it
                    // - returns false otherwise (continue checking remaining rules)
                    boolean ruleHasLineRange = rule.getStartLine() != null && rule.getEndLine() != null;
                    return !ruleHasLineRange || isLineRangeMatch(rule, result);
                });
    }


    /**
     * Checks if a scan result matches the path and/or PURL patterns defined in a rule.
     *
     * The match is considered successful if any of these conditions are met:
     * 1. Rule has both path and PURL defined: Both must match the result
     * 2. Rule has only PURL defined: PURL must match the result
     * 3. Rule has only path defined: Path must match the result
     *
     * @param result The scan result to check
     * @param rule The rule containing the patterns to match against
     * @param <T> Type parameter extending Rule class
     * @return true if the result matches the rule's patterns according to above conditions
     */
    private <T extends Rule> Boolean isMatchingRule(@NotNull ScanFileResult result, @NotNull T rule) {
        // Check if rule has valid path and/or PURL patterns
        boolean hasPath = rule.getPath() != null && !rule.getPath().isEmpty();
        boolean hasPurl = rule.getPurl() != null && !rule.getPurl().isEmpty();

        // Check three possible matching conditions:
        // 1. Both path and PURL match
        if (hasPath && hasPurl && isPathAndPurlMatch(rule, result)) {
            return true;
        // 2. Only PURL match required and matches
        } else if (hasPurl && isPurlOnlyMatch(rule, result)) {
            return true;
        // 3. Only path match required and matches
        } if (hasPath && isPathOnlyMatch(rule, result)) {
            return true;
        }

        return false;
    }


    /**
     * Checks if line range of the remove rule match the result
     */
    private boolean isLineRangeMatch(RemoveRule rule, ScanFileResult result) {
        LineRange ruleLineRange = new LineRange(rule.getStartLine(), rule.getEndLine());

        String lines = result.getFileDetails().get(0).getLines();
        List<LineRange> resultLineRanges = LineRangeUtils.parseLineRanges(lines);

        return LineRangeUtils.hasOverlappingRanges(resultLineRanges,ruleLineRange);
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

    /**
     * Validates if a scan result has the required fields for rule processing.
     *
     * @param result The scan result to validate
     * @return true if the result has valid file details and PURLs
     */
    private boolean isValidScanResult(@NotNull ScanFileResult result) {
        return result.getFileDetails() != null
                && !result.getFileDetails().isEmpty()
                && result.getFileDetails().get(0) != null;
    }
}
