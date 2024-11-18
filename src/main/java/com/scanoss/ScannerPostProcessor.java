package com.scanoss;

import com.scanoss.dto.ScanFileDetails;
import com.scanoss.dto.ScanFileResult;
import com.scanoss.settings.Bom;
import com.scanoss.settings.ReplaceRule;
import com.scanoss.settings.Rule;
import com.scanoss.settings.Settings;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ScannerPostProcessor {

    private Map<String, ScanFileDetails> componentIndex = new HashMap<>();

    /**
     * Processes scan results according to BOM configuration rules.
     * Applies remove, and replace rules as specified in the configuration.
     *
     * @param scanFileResults List of scan results to process
     * @param bom Bom containing BOM rules
     * @return List of processed scan results
     */
    public List<ScanFileResult> process(@NotNull List<ScanFileResult> scanFileResults, @NotNull Bom bom) {
        createComponentIndex(scanFileResults);

        List<ScanFileResult> processedResults = new ArrayList<>(scanFileResults);

        // Apply remove rules
        if (bom.getRemove() != null && !bom.getRemove().isEmpty()) {
            processedResults = applyRemoveRules(processedResults, bom.getRemove());
        }

        //Apply replace rules
        if (bom.getReplace() != null && !bom.getReplace().isEmpty()) {
            processedResults = applyReplaceRules(processedResults, bom.getReplace());
        }

        return processedResults;
    }

    /**
     * Creates a map of PURL (Package URL) to ScanFileDetails from a list of scan results.
     *
     * @param scanFileResults List of scan results to process
     * @return Map where keys are PURLs and values are corresponding ScanFileDetails
     */
    private void createComponentIndex(List<ScanFileResult> scanFileResults) {
        if (scanFileResults == null) {
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
                ));    }


    /**
     * Applies replacement rules to scan results, updating their PURLs (Package URLs) based on matching rules.
     * If a cached component exists for a replacement PURL, it will be used instead of creating a new one.
     *
     * @param results The original list of scan results to process
     * @param replaceRules The list of replacement rules to apply
     * @return A new list containing the processed scan results with updated PURLs
     */
    private List<ScanFileResult> applyReplaceRules(List<ScanFileResult> results, List<ReplaceRule> replaceRules) {
        if (results == null || replaceRules == null) {
            return results;
        }

        List<ScanFileResult> resultsList = new ArrayList<>(results);

        for (ScanFileResult result : resultsList) {
            findMatchingRule(result, replaceRules).ifPresent(matchedRule -> {

                String replacementPurl = matchedRule.getReplaceWith();
                if (replacementPurl == null || replacementPurl.trim().isEmpty()) {
                    return; //Empty replacement PURL found
                }

                // Try to get cached component first
                ScanFileDetails cachedComponent = this.componentIndex.get(replacementPurl);
                if (cachedComponent != null) {
                    result.getFileDetails().set(0, cachedComponent); // Use cached component if available
                } else {
                    result.getFileDetails().get(0).setPurls(new String[] { replacementPurl.trim() }); // Create new PURL array if no cached component exists
                }
            });
        }
        return resultsList;
    }


    /**
     * Applies remove rules to the scan results.
     * A result will be removed if:
     * 1. The remove rule has both path and purl, and both match the result
     * 2. The remove rule has only purl (no path), and the purl matches the result
     */
    private List<ScanFileResult> applyRemoveRules(@NotNull List<ScanFileResult> results, @NotNull List<Rule> removeRules) {
        List<ScanFileResult> resultsList = new ArrayList<>(results);

        resultsList.removeIf(result -> findMatchingRule(result, removeRules).isPresent());
        return resultsList;
    }

    /**
     * Finds and returns the first matching rule for a scan result.
     * A rule matches if:
     * 1. It has both path and purl, and both match the result
     * 2. It has only a purl (no path), and the purl matches the result
     * 3. It has only a path (no purl), and the path matches the result
     *
     * @param <T> The rule type. Must extend Rule class
     * @param result The scan result to check
     * @param rules List of rules to check against
     * @return Optional containing the first matching rule, or empty if no match found
     */
    private <T extends Rule> Optional<T> findMatchingRule(ScanFileResult result, List<T> rules) {
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

                    return false; // Neither path nor purl specified
                })
                .findFirst();
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
