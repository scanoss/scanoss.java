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
package com.scanoss.cli;

import com.scanoss.Scanner;
import com.scanoss.ScannerPostProcessor;
import com.scanoss.dto.ScanFileResult;
import com.scanoss.exceptions.ScannerException;
import com.scanoss.exceptions.WinnowingException;
import com.scanoss.settings.Settings;
import com.scanoss.utils.JsonUtils;
import com.scanoss.utils.ProxyUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import static com.scanoss.ScanossConstants.*;
import static com.scanoss.cli.CommandLine.printDebug;
import static com.scanoss.cli.CommandLine.printMsg;
import static com.scanoss.utils.JsonUtils.toScanFileResultJsonObject;

/**
 * Scan Command Line Processor Class
 * <p>
 * Provide a Scan CLI subcommand to interacting with the SCANOSS Java SDK
 * </p>
 */
@SuppressWarnings({"unused", "CanBeFinal"})
@picocli.CommandLine.Command(name = "scan", description = "Scan the given file/folder/wfp")
@Slf4j
class ScanCommandLine implements Runnable {
    @picocli.CommandLine.Spec
    picocli.CommandLine.Model.CommandSpec spec;

    @picocli.CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display help information")
    private boolean helpRequested = false;

    @picocli.CommandLine.Option(names = {"-S", "--skip-snippets"}, description = "Skip the generation of snippets")
    private boolean skipSnippets = false;

    @picocli.CommandLine.Option(names = "--all-extensions", description = "Scan all file extensions")
    private boolean allExtensions = false;

    @picocli.CommandLine.Option(names = "--all-hidden", description = "Scan all hidden files/folders")
    private boolean allHidden = false;

    @picocli.CommandLine.Option(names = "--all-folders", description = "Scan all folders")
    private boolean allFolders = false;

    @picocli.CommandLine.Option(names = {"-T", "--threads"}, description = "Number of parallel threads to use (optional - default " + DEFAULT_WORKER_THREADS + ")")
    private int numThreads = DEFAULT_WORKER_THREADS;

    @picocli.CommandLine.Option(names = "--apiurl", description = "SCANOSS API URL (optional - default: https://api.osskb.org/scan/direct)")
    private String apiUrl;

    @picocli.CommandLine.Option(names = {"-k", "--key"}, description = "SCANOSS API Key token (optional - not required for default OSSKB URL)")
    private String apiKey;

    @picocli.CommandLine.Option(names = {"-R", "--retry"}, description = "Retry limit for API communication (optional - default " + DEFAULT_HTTP_RETRY_LIMIT + ")")
    private int retryLimit = DEFAULT_HTTP_RETRY_LIMIT;

    @picocli.CommandLine.Option(names = {"-M", "--timeout"}, description = "Timeout (in seconds) for API communication (optional - default " + DEFAULT_TIMEOUT + ")")
    private int timeoutLimit = DEFAULT_TIMEOUT;

    @picocli.CommandLine.Option(names = {"-F", "--flags"}, description = "Scanning engine flags (1: disable snippet matching, 2 enable snippet ids, 4: disable dependencies, 8: disable licenses, 16: disable copyrights, 32: disable vulnerabilities, 64: disable quality, 128: disable cryptography,256: disable best match only, 512: hide identified files, 1024: enable download_url, 2048: enable GitHub full path, 4096: disable extended server stats")
    private String scanFlags;

    @picocli.CommandLine.Option(names = {"--settings"}, description = "Settings file to use for scanning (optional - default scanoss.json)")
    private String settingsPath;

    @picocli.CommandLine.Option(names = {"--snippet-limit"}, description = "Length of single line snippet limit (0 for unlimited, default 1000)")
    private int snippetLimit = 1000;

    @picocli.CommandLine.Option(names = {"--ca-cert"}, description = "Alternative certificate PEM file (optional)")
    private String caCert;

    @picocli.CommandLine.Option(names = {"--proxy"}, description = "HTTP Proxy URL (optional)")
    private String proxyString;

    @picocli.CommandLine.Option(names = {"-H", "--hpsm"}, description = "Use High Precision Snippet Matching algorithm")
    private boolean enableHpsm = false;

    @picocli.CommandLine.Parameters(arity = "1", description = "file/folder to scan")
    private String fileFolder;

    private Scanner scanner;

