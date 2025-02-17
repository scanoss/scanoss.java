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

import com.scanoss.exceptions.ScannerException;
import com.scanoss.filters.FilterConfig;
import com.scanoss.settings.ScanossSettings;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@Slf4j
public class TestScanner {
    @Before
    public void Setup() {
        log.info("Starting Scanner test cases...");
        log.debug("Logging debug enabled");
        log.trace("Logging trace enabled");
    }

    @Test
    public void TestScannerPositive() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        Scanner scanner = Scanner.builder().build();
        Winnowing winnowing = scanner.getWinnowing();
        assertNotNull("Winnowing should be enabled", winnowing);
        assertFalse("Snippets should be enabled", winnowing.getSkipSnippets());

        Winnowing winnowing1 = Winnowing.builder().skipSnippets(true).build();
        Scanner scanner1 = Scanner.builder().winnowing(winnowing1).build();
        assertTrue("Winnowing skip snippets should be true", scanner1.getWinnowing().getSkipSnippets());

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestScannerWfpFilePositive() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        Scanner scanner = Scanner.builder().build();

        String file = "src/test/java/com/scanoss/TestScanner.java";
        String wfp = scanner.wfpFile(file);
        assertNotNull("Should've gotten a WFP", wfp);
        assertFalse("WFP should not be empty", wfp.isEmpty());
        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestScannerWfpFileNegative() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        Scanner scanner = Scanner.builder().build();
        try {
            String file = "";
            String wfp = scanner.wfpFile(file);
            assertNull("Should not have gotten a WFP", wfp);
        } catch (ScannerException e) {
            log.info("Got expected error: {}", e.getLocalizedMessage());
        }
        try {
            String file = "testing/data/does-not-exist.java";
            String wfp = scanner.wfpFile(file);
            assertNull("Should not have gotten a WFP", wfp);
        } catch (ScannerException e) {
            log.info("Got expected error: {}", e.getLocalizedMessage());
        }
        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestScannerWfpFolderPositive() throws IOException {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        Scanner scanner = Scanner.builder().build();

        String folder = ".";
        List<String> wfps = scanner.wfpFolder(folder);
        assertNotNull("Should've gotten a WFP", wfps);
        assertFalse("WFP should not be empty", wfps.isEmpty());
        log.info("Retrieved {} WFPs from {}", wfps.size(), folder);
        FileWriter f = new FileWriter("tmp/test-root-fingers.wfp");
        f.write(String.join("", wfps));
        f.close();

        scanner = Scanner.builder().allExtensions(true).build();
        folder = "testing";
        wfps = scanner.wfpFolder(folder);
        assertNotNull("Should've gotten a WFP", wfps);
        assertFalse("WFP should not be empty", wfps.isEmpty());
        log.info("Retrieved {} WFPs from {}", wfps.size(), folder);
        f = new FileWriter("tmp/test-testing-fingers.wfp");
        f.write(String.join("", wfps));
        f.close();

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestScannerWfpFolderNegative() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        Scanner scanner = Scanner.builder().build();
        try {
            String folder = "";
            List<String> wfps = scanner.wfpFolder(folder);
            assertNull("Should not have gotten a WFP", wfps);
        } catch (ScannerException e) {
            log.info("Got expected error: {}", e.getLocalizedMessage());
        }
        try {
            String folder = "testing/does-no-exist";
            List<String> wfps = scanner.wfpFolder(folder);
            assertNull("Should not have gotten a WFP", wfps);
        } catch (ScannerException e) {
            log.info("Got expected error: {}", e.getLocalizedMessage());
        }
        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestScannerScanFilePositive() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        Scanner scanner = Scanner.builder().build();

        String file = "src/test/java/com/scanoss/TestScanner.java";
        String result = scanner.scanFile(file);
        assertNotNull("Should've gotten a response", result);
        assertFalse("Scan results should not be empty", result.isEmpty());
        log.info("Single Scan result: {}", result);

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestScannerScanFolderPositive() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        Scanner scanner = Scanner.builder().build();
        String folder = "src/test";
        List<String> results = scanner.scanFolder(folder);
        assertNotNull("Should've gotten a response", results);
        assertFalse("Scan results should not be empty", results.isEmpty());
        log.info("Received {} results", results.size());

        log.info("Res Data: {}", results);

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestScannerScanFileListPositive() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        Scanner scanner = Scanner.builder().build();

        List<String> fileList = Arrays.asList(
                "src/test/java/com/scanoss/TestScanner.java",
                "src/test/java/com/scanoss/TestWinnowing.java",
                ".github/workflows/publish.yml",
                ".gitignore",
                "tmp/.gitignore"
        );
        String folder = ".";
        List<String> results = scanner.scanFileList(folder, fileList);

        assertNotNull("Should've gotten a response", results);
        assertFalse("Scan results should not be empty", results.isEmpty());
        assertEquals("Should've only gotten two results",2, results.size());
        log.info("Received {} results", results.size());
        log.info("Res Data: {}", results);

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestScannerScanFileListNegative() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        Scanner scanner = Scanner.builder().build();
        try {
            String folder = "testing/does-no-exist";
            List<String> results = scanner.scanFileList(folder, new ArrayList<>());
            assertNull("Should not have gotten a result", results);
        } catch (ScannerException e) {
            log.info("Got expected error: {}", e.getLocalizedMessage());
        }
        try {
            String folder = ".";
            List<String> results = scanner.scanFileList(folder, new ArrayList<>());
            assertNull("Should not have gotten a result", results);
        } catch (ScannerException e) {
            log.info("Got expected error: {}", e.getLocalizedMessage());
        }

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestScannerTemplate() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestIgnoreFolderExtension() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        Scanner scanner = Scanner.builder().build();
        String folder = "testing/data/test-folder-ignore";
        List<String> results = scanner.scanFolder(folder);
        log.info("Received {} results", results.size());
        assertFalse("Scan results should be empty", results.isEmpty());
        assertEquals("Results should be one", 1, results.size());

        log.info("Finished {} -->", methodName);
    }



