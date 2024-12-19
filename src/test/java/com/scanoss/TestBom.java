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

import com.scanoss.settings.Bom;
import com.scanoss.settings.ReplaceRule;
import com.scanoss.settings.RuleComparator;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;


@Slf4j
public class TestBom {

    @Before
    public void Setup()  {
        log.info("Starting Bom test cases...");
    }

    @Test
    public void testReplaceRulesSortingAllCombinations() {
        log.info("<-- Starting testReplaceRulesSortingAllCombinations");

        // Create rules with different combinations of path and purl
        ReplaceRule bothFieldsLongPath = ReplaceRule.builder()
                .path("/very/long/path/to/specific/file.txt")
                .purl("pkg:maven/org.example/library@1.0.0")
                .build();

        ReplaceRule bothFieldsShortPath = ReplaceRule.builder()
                .path("/short/path")
                .purl("pkg:maven/org.example/another@1.0.0")
                .build();

        ReplaceRule purlOnly = ReplaceRule.builder()
                .purl("pkg:maven/org.example/another@2.0.0")
                .build();

        ReplaceRule pathOnly = ReplaceRule.builder()
                .path("/another/path")
                .build();

        // Create Bom with rules in random order
        Bom bom = Bom.builder()
                .replace(pathOnly)
                .replace(bothFieldsShortPath)
                .replace(purlOnly)
                .replace(bothFieldsLongPath)
                .build();

        // Get sorted rules
        List<ReplaceRule> sortedRules = bom.getReplaceRulesByPriority();

        // Verify order
        assertEquals("Rule with both fields and longer path should be first", bothFieldsLongPath, sortedRules.get(0));
        assertEquals("Rule with both fields and shorter path should be second", bothFieldsShortPath, sortedRules.get(1));
        assertEquals("Rule with purl only should be third", purlOnly, sortedRules.get(2));
        assertEquals("Rule with path only should be last", pathOnly, sortedRules.get(3));

        log.info("Finished testReplaceRulesSortingAllCombinations -->");
    }


    @Test
    public void testReplaceRulesSortingWithDuplicatePaths() {
        log.info("<-- Starting testReplaceRulesSortingWithDuplicatePaths");

        // Create rules with same path length but different paths
        ReplaceRule pathRule1 = ReplaceRule.builder()
                .path("/path/to/first")
                .build();

        ReplaceRule pathRule2 = ReplaceRule.builder()
                .path("/path/to/other")
                .build();

        // Create Bom with rules
        Bom bom = Bom.builder()
                .replace(pathRule1)
                .replace(pathRule2)
                .build();

        // Get sorted rules
        List<ReplaceRule> sortedRules = bom.getReplaceRulesByPriority();

        // Verify the rules with same path length maintain original order
        assertEquals("Size should be 2", 2, sortedRules.size());
        assertTrue("Both rules should have same priority",
                new RuleComparator().compare(sortedRules.get(0), sortedRules.get(1)) == 0);

        log.info("Finished testReplaceRulesSortingWithDuplicatePaths -->");
    }


    @Test
    public void testReplaceRulesSortingEmptyList() {
        log.info("<-- Starting testReplaceRulesSortingEmptyList");

        // Create Bom with no rules
        Bom bom = Bom.builder().build();

        // Get sorted rules
        List<ReplaceRule> sortedRules = bom.getReplaceRulesByPriority();

        // Verify
        assertNotNull("Sorted list should not be null", sortedRules);
        assertTrue("Sorted list should be empty", sortedRules.isEmpty());

        log.info("Finished testReplaceRulesSortingEmptyList -->");
    }
}