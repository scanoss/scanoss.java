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
import com.scanoss.dto.enums.MatchType;
import com.scanoss.settings.Bom;
import com.scanoss.settings.RemoveRule;
import com.scanoss.settings.ReplaceRule;
import com.scanoss.settings.Rule;
import com.scanoss.utils.LineRange;
import com.scanoss.utils.LineRangeUtils;
import com.scanoss.utils.Purl2Url;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

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
    public List<ScanFileResult> process(@NonNull List<ScanFileResult> scanFileResults, @NonNull Bom bom) {
        int removeSize = bom.getRemoveSize();
        int replaceSize = bom.getReplaceSize();
        log.info("Starting scan results processing with {} results", scanFileResults.size());
        log.debug("BOM configuration - Remove rules: {}, Replace rules: {}", removeSize, replaceSize);

        if (scanFileResults.isEmpty()) {
            log.info("No scan results found. Skipping: {}", bom);
        }

        buildPurl2ComponentDetailsMap(scanFileResults);
        List<ScanFileResult> processedResults = new ArrayList<>(scanFileResults);
        if (removeSize > 0) {
            log.info("Applying {} remove rules to scan results", removeSize);
            applyRemoveRules(processedResults, bom.getRemove());
        }
        if (replaceSize > 0) {
            log.info("Applying {} replace rules to scan results", replaceSize);
            applyReplaceRules(processedResults, bom.getReplaceRulesByPriority());
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
     */
    private void buildPurl2ComponentDetailsMap(@NonNull List<ScanFileResult> scanFileResults) {
        log.debug("Creating Purl Component Map from scan results");
        purl2ComponentDetailsMap = new HashMap<>();
        for (ScanFileResult result : scanFileResults) {
            List<ScanFileDetails> fileDetails = result != null ? result.getFileDetails() : null;
            if (fileDetails == null) {
                log.warn("Null result or empty scan file result. Skipping: {}", result);
                continue;
            }
            // Iterate through file details
            for (ScanFileDetails details : fileDetails) {
                if (details != null && details.getMatchType() == MatchType.none) {
                    log.warn("Skipping no match for file: {}", result.getFilePath());
                    continue;
                }

                String[] purls = details != null ? details.getPurls() : null;
                if (purls == null) {
                    log.warn("Null details or empty scan file result details. Skipping: {}", details);
                    continue;
                }
                // Iterate through purls for each detail
                for (String purl : purls) {
                    String trimmedPurl = purl != null ? purl.trim() : "";
                    if (trimmedPurl.isEmpty()) {
                        log.warn("Empty purl details found. Skipping: {}", details);
                        continue;
                    }
                    // Only store if purl not already in map
                    if (!purl2ComponentDetailsMap.containsKey(trimmedPurl)) {
                        purl2ComponentDetailsMap.put(trimmedPurl, details);
                    }
                }
            }
        }
        log.debug("Purl Component Map created with {} entries", purl2ComponentDetailsMap.size());
    }


    /**
     * Applies replacement rules to scan results, updating their PURLs (Package URLs) based on matching rules.
     * If a cached component exists for a replacement PURL, it will be used instead of creating a new one.
     *
     * @param results The list of scan results to process and modify
     * @param rules   The list of replacement rules to apply
     * @return The modified input list of scan results with updated PURLs
     */
    private void applyReplaceRules(@NonNull List<ScanFileResult> results, @NonNull List<ReplaceRule> rules) {
        log.debug("Starting replace rules application for {} results with {} rules", results.size(), rules.size());
        results.forEach(result -> applyReplaceRulesOnResult(result, rules));
    }


    /**
     * Processes a single scan result against all replacement rules.
     * Applies the first matching rule found to update the result's package information.
     *
     * @param result The scan result to process
     * @param rules List of replacement rules to check against
     */
    private void applyReplaceRulesOnResult(@NonNull ScanFileResult result, @NonNull List<ReplaceRule> rules) {
        // Make sure it's a valid result before processing
        if (hasInvalidStructure(result)) {
            log.warn("Scan result has invalid structure - missing required fields for file: {}", result.getFilePath());
            return;
        }

        if (hasNoValidMatch(result)) {
            log.debug("Scan result has no valid matches for file: {}", result.getFilePath());
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
    private void updateResultWithReplaceRule(@NonNull ScanFileResult result, @NonNull ReplaceRule rule) {
        PackageURL newPurl = createPackageUrl(rule);
        if (newPurl == null)
            return;
        List<ScanFileDetails> componentDetails = result.getFileDetails();
        if (componentDetails == null) {
            log.warn("Null scan file details found. Skipping: {}", result);
            return;
        }
        for (ScanFileDetails componentDetail : componentDetails ) {
            if (componentDetail == null) {
                log.warn("Null scan file component details found. Skipping: {}", result);
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
     * @return A new PackageURL object, or <code>null</code> if the URL is malformed
     */
    private PackageURL createPackageUrl(@NonNull ReplaceRule rule) {
        try {
            return new PackageURL(rule.getReplaceWith());
        } catch (MalformedPackageURLException e) {
            log.warn("Failed to parse PURL from replace rule: {}. Skipping", rule);
        }
        return null;
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
            return cached.toBuilder()
                    .file(existingComponent.getFile())
                    .fileHash(existingComponent.getFileHash())
                    .fileUrl(existingComponent.getFileUrl())
                    .lines(existingComponent.getLines())
                    .purls(new String[]{newPurl.toString()})
                    .component(newPurl.getName())
                    .vendor(newPurl.getNamespace())
                    .build();
        }
        // If no cached info, create minimal version
        return existingComponent.toBuilder()
                .copyrightDetails(new CopyrightDetails[]{})
                .licenseDetails(new LicenseDetails[]{})
                .vulnerabilityDetails(new VulnerabilityDetails[]{})
                .version(null)
                .purls(new String[]{newPurl.toString()})
                .url(Purl2Url.isSupported(newPurl) ? Purl2Url.convert(newPurl) : "")
                .component(newPurl.getName())
                .vendor(newPurl.getNamespace())
                .build();
    }

    /**
     * Marks all components in the list as non-matching by replacing each component
     * with a new instance that has MatchType.NONE while preserving the serverDetails
     * Modifies the input list in place using List.replaceAll().
     *
     * @param components List of scan file details to be marked as non-matching
     */
    private void markComponentsAsNonMatch(List<ScanFileDetails> components) {
        components.replaceAll(component ->
                ScanFileDetails.builder()
                        .matchType(MatchType.none)
                        .serverDetails(component.getServerDetails())
                        .build()
        );
    }

    /**
     * Applies remove rules to scan results, filtering out matches based on certain criteria.
     * <p>
     * First, matches are found based on path and/or purl:
     * - Rule must match either both path and purl, just the path, or just the purl
     * <p>
     * Then, for each matched result:
     * 1. If none of the matching rules define line ranges &rarr; Remove the result
     * 2. If any matching rules define line ranges &rarr; Only remove if the result's lines overlap with any rule's line range
     *
     * @param results The list of scan results to process
     * @param rules   The list of remove rules to apply
     */
    public void applyRemoveRules(@NonNull List<ScanFileResult> results, @NonNull List<RemoveRule> rules) {
        log.debug("Starting remove rules application to {} results", results.size());
        results.stream()
                .filter(result -> matchesRemovalCriteria(result, rules))
                .forEach(result -> markComponentsAsNonMatch(result.getFileDetails()));
        log.debug("Remove rules application completed. Results remaining: {}", results.size());
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
    private Boolean matchesRemovalCriteria(@NonNull ScanFileResult result, @NonNull List<RemoveRule> rules) {
        // Make sure it's a valid result before processing
        if (hasInvalidStructure(result)) {
            log.warn("Scan result has invalid structure - missing required fields for file: {}", result.getFilePath());
            return false;
        }

        if (hasNoValidMatch(result)) {
            log.debug("Scan result has no valid matches for file: {}", result.getFilePath());
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
                    return !ruleHasLineRange || isRemoveLineRangeMatch(rule, result);
                });
    }

    /**
     * Checks if a scan result matches the path and/or PURL patterns defined in a rule.
     * The match is considered successful if any of these conditions are met:
     * 1. Rule has both a path and PURL defined: Both must match the result
     * 2. Rule has only a PURL defined: PURL must match the result
     * 3. Rule has only a path defined: Path must match the result
     *
     * @param result The scan result to check
     * @param rule The rule containing the patterns to match against
     * @param <T> Type parameter extending Rule class
     * @return true if the result matches the rule's patterns according to above conditions
     */
    private <T extends Rule> Boolean isMatchingRule(@NonNull ScanFileResult result, @NonNull T rule) {
        // Check if rule has valid path and/or PURL patterns
        boolean hasPath = rule.getPath() != null && !rule.getPath().isEmpty();
        boolean hasPurl = rule.getPurl() != null && !rule.getPurl().isEmpty();

        // Check three possible matching conditions:

        // 1. Both path and PURL match
        if (hasPath && hasPurl) {
            return isPathAndPurlMatch(rule, result);
        }
        // 2. Only PURL match required and matches
        if (hasPurl) {
            return isPurlOnlyMatch(rule, result);
        }
        // 3. Only path match required and matches
        if (hasPath) {
            return isPathOnlyMatch(rule, result);
        }

        return false;
    }

    /**
     * Checks if the line range of the remove rule match the result
     *
     * @param rule Remove Rule
     * @param result Scan file Result
     * @return <code>true</code> if remove rule is in range, <code>false</code> otherwise
     */
    private boolean isRemoveLineRangeMatch(@NonNull RemoveRule rule, @NonNull ScanFileResult result) {
        LineRange ruleLineRange = new LineRange(rule.getStartLine(), rule.getEndLine());

        String lines = result.getFileDetails().get(0).getLines();
        List<LineRange> resultLineRanges = LineRangeUtils.parseLineRanges(lines);

        return LineRangeUtils.hasOverlappingRanges(resultLineRanges,ruleLineRange);
    }

    /**
     * Checks if both path and purl of the rule match the result
     *
     * @param rule BOM Rule
     * @param result Scan file Result
     * @return <code>true</code> if it matches, <code>false</code> otherwise
     */
    private boolean isPathAndPurlMatch(@NonNull Rule rule, @NonNull ScanFileResult result) {
        return result.getFilePath().startsWith(rule.getPath()) &&
                isPurlMatch(rule.getPurl(), result.getFileDetails().get(0).getPurls());
    }


    /**
     * Checks if the rule's path matches the result (ignoring purl)
     *
     * @param rule BOM Rule
     * @param result Scan file Results
     * @return <code>true</code> if it matches, <code>false</code> otherwise
     */
    private boolean isPathOnlyMatch(@NonNull Rule rule, @NonNull ScanFileResult result) {
        return result.getFilePath().startsWith(rule.getPath());
    }

    /**
     * Checks if the rule's purl matches the result (ignoring path)
     *
     * @param rule BOM Rule
     * @param result Scan file Result
     * @return <code>true</code> if it matches, <code>false</code> otherwise
     */
    private boolean isPurlOnlyMatch(@NonNull Rule rule, @NonNull ScanFileResult result) {
        return isPurlMatch(rule.getPurl(), result.getFileDetails().get(0).getPurls());
    }

    /**
     * Checks if a specific purl exists in an array of purls
     *
     * @param rulePurl PURL from Rule
     * @param resultPurls List of Scan file Result PURLs
     * @return <code>true</code> if it matches, <code>false</code> otherwise
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
     * Checks if a scan result contains the minimum required data structure for processing.
     * This validation ensures that:
     * 1. The result has a valid file path identifier
     * 2. The result contains a non-empty list of scan details
     * 3. The primary scan detail entry (first in the list) exists
     *
     * This structural validation is a prerequisite for any further processing of scan results,
     * such as match analysis or rule processing. Without these basic elements, the scan result
     * cannot be meaningfully processed.
     *
     * @param result The scan result to validate
     * @return true if the basic structure is invalid, false if valid
     */
    private boolean hasInvalidStructure(@NonNull ScanFileResult result) {
        String filepath = result.getFilePath();
        List<ScanFileDetails> details = result.getFileDetails();
        return filepath == null ||
                details == null ||
                details.isEmpty() ||
                details.get(0) == null;
    }

    /**
     * Checks if the scan result has a valid match.
     * A result is considered to have no valid match if:
     * - Match type is 'none'
     * - Lines array is null
     * - PURLs array is null or empty
     *
     * @param result The scan result to validate
     * @return true if there's no valid match, false if there is a valid match
     */
    private boolean hasNoValidMatch(@NonNull ScanFileResult result) {
        if (hasInvalidStructure(result)) {
            return true;
        }

        ScanFileDetails firstDetail = result.getFileDetails().get(0);
        return firstDetail.getMatchType() == MatchType.none ||
                firstDetail.getLines() == null ||
                firstDetail.getPurls() == null ||
                firstDetail.getPurls().length == 0;
    }

}