    private Settings settings;
    /**
     * Run the 'scan' command
     */
    @Override
    public void run() {
        var err = spec.commandLine().getErr();
        if (fileFolder == null || fileFolder.isEmpty()) {
            throw new RuntimeException("Error: No file or folder specified to scan");
        }

        //TODO: Deprecate options
        String sbomType = null;
        String sbom = null;

        String caCertPem = null;
        if (caCert != null && !caCert.isEmpty()) {
            caCertPem = loadFileToString(caCert);
        }
        Proxy proxy = null;
        if (proxyString != null && !proxyString.isEmpty()) {
            proxy = ProxyUtils.createProxyFromString(proxyString);
            if (proxy == null) {
                throw new RuntimeException("Error: Failed to setup proxy config");
            }
        }

        if(settingsPath != null && !settingsPath.isEmpty()) {
            settings = Settings.createFromPath(Paths.get(settingsPath));
            if (settings == null) throw new RuntimeException("Error: Failed to read settings file");
        }
        log.info("Settings file read: {}", settings);

        if (com.scanoss.cli.CommandLine.debug) {
            if (numThreads != DEFAULT_WORKER_THREADS) {
                printMsg(err, String.format("Running with %d threads.", numThreads));
            }
            if (timeoutLimit != DEFAULT_TIMEOUT) {
                printMsg(err, String.format("Scanning with timeout of %d seconds", timeoutLimit));
            }
            if (skipSnippets) {
                printDebug(err, "Skipping snippets.");
            }
            if (allExtensions) {
                printMsg(err, "Scanning all file extensions/types.");
            }
            if (allHidden) {
                printDebug(err, "Scanning all hidden files/folders.");
            }
            if (scanFlags != null && !scanFlags.isEmpty()) {
                printMsg(err, String.format("Using flags %s", scanFlags));
            }
        }
        scanner = Scanner.builder().skipSnippets(skipSnippets).allFolders(allFolders).allExtensions(allExtensions)
                .hiddenFilesFolders(allHidden).numThreads(numThreads).url(apiUrl).apiKey(apiKey)
                .retryLimit(retryLimit).timeout(Duration.ofSeconds(timeoutLimit)).scanFlags(scanFlags)
                .sbomType(sbomType).sbom(sbom).snippetLimit(snippetLimit).customCert(caCertPem).proxy(proxy).hpsm(enableHpsm)
                .settings(settings)
                .build();

        File f = new File(fileFolder);
        if (!f.exists()) {
            throw new RuntimeException(String.format("Error: File or folder does not exist: %s\n", fileFolder));
        }
        if (f.isFile()) {
            scanFile(fileFolder);
        } else if (f.isDirectory()) {
            scanFolder(fileFolder);
        } else {
            throw new RuntimeException(String.format("Error: Specified path is not a file or a folder: %s\n", fileFolder));
        }
    }

    /**
     * Load the specified file into a string
     *
     * @param filename filename to load
     * @return loaded string
     */
    private String loadFileToString(@NonNull String filename) {
        File file = new File(filename);
        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException(String.format("File does not exist or is not a file: %s", filename));
        }
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Scan the specified file and output the results
     *
     * @param file file to scan
     */
    private void scanFile(String file) {
        var out = spec.commandLine().getOut();
        var err = spec.commandLine().getErr();
        try {
            printMsg(err, String.format("Scanning %s...", file));
            String result = scanner.scanFile(file);
            if (result != null && !result.isEmpty()) {
                JsonUtils.writeJsonPretty(JsonUtils.toJsonObject(result), out);
                return;
            } else {
                err.println("Warning: No results returned.");
            }
        } catch (ScannerException | WinnowingException e) {
            if (CommandLine.debug) {
                e.printStackTrace(err);
            }
            throw e;
        }
        throw new RuntimeException(String.format("Something went wrong while scanning %s", file));
    }
    /**
     * Scan the specified folder/directory and return the results
     *
     * @param folder folder to scan
     */
    private void scanFolder(String folder) {
        var out = spec.commandLine().getOut();
        var err = spec.commandLine().getErr();
        try {
            printMsg(err, String.format("Scanning %s...", folder));
            List<String> results = scanner.scanFolder(folder);
            if (results != null && !results.isEmpty()) {
                printMsg(err, String.format("Found %d results.", results.size()));
                printDebug(err, "Converting to JSON...");
                JsonUtils.writeJsonPretty(JsonUtils.joinJsonObjects(JsonUtils.toJsonObjects(results)), out);
                return;
            } else {
                err.println("Error: No results return.");
            }
        } catch (ScannerException | WinnowingException e) {
            if (CommandLine.debug) {
                e.printStackTrace(err);
            }
            throw e;
        }
        throw new RuntimeException(String.format("Something went wrong while scanning %s", folder));
    }
}
