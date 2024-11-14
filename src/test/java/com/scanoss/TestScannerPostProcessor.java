// SPDX-License-Identifier: MIT
/*
 * Copyright (c) 2023, SCANOSS
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
import com.scanoss.exceptions.ScannerPostProcessorException;
import com.scanoss.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import static com.scanoss.TestConstants.jsonResultsString;
import static org.junit.Assert.assertFalse;
import com.scanoss.dto.ScanFileResult;
import com.scanoss.settings.BomConfiguration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;

@Slf4j
public class TestScannerPostProcessor {
    private ScannerPostProcessor scannerPostProcessor;
    private BomConfiguration bomConfiguration;
    private List<ScanFileResult> sampleScanResults;

    @Before
    public void Setup() {
        log.info("Starting ScannerPostProcessor test cases...");
        scannerPostProcessor = new ScannerPostProcessor();
        setupBomConfiguration();
        setupSampleScanResults();
    }

    private void setupBomConfiguration() {
        bomConfiguration = new BomConfiguration();
        BomConfiguration.Bom bom = new BomConfiguration.Bom();
        bomConfiguration.setBom(bom);
    }

    private void setupSampleScanResults() {
        JsonObject jsonObject = JsonUtils.toJsonObject(jsonResultsString);
        sampleScanResults = JsonUtils.toScanFileResultsFromObject(jsonObject);
    }

    @Test
    public void TestNullParameters() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        try {
            scannerPostProcessor.process(null, bomConfiguration);
            fail("Should throw ScannerPostProcessorException when scan results is null");
        } catch (Exception e) {
            assertTrue("Wrong exception type thrown: " + e.getClass().getSimpleName(), e instanceof ScannerPostProcessorException);
        }

        try {
            scannerPostProcessor.process(sampleScanResults, null);
            fail("Should throw ScannerPostProcessorException when BOM configuration is null");
        } catch (Exception e) {
            assertTrue("Wrong exception type thrown: " + e.getClass().getSimpleName(), e instanceof ScannerPostProcessorException);
        }

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestRemoveRuleWithPathAndPurl() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        // Setup remove rule with both path and purl
        BomConfiguration.Component removeRule = new BomConfiguration.Component();
        removeRule.setPath("CMSsite/admin/js/npm.js");
        removeRule.setPurl("pkg:github/twbs/bootstrap");
        bomConfiguration.getBom().setRemove(Collections.singletonList(removeRule));

        // Process results
        List<ScanFileResult> results = scannerPostProcessor.process(sampleScanResults, bomConfiguration);

        // Verify
        assertEquals("Should have one result less after removal", sampleScanResults.size()-1, results.size());
        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestRemoveRuleWithPurlOnly() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        // Setup remove rule with only purl
        BomConfiguration.Component removeRule = new BomConfiguration.Component();
        removeRule.setPurl("pkg:npm/mip-bootstrap");
        bomConfiguration.getBom().setRemove(Collections.singletonList(removeRule));

        // Process results
        List<ScanFileResult> results = scannerPostProcessor.process(sampleScanResults, bomConfiguration);

        // Verify
        assertEquals("Size should decrease by 1 after removal",
                sampleScanResults.size()-1,
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
        BomConfiguration.Component removeRule = new BomConfiguration.Component();
        removeRule.setPath("non/existing/path.c");
        removeRule.setPurl("pkg:github/non-existing/lib@1.0.0");
        bomConfiguration.getBom().setRemove(Collections.singletonList(removeRule));

        // Process results
        List<ScanFileResult> results = scannerPostProcessor.process(sampleScanResults, bomConfiguration);

        // Verify
        assertEquals("Should keep all results", sampleScanResults.size(), results.size());

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestMultipleRemoveRules() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        // Setup multiple remove rules
        List<BomConfiguration.Component> removeRules = new ArrayList<>();

        BomConfiguration.Component rule1 = new BomConfiguration.Component();
        rule1.setPath("CMSsite/admin/js/npm.js");
        rule1.setPurl("pkg:npm/myoneui");

        BomConfiguration.Component rule2 = new BomConfiguration.Component();
        rule2.setPurl("pkg:pypi/scanoss");

        BomConfiguration.Component rule3 = new BomConfiguration.Component();
        rule3.setPath("scanoss/__init__.py");

        removeRules.add(rule1);
        removeRules.add(rule2);
        removeRules.add(rule3);
        bomConfiguration.getBom().setRemove(removeRules);

        // Process results
        List<ScanFileResult> results = scannerPostProcessor.process(sampleScanResults, bomConfiguration);

        // Verify
        assertTrue("Should remove all results", results.isEmpty());

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestEmptyRemoveRules() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        // Process results with empty remove rules
        List<ScanFileResult> results = scannerPostProcessor.process(sampleScanResults, bomConfiguration);

        // Verify
        assertEquals("Should keep all results", sampleScanResults.size(), results.size());
        assertEquals("Results should match original", sampleScanResults, results);

        log.info("Finished {} -->", methodName);
    }
}