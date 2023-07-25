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

import com.scanoss.cli.CommandLine;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
public class TestCli {

    @Before
    public void Setup() {
        log.info("Starting CLI test cases...");
        log.debug("Logging debug enabled");
        log.trace("Logging trace enabled");
    }

    @Test
    public void TestRootCommand() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);
        String[] args = new String[]{};
        int exitCode = new picocli.CommandLine(new CommandLine()).execute(args);
        assertEquals("command should not fail", 0, exitCode);

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestVersionCommand() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);
        String[] args = new String[]{"version"};
        int exitCode = new picocli.CommandLine(new CommandLine()).execute(args);
        assertEquals("command should not fail", 0, exitCode);

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestWfpCommandPositive() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        String[] args = new String[]{"-d", "wfp", "src/test/java/com/scanoss/TestScanner.java"};
        int exitCode = new picocli.CommandLine(new CommandLine()).execute(args);
        assertEquals("command should not fail", 0, exitCode);

        String[] args2 = new String[]{"-d", "wfp", "src/test/java/com"};
        exitCode = new picocli.CommandLine(new CommandLine()).execute(args2);
        assertEquals("command should not fail", 0, exitCode);

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestWfpCommandNegative() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);
        String[] args = new String[]{"-d", "wfp"};
        int exitCode = new picocli.CommandLine(new CommandLine()).execute(args);
        assertTrue("command should fail", exitCode != 0);

        String[] args2 = new String[]{"-d", "wfp", ""};
        exitCode = new picocli.CommandLine(new CommandLine()).execute(args2);
        assertTrue("command should fail", exitCode != 0);

        String[] args3 = new String[]{"-d", "wfp", "path/to/does-not-exist.java"};
        exitCode = new picocli.CommandLine(new CommandLine()).execute(args3);
        assertTrue("command should fail", exitCode != 0);

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestScanCommandPositive() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        String[] args = new String[]{"-d", "scan", "src/test/java/com/scanoss/TestScanner.java"};
        int exitCode = new picocli.CommandLine(new CommandLine()).execute(args);
        assertEquals("command should not fail", 0, exitCode);

        String[] args2 = new String[]{"-d", "scan", "src/test/java/com", "-T", "2", "--all-hidden",
                "--identify", "SBOM.json", "--skip-snippets", "--all-extensions", "-F", "256"
        };
        exitCode = new picocli.CommandLine(new CommandLine()).execute(args2);
        assertEquals("command should not fail", 0, exitCode);

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestScanCommandNegative() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);
        String[] args = new String[]{"-d", "scan"};
        int exitCode = new picocli.CommandLine(new CommandLine()).execute(args);
        assertTrue("command should fail", exitCode != 0);

        String[] args2 = new String[]{"-d", "scan", ""};
        exitCode = new picocli.CommandLine(new CommandLine()).execute(args2);
        assertTrue("command should fail", exitCode != 0);

        String[] args3 = new String[]{"-d", "scan", "path/to/does-not-exist.java"};
        exitCode = new picocli.CommandLine(new CommandLine()).execute(args3);
        assertTrue("command should fail", exitCode != 0);

        String[] args4 = new String[]{"-d", "scan", "path/to/does-not-exist.java",
                "--identify", "SBOM.json", "--ignore", "does-not-exist.json"
        };
        exitCode = new picocli.CommandLine(new CommandLine()).execute(args4);
        assertTrue("command should fail", exitCode != 0);

        String[] args5 = new String[]{"-d", "scan", "src/test/java/com", "--ignore", "does-not-exist.json"};
        exitCode = new picocli.CommandLine(new CommandLine()).execute(args5);
        assertTrue("command should fail", exitCode != 0);

        log.info("Finished {} -->", methodName);
    }
}
