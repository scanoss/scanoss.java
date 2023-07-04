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

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * Command Line Processor Class
 * <p>
 * Provide a CLI to interacting with the SCANOSS Java SDK
 * </p>
 */
@Command(name = "scanoss-java",
        description = "SCANOSS Java CLI, License: MIT",
        subcommands = {
                VersionCommandLine.class,
                WfpCommandLine.class,
                ScanCommandLine.class,
        }
)
//        mixinStandardHelpOptions = true
public class CommandLine implements Runnable {
    static final String version = getVersion();
    @Option(names = {"-d", "--debug"}, description = "Enable debug output")
    static boolean debug;
    @Option(names = "--trace")
    static boolean trace;
    @Option(names = "--quiet", description = "Run the command in quite mode")
    static boolean quiet;
    @Option(names = { "-h", "--help" }, usageHelp = true, description = "Display help information")
    private boolean helpRequested = false;


    @Override
    public void run() {
        System.out.println("scanoss-java command processor.");
    }

    private static synchronized String getVersion() {
        String version = null;
        // try to load from maven properties first
        try {
            Properties p = new Properties();
            InputStream is = CommandLine.class.getResourceAsStream("/META-INF/maven/com.scanoss/scanoss/pom.properties");
            if (is != null) {
                p.load(is);
                version = p.getProperty("version", "");
            }
        } catch (IOException | RuntimeException e) {
            // ignore
        }
        // fallback to using Java API
        if (version == null) {
            Package aPackage = CommandLine.class.getPackage();
            if (aPackage != null) {
                version = aPackage.getImplementationVersion();
                if (version == null) {
                    version = aPackage.getSpecificationVersion();
                }
            }
        }
        // we could not compute the version so use a blank
        if (version == null) {
            version = "";
        }
        return version;
    }

    /**
     * Print the given Debug message to the specified Writer
     * <p>
     *     Only print if debug is enabled and quiet is disabled
     * </p>
     * @param writer Print Writer
     * @param msg Message to print
     */
    static void printDebug(PrintWriter writer, String msg) {
        if (!quiet && debug && writer != null) {
            writer.println(msg);
        }
    }

    /**
     * Print the given message to the specified Writer
     * <p>
     *     Only print details if <code>quiet</code> mode is not enabled
     * </p>
     * @param writer Print Writer
     * @param msg Message to print
     */
    static void printMsg(PrintWriter writer, String msg) {
        if (!quiet && writer != null) {
            writer.println(msg);
        }
    }

    /**
     * Main Command Line entry point
     * @param args command arguments
     */
    public static void main(String[] args) {
        int exitCode = new picocli.CommandLine(new CommandLine()).execute(args);
        System.exit(exitCode);
    }
}