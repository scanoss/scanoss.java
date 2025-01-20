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
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.scanoss.TestConstants.SCAN_RESP_SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
public class TestCli {

    private MockWebServer server;

    @Before
    public void Setup() throws IOException {
        log.info("Starting CLI test cases...");
        log.debug("Logging debug enabled");
        log.trace("Logging trace enabled");
        server = new MockWebServer();
        server.start(); // Start the server.
    }

    @After
    public void Finish() {
        log.info("Shutting down mock server.");
        try {
            server.close();
            server.shutdown(); // Shut down the server. Instances cannot be reused.
        } catch (IOException e) {
            log.warn("Some issue shutting down mock server: {}", e.getLocalizedMessage());
        }
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

        String[] args = new String[]{"-d", "wfp", "src/test/java/com/scanoss/TestCli.java"};
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

        String[] args = new String[]{"-d", "scan", "src/test/java/com/scanoss/TestCli.java"};
        int exitCode = new picocli.CommandLine(new CommandLine()).execute(args);
        assertEquals("command should not fail", 0, exitCode);

        String[] args2 = new String[]{"-d", "scan", "src/test/java/com", "-T", "2", "--all-hidden",
                "--identify", "SBOM.json", "--skip-snippets", "--all-extensions", "-F", "256"
        };
        exitCode = new picocli.CommandLine(new CommandLine()).execute(args2);
        assertEquals("command should not fail", 0, exitCode);


        String[] args3 = new String[]{"-d", "scan", "src/test/java/com", "--settings", "src/test/resources/scanoss.json"
        };
        exitCode = new picocli.CommandLine(new CommandLine()).execute(args3);
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

        String[] args6 = new String[]{"-d", "scan", "src/test/java/com", "--settings", "does-not-exist.json"};
        exitCode = new picocli.CommandLine(new CommandLine()).execute(args6);
        assertTrue("command should fail", exitCode != 0);


        String[] args7 = new String[]{"-d", "scan", "src/test/java/com", "--settings", "src/test/resources/scanoss-broken.json"};
        exitCode = new picocli.CommandLine(new CommandLine()).execute(args7);
        assertTrue("command should fail", exitCode != 0);


        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestScanCommandMockPositive() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        server.enqueue(new MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(SCAN_RESP_SUCCESS).setResponseCode(200));

        String[] args = new String[]{"-d", "scan", "src/test/java/com/scanoss/TestCli.java", "-T", "2", "--all-hidden",
                "--identify", "SBOM.json", "--skip-snippets", "--all-extensions", "-F", "256",
                "-M", "60",
                "--ca-cert", "testing/data/localhost.pem",
                "--apiurl", server.url("/api/scan/direct").toString()
        };
        int exitCode = new picocli.CommandLine(new CommandLine()).execute(args);
        assertEquals("command should not fail", 0, exitCode);

        log.info("Finished {} -->", methodName);
    }

}
