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

import com.scanoss.settings.FileSnippet;
import com.scanoss.settings.ScanossSettings;
import com.scanoss.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

@Slf4j
public class TestSettings {
    private Path existingSettingsPath;
    private Path nonExistentSettingsPath;

    @Before
    public void Setup() throws URISyntaxException {
        log.info("Starting Settings test cases...");
        log.debug("Logging debug enabled");
        log.trace("Logging trace enabled");

        // Check if resource exists before attempting to get its path
        var resource = getClass().getClassLoader().getResource("scanoss.json");
        if (resource == null) {
            throw new IllegalStateException(
                    "Required test resource 'scanoss.json' not found. Please ensure it exists in src/test/resources/data/"
            );
        }

        existingSettingsPath = Paths.get(resource.toURI());
        nonExistentSettingsPath = Paths.get("non-existent-settings.json");

        // Verify the file actually exists
        if (!Files.exists(existingSettingsPath)) {
            throw new IllegalStateException(
                    "Test file exists as resource but cannot be accessed at path: " +
                            existingSettingsPath
            );
        }
    }


    @Test
    public void testSettingsFromExistingFile() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);


        ScanossSettings settings = ScanossSettings.createFromPath(existingSettingsPath);
        assertNotNull("Settings should not be null", settings);

        assertEquals("scanner.c", settings.getBom().getRemove().get(0).getPath());
        assertEquals("pkg:github/scanoss/scanner.c", settings.getBom().getRemove().get(0).getPurl());



        log.info("Finished {} -->", methodName);
    }

    @Test
    public void testSettingsFromNonExistentFile() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        ScanossSettings settings = ScanossSettings.createFromPath(nonExistentSettingsPath);

        assertNull("Settings should be null", settings);

        log.info("Finished {} -->", methodName);
    }


    @Test
    public void testEmptySettingsInitialization() {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        // Test with completely empty JSON
        String emptyJson = "{}";
        ScanossSettings emptySettings = JsonUtils.fromJson(emptyJson, ScanossSettings.class);

        assertNotNull("Settings should not be null", emptySettings);
        assertNotNull("Bom should not be null", emptySettings.getBom());
        assertNotNull("Include list should not be null", emptySettings.getBom().getInclude());
        assertNotNull("Ignore list should not be null", emptySettings.getBom().getIgnore());
        assertNotNull("Remove list should not be null", emptySettings.getBom().getRemove());
        assertNotNull("Replace list should not be null", emptySettings.getBom().getReplace());
        assertNotNull("Settings should not be null", emptySettings.getSettings());

        assertTrue("Include list should be empty", emptySettings.getBom().getInclude().isEmpty());
        assertTrue("Ignore list should be empty", emptySettings.getBom().getIgnore().isEmpty());
        assertTrue("Remove list should be empty", emptySettings.getBom().getRemove().isEmpty());
        assertTrue("Replace list should be empty", emptySettings.getBom().getReplace().isEmpty());

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void testSkip() {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        ScanossSettings settings = ScanossSettings.createFromPath(existingSettingsPath);
        assertNotNull("Settings should not be null", settings);
        assertNotEquals(0, settings.getSettings().getSkip().getPatterns().getScanning().size());
        assertEquals((List.of(
                "*.log",
                "!important.log",
                "temp/",
                "debug[0-9]*.txt",
                "src/client/specific-file.js",
                "src/nested/folder/"
        )), settings.getSettings().getSkip().getPatterns().getScanning());

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void testScanConfigFromJson() {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        assertNotNull("Scan config settings path should exist", existingSettingsPath);
        ScanossSettings settings = ScanossSettings.createFromPath(existingSettingsPath);
        assertNotNull("Settings should not be null", settings);

        // Verify settings-level scan config fields
        assertEquals(Integer.valueOf(3), settings.getSettings().getFileSnippet().getMinSnippetHits());
        assertEquals(Integer.valueOf(10), settings.getSettings().getFileSnippet().getMinSnippetLines());
        assertEquals(Boolean.TRUE, settings.getSettings().getFileSnippet().getHonourFileExts());
        assertEquals(Boolean.TRUE, settings.getSettings().getFileSnippet().getRankingEnabled());
        assertEquals(Integer.valueOf(5), settings.getSettings().getFileSnippet().getRankingThreshold());
        assertEquals(Boolean.FALSE, settings.getSettings().getFileSnippet().getSkipHeaders());
        assertEquals(Integer.valueOf(0), settings.getSettings().getFileSnippet().getSkipHeadersLimit());

        // Verify file_snippet section
        assertNotNull("file_snippet should not be null", settings.getSettings().getFileSnippet());
        assertEquals(Integer.valueOf(3), settings.getSettings().getFileSnippet().getMinSnippetHits());
        assertEquals(Integer.valueOf(5), settings.getSettings().getFileSnippet().getRankingThreshold());

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void testScanConfigResolution() {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        // CLI config (lowest priority)
        FileSnippet cliConfig = FileSnippet.builder()
                .minSnippetHits(2)
                .minSnippetLines(5)
                .rankingEnabled(false)
                .rankingThreshold(3)
                .build();

        // File-snippet config (highest priority)
        FileSnippet fileSnippetConfig = FileSnippet.builder()
                .minSnippetHits(5)
                .rankingThreshold(8)
                .build();

        FileSnippet resolved = FileSnippet.resolve(cliConfig, fileSnippetConfig);

        // file_snippet has min_snippet_hits=5 (highest priority)
        assertEquals(Integer.valueOf(5), resolved.getMinSnippetHits());
        // settings has ranking_enabled=true (file_snippet doesn't override)
        assertEquals(Boolean.FALSE, resolved.getRankingEnabled());
        // file_snippet has ranking_threshold=8 (highest priority)
        assertEquals(Integer.valueOf(8), resolved.getRankingThreshold());
        // CLI has min_snippet_lines=5 (only CLI sets it)
        assertEquals(Integer.valueOf(5), resolved.getMinSnippetLines());

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void testScanConfigResolutionFromSettingsFile() {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        assertNotNull("Scan config settings path should exist", existingSettingsPath);
        ScanossSettings settings = ScanossSettings.createFromPath(existingSettingsPath);
        assertNotNull("Settings should not be null", settings);

        // CLI provides some values
        FileSnippet cliConfig = FileSnippet.builder()
                .minSnippetHits(2)
                .minSnippetLines(9)
                .rankingThreshold(4)
                .build();

        FileSnippet resolved = settings.getResolvedScanConfig(cliConfig);

        // file_snippet provides min_snippet_hits=3 (overrides cli: 2)
        assertEquals(Integer.valueOf(3), resolved.getMinSnippetHits());
        // file_snippet provides ranking_threshold=5 (overrides cli: 4)
        assertEquals(Integer.valueOf(5), resolved.getRankingThreshold());
        // file_snippet provides ranking_enabled=true (CLI doesn't set it)
        assertEquals(Boolean.TRUE, resolved.getRankingEnabled());
        // file_snippet provides min_snippet_lines=10 (overrides cli: 9)
        assertEquals(Integer.valueOf(10), resolved.getMinSnippetLines());
        // file_snippet provides honour_file_exts=true (CLI doesn't set it)
        assertEquals(Boolean.TRUE, resolved.getHonourFileExts());

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void testScanConfigUnsetValues() {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        // Default config - all values unset
        FileSnippet defaultConfig = FileSnippet.builder().build();

        assertFalse("min_snippet_hits should be unset", defaultConfig.isMinSnippetHitsSet());
        assertFalse("min_snippet_lines should be unset", defaultConfig.isMinSnippetLinesSet());
        assertFalse("honour_file_exts should be unset", defaultConfig.isHonourFileExtsSet());
        assertFalse("ranking_enabled should be unset", defaultConfig.isRankingEnabledSet());
        assertFalse("ranking_threshold should be unset", defaultConfig.isRankingThresholdSet());
        assertFalse("skip_headers_limit should be unset", defaultConfig.isSkipHeadersLimitSet());

        // Config with values set
        FileSnippet setConfig = FileSnippet.builder()
                .minSnippetHits(3)
                .minSnippetLines(10)
                .honourFileExts(true)
                .rankingEnabled(false)
                .rankingThreshold(5)
                .skipHeadersLimit(20)
                .build();

        assertTrue("min_snippet_hits should be set", setConfig.isMinSnippetHitsSet());
        assertTrue("min_snippet_lines should be set", setConfig.isMinSnippetLinesSet());
        assertTrue("honour_file_exts should be set", setConfig.isHonourFileExtsSet());
        assertTrue("ranking_enabled should be set", setConfig.isRankingEnabledSet());
        assertTrue("ranking_threshold should be set", setConfig.isRankingThresholdSet());
        assertTrue("skip_headers_limit should be set", setConfig.isSkipHeadersLimitSet());

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void testScanConfigResolutionWithNulls() {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        // Test resolution with null layers
        FileSnippet cliConfig = FileSnippet.builder()
                .minSnippetHits(2)
                .build();

        FileSnippet resolved = FileSnippet.resolve(cliConfig, null);
        assertEquals(Integer.valueOf(2), resolved.getMinSnippetHits());
        assertNull("ranking_enabled should remain unset", resolved.getRankingEnabled());

        // Test with all nulls
        FileSnippet allNullResolved = FileSnippet.resolve(null, null);
        assertFalse("All values should be unset", allNullResolved.isMinSnippetHitsSet());
        assertNull("ranking_enabled should be null", allNullResolved.getRankingEnabled());

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void testExistingSettingsWithoutScanConfig() {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        // Test that existing settings file without scan config fields still works
        ScanossSettings settings = ScanossSettings.builder().build();

        // Scan config fields should be null when not in JSON
        assertNull("file snippet should be null", settings.getSettings().getFileSnippet());


        // Resolution should still work with defaults
        FileSnippet cliConfig = FileSnippet.builder().minSnippetHits(2).build();
        FileSnippet resolved = settings.getResolvedScanConfig(cliConfig);
        assertEquals(Integer.valueOf(2), resolved.getMinSnippetHits());

        log.info("Finished {} -->", methodName);
    }
}
