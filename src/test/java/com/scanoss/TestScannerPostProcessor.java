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

import com.google.gson.JsonObject;
import com.scanoss.dto.ScanFileResult;
import com.scanoss.settings.Bom;
import com.scanoss.settings.RemoveRule;
import com.scanoss.settings.ReplaceRule;
import com.scanoss.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.scanoss.TestConstants.jsonResultsString;
import static org.junit.Assert.*;

@Slf4j
public class TestScannerPostProcessor {
    private ScannerPostProcessor scannerPostProcessor;
    private List<ScanFileResult> sampleScanResults;
    private List<ScanFileResult> longScanResults;

    @Before
    public void Setup() throws URISyntaxException, IOException {
        log.info("Starting ScannerPostProcessor test cases...");
        scannerPostProcessor = ScannerPostProcessor.builder().build();
        JsonObject jsonObject = JsonUtils.toJsonObject(jsonResultsString);
        sampleScanResults = JsonUtils.toScanFileResultsFromObject(jsonObject);      //TODO: Create sampleScanResults with a helper function


        var resource = getClass().getClassLoader().getResource("results.json");
        if (resource == null) {
            throw new IllegalStateException(
                    "Required test resource 'results.json' not found. Please ensure it exists in src/test/resources/data/"
            );
        }


        String json = Files.readString(Paths.get(resource.toURI()), StandardCharsets.UTF_8);
        longScanResults = JsonUtils.toScanFileResultsFromObject(JsonUtils.toJsonObject(json));

    }


    /**
     * TESTING REMOVE RULES
     **/
    @Test
    public void TestRemoveRuleWithPathAndPurl() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        RemoveRule rule = RemoveRule.builder()
                .purl("pkg:github/twbs/bootstrap")
                .path("CMSsite/admin/js/npm.js")
                .build();

        Bom bom = Bom.builder().remove(rule).build();

        // Process results
        List<ScanFileResult> results = scannerPostProcessor.process(sampleScanResults, bom);

        // Verify
        assertEquals("Should have one result less after removal", sampleScanResults.size() - 1, results.size());
        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestRemoveRuleWithPurlOnly() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        // Setup remove rule with only purl
        RemoveRule removeRule = RemoveRule.builder()
                .purl("pkg:npm/mip-bootstrap")
                .build();

        Bom bom = Bom.builder().
                remove(Collections.singletonList(removeRule))
                .build();

        // Process results
        List<ScanFileResult> results = scannerPostProcessor.process(sampleScanResults, bom);

        // Verify
        assertEquals("Size should decrease by 1 after removal",
                sampleScanResults.size() - 1,
                results.size());

