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
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ScannerPostProcessor {

    private Map<String, ScanFileDetails> componentIndex = new HashMap<>();

    /**
     * Processes scan results according to BOM configuration rules.
     * Applies remove, and replace rules as specified in the configuration.
     *
     * @param scanFileResults List of scan results to process
     * @param bom             Bom containing BOM rules
     * @return List of processed scan results
     */
    public List<ScanFileResult> process(@NotNull List<ScanFileResult> scanFileResults, @NotNull Bom bom) {
        createComponentIndex(scanFileResults);

        List<ScanFileResult> processedResults = new ArrayList<>(scanFileResults);

        if (bom.getRemove() != null && !bom.getRemove().isEmpty()) {
            processedResults = applyRemoveRules(processedResults, bom.getRemove());
        }

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
                ));
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
        List<ScanFileResult> resultsList = new ArrayList<>(results);

        for (ScanFileResult result : resultsList) {

            for (ReplaceRule rule : this.findMatchingRules(result, rules)) {


                PackageURL newPurl;
                try {
                    newPurl = new PackageURL(rule.getReplaceWith());
                } catch (MalformedPackageURLException e) {
                    log.error("ERROR: Parsing purl from rule: {} - {}", rule, e.getMessage());
                    continue;
                }

                LicenseDetails[] licenseDetails = new LicenseDetails[]{LicenseDetails.builder().name(rule.getLicense()).build()};

                ScanFileDetails cachedFileDetails = this.componentIndex.get(newPurl.toString());
                ScanFileDetails currentFileDetails = result.getFileDetails().get(0);
                ScanFileDetails newFileDetails;

                if (cachedFileDetails != null) {

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

                    newFileDetails = currentFileDetails;

                    newFileDetails.setCopyrightDetails(new CopyrightDetails[]{});
                    newFileDetails.setLicenseDetails(new LicenseDetails[]{});
                    newFileDetails.setVulnerabilityDetails(new VulnerabilityDetails[]{});
                    newFileDetails.setPurls(new String[]{newPurl.toString()});
                    newFileDetails.setUrl("");

                    newFileDetails.setComponent(newPurl.getName());
                    newFileDetails.setVendor(newPurl.getNamespace());
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
        List<ScanFileResult> resultsList = new ArrayList<>(results);

        resultsList.removeIf(result -> {
            List<RemoveRule> matchingRules = findMatchingRules(result, rules);
            if (matchingRules.isEmpty()) {
                return false;
            }

            // Check if any matching rules have line ranges defined
            List<RemoveRule> rulesWithLineRanges = matchingRules.stream()
                    .filter(rule -> rule.getStartLine() != null && rule.getEndLine() != null)
                    .collect(Collectors.toList());

            // If no rules have line ranges, remove the result
            if (rulesWithLineRanges.isEmpty()) {
                return true;
            }

            // If we have line ranges, check for overlaps
            String resultLineRangesString = result.getFileDetails().get(0).getLines();
            List<LineRange> resultLineRanges = LineRangeUtils.parseLineRanges(resultLineRangesString);

            return rulesWithLineRanges.stream()
                    .map(rule -> new LineRange(rule.getStartLine(), rule.getEndLine()))
                    .anyMatch(ruleLineRange -> LineRangeUtils.hasOverlappingRanges(resultLineRanges, ruleLineRange));
        });

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
