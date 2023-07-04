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

import java.io.File;
import java.util.List;

import static com.scanoss.cli.CommandLine.printDebug;
import static com.scanoss.cli.CommandLine.printMsg;

/**
 * Scan Command Line Processor Class
 * <p>
 * Provide a Scan CLI subcommand to interacting with the SCANOSS Java SDK
 * </p>
 */
@picocli.CommandLine.Command(name = "scan")
class ScanCommandLine implements Runnable {
    @picocli.CommandLine.ParentCommand
    CommandLine parent;
    @picocli.CommandLine.Spec
    picocli.CommandLine.Model.CommandSpec spec;
    @picocli.CommandLine.Parameters(arity = "1", description = "file/folder to scan")
    public String fileFolder;

    /**
     * Run the 'scan' command
     */
    @Override
    public void run() {
        var err = spec.commandLine().getErr();
        if (parent.debug) {
            printDebug(err, "Debug enabled.");
        }
        if (fileFolder == null || fileFolder.isEmpty()) {
            throw new RuntimeException("Error: No file or folder specified to scan");
        }
        File f = new File(fileFolder);
        if (!f.exists()) {
            throw new RuntimeException( String.format("Error: File or folder does not exist: %s\n", fileFolder));
        }
        if (f.isFile()) {
            scanFile(fileFolder);
        } else if (f.isDirectory()) {
            scanFolder(fileFolder);
        } else {
            throw new RuntimeException( String.format("Error: Specified path is not a file or a folder: %s\n", fileFolder));
        }
    }

    /**
     * Scan the specified file and return the results
     *
     * @param file file to scan
     */
    private void scanFile(String file) {
        var out = spec.commandLine().getOut();
        var err = spec.commandLine().getErr();
        Scanner scanner = Scanner.builder().build();
        try {
            printMsg(err, String.format("Scanning %s...", file));
            String result = scanner.scanFile(file);
            if (result != null || !result.isEmpty()) {
                JsonUtils.writeJsonPretty(JsonUtils.toJsonObject(result), out);
                return;
            } else {
                err.println("Warning: No results returned.");
            }
        } catch (ScannerException | WinnowingException e) {
            if (parent.debug) {
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
        Scanner scanner = Scanner.builder().build();
        try {
            printMsg(err, String.format("Scanning %s...", folder));
            List<String> results = scanner.scanFolder(folder);
            if (results != null || !results.isEmpty()) {
                printMsg(err, String.format("Found %d results.", results.size()));
                printDebug(err, "Converting to JSON...");
                JsonUtils.writeJsonPretty(JsonUtils.joinJsonObjects(JsonUtils.toJsonObjects(results)), out);
                return;
            } else {
                err.println("Error: No results return.");
            }
        } catch (ScannerException | WinnowingException e) {
            if (parent.debug) {
                e.printStackTrace(err);
            }
            throw e;
        }
        throw new RuntimeException(String.format("Something went wrong while scanning %s", folder));
    }
}
