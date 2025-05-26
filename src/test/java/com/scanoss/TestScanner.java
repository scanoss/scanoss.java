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

import com.google.gson.Gson;
import com.scanoss.dto.ScanFileDetails;
import com.scanoss.dto.ScanFileResult;
import com.scanoss.dto.ServerDetails;
import com.scanoss.dto.enums.MatchType;
import com.scanoss.exceptions.ScannerException;
import com.scanoss.filters.FilterConfig;
import com.scanoss.settings.ScanossSettings;
import com.scanoss.utils.JsonUtils;
import com.scanoss.utils.WinnowingUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@Slf4j
public class TestScanner {
    private MockWebServer server;


    @Before
    public void Setup() throws IOException{
        log.info("Starting Scanner test cases...");
        log.debug("Logging debug enabled");
        log.trace("Logging trace enabled");
        log.info("Starting Mock Server...");
        server = new MockWebServer();
        server.start();
    }

    @After
    public void Finish() {
        log.info("Shutting down mock server.");
        try {
            server.close();
            server.shutdown();
        } catch (IOException e) {
            log.warn("Some issue shutting down mock server: {}", e.getLocalizedMessage());
        }
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
    public void TestScannerScanFileListPositiveWithObfuscation() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        Scanner scanner = Scanner.builder().obfuscate(true).build();

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
        assertFalse("WFP should NOT be empty", wfps.isEmpty());

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

    @Test
    public void TestScannerCustomFilterConfig() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        String f;
        List<String> wfps;

        log.info("Testing GitIgnore Pattern:  ");
        f = "testing/data/folder-ends-with-nbproject";

        FilterConfig filterConfig = FilterConfig.builder()
                .customFilter(p -> true)
                .build();

        Scanner scanner = Scanner.builder().filterConfig(filterConfig).build();
        wfps = scanner.wfpFolder(f);
        assertTrue("There is no fingerprint since custom filter it's always true", wfps.isEmpty());

        log.info("Finished {} -->", methodName);
    }

    /**
     * Collects all files from the specified directory, returning their paths relative to the provided directory.
     *
     * @param directory the directory to scan for source files
     * @return a list of paths relative to the specified directory
     * @throws IOException if there's an error accessing the file system
     */
    private List<String> collectFilePaths(String directory) throws IOException {
        Path dirPath = Paths.get(directory);
        return Files.walk(dirPath)
                .filter(Files::isRegularFile)
                .map(path -> dirPath.relativize(path).toString())
                .collect(Collectors.toList());
    }

    /**
     * Helper method to create a mock server dispatcher that returns "no match" results
     * for all scan requests while tracking received paths for verification.
     *
     * @param receivedPaths Set that will be populated with paths extracted from the WFP block in requests
     * @return Dispatcher that returns "no match" results for all files
     */
    private Dispatcher createNoMatchDispatcher(Set<String> receivedPaths) {
        return new Dispatcher() {
            @NonNull
            @Override
            public MockResponse dispatch(@NonNull RecordedRequest request) {
                // Extract the WFP from the request and parse all obfuscated paths
                String requestBody = request.getBody().readUtf8();
                Set<String> paths = WinnowingUtils.extractFilePathsFromWFPBlock(requestBody);

                // Store all received paths for later verification
                receivedPaths.addAll(paths);

                for (String path : paths) {
                    log.debug("Server received obfuscated path: {}", path);
                }

                if (paths.isEmpty()) {
                    return new MockResponse()
                            .setResponseCode(400)
                            .setBody("error: Bad Request - No valid obfuscated paths found");
                }

                // Create response objects using the DTO classes
                Map<String, List<ScanFileDetails>> responseMap = new HashMap<>();

                // Create server details object (same for all responses)
                ServerDetails.KbVersion kbVersion = new ServerDetails.KbVersion("25.05", "21.05.21");
                ServerDetails serverDetails = new ServerDetails("5.4.10", kbVersion);

                // Create a "none" match result for each path
                for (String path : paths) {
                    ScanFileDetails noMatchResult = ScanFileDetails.builder()
                            .matchType(MatchType.none)
                            .serverDetails(serverDetails)
                            .build();

                    responseMap.put(path, Collections.singletonList(noMatchResult));
                }

                // Convert to JSON
                Gson gson = new Gson();
                String responseJson = gson.toJson(responseMap);

                return new MockResponse()
                        .setResponseCode(200)
                        .setBody(responseJson);
            }
        };
    }

