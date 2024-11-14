package com.scanoss;

import com.scanoss.dto.ScanFileResult;
import com.scanoss.exceptions.ScannerPostProcessorException;
import com.scanoss.settings.BomConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScannerPostProcessor {


    /**
     * Processes scan results according to BOM configuration rules.
     * Applies remove, and replace rules as specified in the configuration.
     *
     * @param scanFileResults List of scan results to process
     * @param bomConfiguration Configuration containing BOM rules
     * @return List of processed scan results
     */
    public List<ScanFileResult> process(List<ScanFileResult> scanFileResults, BomConfiguration bomConfiguration) {
        if (scanFileResults == null || bomConfiguration == null) {
            throw new ScannerPostProcessorException("Scan results and BOM configuration cannot be null");
        }

        List<ScanFileResult> processedResults = new ArrayList<>(scanFileResults);

        // Apply remove rules
        if (bomConfiguration.getBom().getRemove() != null && !bomConfiguration.getBom().getRemove().isEmpty()) {
            processedResults = applyRemoveRules(processedResults, bomConfiguration.getBom().getRemove());
        }

        return processedResults;
    }

    /**
     * Applies remove rules to the scan results.
     * A result will be removed if:
     * 1. The remove rule has both path and purl, and both match the result
     * 2. The remove rule has only purl (no path), and the purl matches the result
     */
    private List<ScanFileResult> applyRemoveRules(List<ScanFileResult> results, List<BomConfiguration.Component> removeRules) {
        if (results == null || removeRules == null) {
            return results;
        }

        List<ScanFileResult> resultsList = new ArrayList<>(results);

        resultsList.removeIf(result -> shouldRemoveResult(result, removeRules));
        return resultsList;
    }

    /**
     * Determines if a result should be removed based on the remove rules.
     * Returns true if the result should be removed, false if it should be kept.
     */
    private boolean shouldRemoveResult(ScanFileResult result, List<BomConfiguration.Component> removeRules) {
        for (BomConfiguration.Component rule : removeRules) {
            if (isMatchingRule(rule, result)) {
                return true; // Found a matching rule, remove the result
            }
        }

        return false; // No matching remove rules found, keep the result
    }

    /**
     * Checks if a rule matches a scan result.
     * A rule matches if:
     * 1. It has both path and purl, and both match the result
     * 2. It has only a purl (no path), and the purl matches the result
     * 3. It has only a path (no purl), and the path matches the result
     */
    private boolean isMatchingRule(BomConfiguration.Component rule, ScanFileResult result) {
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
    }

    /**
     * Checks if both path and purl of the rule match the result
     */
    private boolean isPathAndPurlMatch(BomConfiguration.Component rule, ScanFileResult result) {
        return Objects.equals(rule.getPath(), result.getFilePath()) &&
                isPurlMatch(rule.getPurl(), result.getFileDetails().get(0).getPurls());
    }


    /**
     * Checks if the rule's path matches the result (ignoring purl)
     */
    private boolean isPathOnlyMatch(BomConfiguration.Component rule, ScanFileResult result) {
        return Objects.equals(rule.getPath(), result.getFilePath());
    }

    /**
     * Checks if the rule's purl matches the result (ignoring path)
     */
    private boolean isPurlOnlyMatch(BomConfiguration.Component rule, ScanFileResult result) {
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
