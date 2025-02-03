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
package com.scanoss.cli;

import com.scanoss.Scanner;
import com.scanoss.exceptions.ScannerException;
import com.scanoss.exceptions.WinnowingException;
import picocli.CommandLine;

import java.io.File;
import java.util.List;

import static com.scanoss.ScanossConstants.DEFAULT_WORKER_THREADS;
import static com.scanoss.cli.CommandLine.printMsg;

/**
 * Fingerprint Command Line Processor Class
 * <p>
 * Produce fingerprints using the Winnowing algorithm
 * </p>
 */
@SuppressWarnings("CanBeFinal")
@CommandLine.Command(name = "wfp", aliases = {"fingerprint", "fp"}, description = "Fingerprint the given file/folder")
public class WfpCommandLine implements Runnable {
    @picocli.CommandLine.Spec
    picocli.CommandLine.Model.CommandSpec spec;

    @picocli.CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display help information")
    private boolean helpRequested = false;

    @picocli.CommandLine.Option(names = {"-S", "--skip-snippets"}, description = "Skip the generation of snippets")
    private boolean skipSnippets = false;

    @picocli.CommandLine.Option(names = "--all-extensions", description = "Fingerprint all file extensions")
    private boolean allExtensions = false;

    @picocli.CommandLine.Option(names = "--all-hidden", description = "Fingerprint all hidden files/folders")
    private boolean allHidden = false;

    @picocli.CommandLine.Option(names = "--all-folders", description = "Fingerprint all folders")
    private boolean allFolders = false;

    @picocli.CommandLine.Option(names = {"-T", "--threads"}, description = "Number of parallel threads to use")
    private int numThreads = DEFAULT_WORKER_THREADS;

    @picocli.CommandLine.Option(names = {"--snippet-limit"}, description = "Length of single line snippet limit (0 for unlimited, default 1000)")
    private int snippetLimit = 1000;

    @picocli.CommandLine.Parameters(arity = "1", description = "file/folder to fingerprint")
    private String fileFolder;

    private Scanner scanner;

    /**
     * Run the 'wfp' command
     */
    @Override
    public void run() {
        if (fileFolder == null || fileFolder.isEmpty()) {
            throw new RuntimeException("Error: No file or folder specified to scan");
        }
        File f = new File(fileFolder);
        if (!f.exists()) {
            throw new RuntimeException(String.format("Error: File or folder does not exist: %s\n", fileFolder));
        }
        if (com.scanoss.cli.CommandLine.debug) {
            var err = spec.commandLine().getErr();
            if (numThreads != DEFAULT_WORKER_THREADS) {
                printMsg(err, String.format("Running with %d threads.", numThreads));
            }
        }
        scanner = Scanner.builder().skipSnippets(skipSnippets).allFolders(allFolders).allExtensions(allExtensions)
                .hiddenFilesFolders(allHidden).numThreads(numThreads).snippetLimit(snippetLimit).build();
        if (f.isFile()) {
            wfpFile(fileFolder);
        } else if (f.isDirectory()) {
            wfpFolder(fileFolder);
        } else {
            throw new RuntimeException(String.format("Error: Specified path is not a file or a folder: %s\n", fileFolder));
        }
    }

    /**
     * Fingerprint the specified file and output the results
     *
     * @param file file to fingerprint
     */
    private void wfpFile(String file) {
        var out = spec.commandLine().getOut();
        var err = spec.commandLine().getErr();
        try {
            printMsg(err, String.format("Fingerprinting %s...", file));
            String result = scanner.wfpFile(file);
            if (result != null && !result.isEmpty()) {
                out.println(result);
                return;
            } else {
                err.println("Warning: No WFP returned.");
            }
        } catch (ScannerException | WinnowingException e) {
            if (com.scanoss.cli.CommandLine.debug) {
                e.printStackTrace(err);
            }
            throw e;
        }
        throw new RuntimeException(String.format("Something went wrong while fingerprinting %s", file));
    }

    /**
     * Fingerprint the specified folder and output the results
     *
     * @param folder folder to fingerprint
     */
    private void wfpFolder(String folder) {
        var out = spec.commandLine().getOut();
        var err = spec.commandLine().getErr();
        try {
            printMsg(err, String.format("Fingerprinting %s...", folder));
            List<String> results = scanner.wfpFolder(folder);
            if (results != null && !results.isEmpty()) {
                printMsg(err, String.format("Found %d files.", results.size()));
                results.forEach(out::print);
                out.flush();
                return;
            } else {
                err.println("Error: No results return.");
            }
        } catch (ScannerException | WinnowingException e) {
            if (com.scanoss.cli.CommandLine.debug) {
                e.printStackTrace(err);
            }
            throw e;
        }
        throw new RuntimeException(String.format("Something went wrong while fingerprinting %s", folder));
    }
}