    /**
     * Test that we can scan a file with obfuscation enabled using a mock server.
     * This test focuses on the path obfuscation/deobfuscation cycle
     */
    @Test
    public void testScanFileWithObfuscation() {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        final String fileToScan = "src/test/java/com/scanoss/TestScanner.java";

        // Set to capture the path received by the server
        final Set<String> receivedPaths = ConcurrentHashMap.newKeySet();

        // Configure the MockWebServer to return a 'no match' response
        server.setDispatcher(createNoMatchDispatcher(receivedPaths));

        Scanner scanner = Scanner.builder()
                .obfuscate(true)
                .url(server.url("/api/scan/direct").toString())
                .build();

        String result = scanner.scanFile(fileToScan);

        // Verify we got scan results
        assertNotNull("Should have scan results", result);
        assertFalse("Should have non-empty result", result.isEmpty());
        log.info("Received scan result for file");

        // Verify path received by the server is obfuscated (not matching the source file path)
        assertFalse("Received paths should not be empty", receivedPaths.isEmpty());
        String receivedPath = receivedPaths.iterator().next();
        assertNotEquals("Path should be obfuscated", fileToScan, receivedPath);

        // Verify (deobfuscation) that the result has the correct file path
        List<ScanFileResult> resultsDto = JsonUtils.toScanFileResults(Collections.singletonList(result));
        assertFalse("Results should not be empty", resultsDto.isEmpty());

        String resultPath = resultsDto.get(0).getFilePath();
        assertEquals("resultPath should be equal to the original file path", fileToScan, resultPath);

        log.info("Finished {} -->", methodName);
    }

    /**
     * Test that we can scan a list of files with obfuscation enabled using a mock server.
     * This test focuses on the path obfuscation/deobfuscation cycle
     */
    @Test
    public void testScanFileListWithObfuscation() throws IOException {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        String testDir = "src/test/java/com/scanoss";

        List<String> allFiles = collectFilePaths(testDir);
        log.info("Found {} files in source directory", allFiles.size());

        // Set to capture paths received by the server
        final Set<String> receivedPaths = ConcurrentHashMap.newKeySet();

        // Configure the MockWebServer to return a 'no match' response
        server.setDispatcher(createNoMatchDispatcher(receivedPaths));

        Scanner scanner = Scanner.builder()
                .obfuscate(true)
                .url(server.url("/api/scan/direct").toString())
                .build();

        List<String> results = scanner.scanFileList(testDir, allFiles);

        // Verify we got scan results
        assertNotNull("Should have scan results", results);
        assertFalse("Should have non-empty results", results.isEmpty());
        log.info("Received {} scan results", results.size());

        // Verify paths received by the server are obfuscated (not matching any source file paths)
        assertFalse("Received paths should not be empty", receivedPaths.isEmpty());
        receivedPaths.forEach(receivedPath ->
                assertFalse("Path should be obfuscated", allFiles.contains(receivedPath)));

        // Verify all original paths are in the results (deobfuscation check)
        List<ScanFileResult> resultsDto = JsonUtils.toScanFileResults(results);
        resultsDto.forEach(r ->
                assertTrue("Result should contain the original file path: " + r.getFilePath(),
                        allFiles.contains(r.getFilePath())));

        log.info("Finished {} -->", methodName);
    }

    /**
     * Test that we can scan a folder with obfuscation enabled using a mock server.
     * This test focuses on the path obfuscation/deobfuscation cycle
     */
    @Test
    public void testScanWithObfuscationCycle() throws IOException {
        final String folderToScan = "src/test";

        // Set to capture all paths received by the server
        final Set<String> receivedPaths = ConcurrentHashMap.newKeySet();

        // Collect all files in the src/test folder before scanning
        List<String> allFiles = collectFilePaths(folderToScan);
        log.info("Found {} files in source directory", allFiles.size());

        // Configure the MockWebServer to return a 'no match' response
        server.setDispatcher(createNoMatchDispatcher(receivedPaths));

        Scanner scanner = Scanner.builder()
                .obfuscate(true)
                .url(server.url("/api/scan/direct").toString()) // Use our mock server
                .build();

        // Scan the files to test the full obfuscation/deobfuscation cycle
        List<String> results = scanner.scanFolder(folderToScan);

        // Verify we got scan results
        assertNotNull("Should have scan results", results);
        assertFalse("Should have result non empty", results.isEmpty());
        log.info("Received {} scan results", results.size());

        // Verify paths received by the server are obfuscated (not matching any source file paths)
        receivedPaths.forEach(receivedPath ->
                assertFalse("Path should be obfuscated: " + receivedPath, allFiles.contains(receivedPath)));

        List<ScanFileResult> resultsDto = JsonUtils.toScanFileResults(results);
        // Verify (deobfuscation) that all results from scanFolder are valid file paths from our source directory
        resultsDto.forEach(r ->
                assertTrue("Result should be a valid source file path: " + r.getFilePath(),
                        allFiles.contains(r.getFilePath())));
    }
}
