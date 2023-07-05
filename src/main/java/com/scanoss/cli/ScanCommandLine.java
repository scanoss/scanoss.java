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
import com.scanoss.exceptions.ScannerException;
import com.scanoss.exceptions.WinnowingException;
import com.scanoss.utils.JsonUtils;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static com.scanoss.cli.CommandLine.printDebug;
import static com.scanoss.cli.CommandLine.printMsg;

/**
 * Scan Command Line Processor Class
 * <p>
 * Provide a Scan CLI subcommand to interacting with the SCANOSS Java SDK
 * </p>
 */
@SuppressWarnings({"unused", "CanBeFinal"})
@picocli.CommandLine.Command(name = "scan", description = "Scan the given file/folder/wfp")
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

    @picocli.CommandLine.Option(names = {"-T", "--threads"}, description = "Number of parallel threads to use")
    private int numThreads = 5;

    @picocli.CommandLine.Option(names = "--apiurl", description = "SCANOSS API URL (optional - default: https://osskb.org/api/scan/direct)")
    private String apiUrl;

    @picocli.CommandLine.Option(names = {"-k", "--key"}, description = "SCANOSS API Key token (optional - not required for default OSSKB URL)")
    private String apiKey;

    @picocli.CommandLine.Option(names = {"-R", "--retry"}, description = "Retry limit for API communication (optional - default 5)")
    private int retryLimit = 5;

    @picocli.CommandLine.Option(names = {"-M", "--timeout"}, description = "Timeout (in seconds) for API communication (optional - default 120)")
    private int timeoutLimit = 5;

    @picocli.CommandLine.Option(names = {"-F", "--flags"}, description = "Scanning engine flags (1: disable snippet matching, 2 enable snippet ids, 4: disable dependencies, 8: disable licenses, 16: disable copyrights, 32: disable vulnerabilities, 64: disable quality, 128: disable cryptography,256: disable best match only, 512: hide identified files, 1024: enable download_url, 2048: enable GitHub full path, 4096: disable extended server stats")
    private String scanFlags;

    @picocli.CommandLine.Option(names = {"-i", "--identify"}, description = "Scan and identify components in SBOM file")
    private String identifySbom;

    @picocli.CommandLine.Option(names = {"-n", "--ignore"}, description = "Ignore components specified in the SBOM file")
    private String ignoreSbom;

    @picocli.CommandLine.Parameters(arity = "1", description = "file/folder to scan")
    private String fileFolder;

    private Scanner scanner;

    /**
     * Run the 'scan' command
     */
    @Override
    public void run() {
        var err = spec.commandLine().getErr();
        if (fileFolder == null || fileFolder.isEmpty()) {
            throw new RuntimeException("Error: No file or folder specified to scan");
        }
        if (identifySbom != null && ignoreSbom != null) {
            throw new RuntimeException("Error: Specify one of --identify or --ignore not both");
        }
        String sbomType = null;
        String sbom = null;
        if (identifySbom != null && !identifySbom.isEmpty()) {
            sbomType = "identify";
            sbom = loadFileToString(identifySbom);
        } else if (ignoreSbom != null && !ignoreSbom.isEmpty()) {
            sbomType = "ignore";
            sbom = loadFileToString(ignoreSbom);
        }
        if (com.scanoss.cli.CommandLine.debug) {
            if (numThreads != 5) {
                printMsg(err, String.format("Running with %d threads.", numThreads));
            }
            if (timeoutLimit != 120) {
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
                .retryLimit(retryLimit).timeout(timeoutLimit).scanFlags(scanFlags)
                .sbomType(sbomType).sbom(sbom)
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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