        assertFalse("Should remove file CMSsite/admin/js/npm.js",
                results.stream().anyMatch(r -> r.getFilePath().matches("CMSsite/admin/js/npm.js")));

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestNoMatchingRemoveRules() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);


        // Setup non-matching remove rule
        RemoveRule removeRule = RemoveRule.builder()
                .purl("pkg:github/non-existing/lib@1.0.0")
                .path("non/existing/path.c")
                .build();

        Bom bom = Bom.builder().
                remove(Collections.singletonList(removeRule))
                .build();


        // Process results
        List<ScanFileResult> results = scannerPostProcessor.process(longScanResults, bom);

        // Verify
        assertEquals("Should keep all results", longScanResults.size(), results.size());

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestMultipleRemoveRules() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        // Setup multiple remove rules
        Bom bom = Bom.builder().
                remove(Arrays.asList(
                        RemoveRule.builder()
                                .purl("pkg:npm/myoneui")
                                .path("CMSsite/admin/js/npm.js")
                                .build(),

                        RemoveRule.builder()
                                .purl("pkg:pypi/scanoss")
                                .build(),

                        RemoveRule.builder()
                                .path("scanoss/__init__.py")
                                .build(),

                        RemoveRule.builder()
                                .path("src/spdx.c")
                                .build()
                ))
                .build();

        // Process results
        List<ScanFileResult> results = scannerPostProcessor.process(sampleScanResults, bom);

        // Verify
        assertFalse("Should keep scanoss/__init__.py since it's a non match", results.isEmpty());

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestEmptyRemoveRules() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        Bom bom = Bom.builder()
                .build();

        // Process results with empty remove rules
        List<ScanFileResult> results = scannerPostProcessor.process(sampleScanResults, bom);

        // Verify
        assertEquals("Should keep all results", sampleScanResults.size(), results.size());
        assertEquals("Results should match original", sampleScanResults, results);

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void testRemoveRuleWithNonOverlappingLineRanges() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        // Setup remove rule with non-overlapping line ranges
        Bom bom = Bom.builder()
                .remove(Collections.singletonList(
                        RemoveRule.builder()
                                .path("src/spdx.c")
                                .startLine(1)
                                .endLine(10)  // Before the first range
                                .build()
                ))
                .build();


        // Process results
        List<ScanFileResult> results = scannerPostProcessor.process(sampleScanResults, bom);

        // Verify - should keep because lines don't overlap
        assertEquals("Results should match original", sampleScanResults.size(), results.size());

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void testRemoveRuleWithOverlappingLineRanges() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        // Setup remove rule with overlapping line ranges
        Bom bom = Bom.builder()
                .remove(Collections.singletonList(
                        RemoveRule.builder()
                                .path("src/spdx.c")
                                .startLine(40)
                                .endLine(60)  // Overlaps with 11-52
                                .build()
                ))
                .build();

        // Process results
        List<ScanFileResult> results = scannerPostProcessor.process(sampleScanResults, bom);

        // Verify - should remove because lines overlap
        assertEquals("Should have one result less after removal", sampleScanResults.size() - 1, results.size());

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void testMultipleRemoveRulesWithMixedLineRanges() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        // Setup multiple remove rules with different line range configurations
        Bom bom = Bom.builder()
                .remove(Arrays.asList(
                        RemoveRule.builder()
                                .path("src/spdx.c")
                                .startLine(1)
                                .endLine(10)  // Non-overlapping
                                .build(),
                        RemoveRule.builder()
                                .path("src/spdx.c")
                                .startLine(40)
                                .endLine(60)  // Overlapping
                                .build()
                ))
                .build();


        // Process results
        List<ScanFileResult> results = scannerPostProcessor.process(sampleScanResults, bom);

        assertEquals("Should have one result less after removal", sampleScanResults.size() - 1, results.size());

        log.info("Finished {} -->", methodName);
    }


    /**
     * TESTING REPLACE RULES
     **/
    @Test
    public void TestReplaceRuleWithEmptyPurl() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        // Setup replace rule with empty PURL
        ReplaceRule replace = ReplaceRule.builder()
                .purl("pkg:github/scanoss/scanoss.py")
                .replaceWith("")
                .build();

        Bom bom = Bom.builder()
                .replace(Collections.singletonList(replace))
                .build();


        // Find the specific result for scanoss.py
        Optional<ScanFileResult> originalResult = sampleScanResults.stream()
                .filter(r -> r.getFilePath().equals("scanoss/api/__init__.py"))
                .findFirst();

        assertTrue("Original result should exist", originalResult.isPresent());
        String originalPurl = originalResult.get().getFileDetails().get(0).getPurls()[0];


        // Process results
        List<ScanFileResult> results = scannerPostProcessor.process(sampleScanResults, bom);

        Optional<ScanFileResult> processedResult = results.stream()
                .filter(r -> r.getFilePath().equals("scanoss/api/__init__.py"))
                .findFirst();

        assertTrue("Processed result should exist", processedResult.isPresent());

        // Verify original PURL remains unchanged
        String resultPurl = processedResult.get().getFileDetails().get(0).getPurls()[0];
        assertEquals("PURL should remain unchanged with empty replacement", originalPurl, resultPurl);

        log.info("Finished {} -->", methodName);
    }

    @Test()
    public void TestReplaceRuleWithPurl() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);


        // Setup replace rule with empty PURL
        ReplaceRule replace = ReplaceRule.builder()
                .purl("pkg:github/scanoss/scanoss.py")
                .replaceWith("pkg:github/scanoss/scanner.c")
                .build();

        Bom bom = Bom.builder()
                .replace(Collections.singletonList(replace))
                .build();


        List<ScanFileResult> results = scannerPostProcessor.process(sampleScanResults, bom);

        Optional<ScanFileResult> processedResult = results.stream()
                .filter(r -> r.getFilePath().equals("scanoss/api/__init__.py"))
                .findFirst();

        assertTrue("Processed result should exist", processedResult.isPresent());

        // Verify exactly one PURL exists and it's the correct one
        String[] processedPurls = processedResult.get().getFileDetails().get(0).getPurls();
        assertEquals("Should have exactly one PURL", 1, processedPurls.length);
        assertEquals("PURL should be scanner.c",
                "pkg:github/scanoss/scanner.c", processedPurls[0]);

        log.info("Finished {} -->", methodName);

    }


    @Test()
    public void TestOriginalPurlExistsWhenNoReplacementRule() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        String originalPurl = "pkg:github/scanoss/scanner.c";

        // Setup BOM without replace rule - expecting original PURL to exist
        Bom bom = Bom.builder()
                .build();

        List<ScanFileResult> results = scannerPostProcessor.process(longScanResults, bom);

        assertNotNull("Results should not be null", results);
        assertFalse("Results should not be empty", results.isEmpty());

        List<String> allPurls = results.stream()
                .map(result -> result.getFileDetails().get(0).getPurls())
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());

        log.info("All PURLs found: {}", allPurls);
        log.info("Original PURL we're looking for: '{}'", originalPurl);


        boolean hasOriginalPurl = results.stream()
                .map(result -> result.getFileDetails().get(0).getPurls())
                .flatMap(Arrays::stream)
                .anyMatch(purl -> {
                    log.info("Comparing: '{}' with '{}' = {}",
                            purl, originalPurl, purl.equals(originalPurl));
                    return purl.equals(originalPurl);
                });

        assertTrue("Original PURL should exist since no replacement rule was set", hasOriginalPurl);

        log.info("Finished {} -->", methodName);
    }


    @Test
    public void TestOriginalPurlNotExistsWhenReplacementRuleDefined() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        String originalPurl = "pkg:github/scanoss/scanner.c";
        String replacementPurl = "pkg:maven/com.scanoss/scanoss";

        // Setup replace rule
        ReplaceRule replace = ReplaceRule.builder()
                .purl(originalPurl)
                .replaceWith(replacementPurl)
                .build();

        Bom bom = Bom.builder()
                .replace(Collections.singletonList(replace))
                .build();

        List<ScanFileResult> results = scannerPostProcessor.process(longScanResults, bom);

        assertNotNull("Results should not be null", results);
        assertFalse("Results should not be empty", results.isEmpty());

        boolean hasOriginalPurl = results.stream()
                .map(result -> result.getFileDetails().get(0).getPurls())
                .flatMap(Arrays::stream)
                .anyMatch(purl -> purl.equals(originalPurl));

        assertFalse("Original PURL should not exist when replacement rule is set", hasOriginalPurl);

        boolean hasReplacementPurl = results.stream()
                .map(result -> result.getFileDetails().get(0).getPurls())
                .flatMap(Arrays::stream)
                .anyMatch(purl -> purl.equals(replacementPurl));

        assertTrue("Replacement PURL should exist", hasReplacementPurl);

        log.info("Finished {} -->", methodName);
    }


}