    @Test
    public void TestScannerFiltering() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        String f;

        log.info("Testing filtering: folder ends with nbproject should NOT be filtered... ");
        f = "testing/data/folder-ends-with-nbproject";
        Scanner scanner = Scanner.builder().build();
        List<String> wfps = scanner.wfpFolder(f);
        assertTrue("WFP should NOT be empty", !wfps.isEmpty());

        log.info("Testing filtering: file nbproject should not be filtered... ");
        f = "testing/data";
        wfps = scanner.wfpFolder(f);
        boolean wasFilenbprojectFingerprinted = wfps.stream().anyMatch(w -> w.contains(",nbproject\n"));
        assertTrue("nbproject file should be fingerprinted", wasFilenbprojectFingerprinted);

        log.info("Testing filtering: file scanner.build should be filtered... ");
        f = "testing/data";
        wfps = scanner.wfpFolder(f);
        boolean wasFileScannerBuildFingerprinted = wfps.stream().anyMatch(w -> w.contains(",scanner.build\n"));
        assertFalse("scanner.build file should not be fingerprinted", wasFileScannerBuildFingerprinted);

        log.info("Testing filtering: folder folder.build should not be filtered... ");
        f = "testing/data";
        wfps = scanner.wfpFolder(f);
        boolean wasFolderScannerBuildFingerprinted = wfps.stream().anyMatch(w -> w.contains(",scanner.build\n"));
        assertFalse("scanner.build file should not be fingerprinted", wasFolderScannerBuildFingerprinted);


        log.info("Finished {} -->", methodName);
    }



    @Test
    public void TestScannerSkipGitIgnore() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        String f;
        List<String> wfps;

        log.info("Testing GitIgnore Pattern:  ");
        f = "testing/data/folder-ends-with-nbproject";

        ScanossSettings.Patterns patterns = ScanossSettings.Patterns.builder().scanning(List.of("*nbproject")).build();
        ScanossSettings.Skip skip = ScanossSettings.Skip.builder().patterns(patterns).build();
        ScanossSettings.Settings settings = ScanossSettings.Settings.builder().skip(skip).build();
        ScanossSettings scanossSettings = ScanossSettings.builder().settings(settings).build();

        Scanner scanner = Scanner.builder().settings(scanossSettings).build();
        wfps = scanner.wfpFolder(f);
        assertTrue("Folder should be skipped by gitignore", wfps.isEmpty());

        log.info("Finished {} -->", methodName);
    }


    @Test
    public void TestScannerSkipAntPattern() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        String f;
        List<String> wfps;

        log.info("Testing GitIgnore Pattern:  ");
        f = "testing/data/folder-ends-with-nbproject";

        FilterConfig filterConfig = FilterConfig.builder()
                .allFolders(false)
                .hiddenFilesFolders(false)
                .allFilesFolders(false)
                .antPatterns(List.of("**/*nbproject/")).build();

        Scanner scanner = Scanner.builder().filterConfig(filterConfig).build();
        wfps = scanner.wfpFolder(f);
        assertTrue("Folder should be skipped by Ant Patterns", wfps.isEmpty());

        log.info("Finished {} -->", methodName);
    }
}
