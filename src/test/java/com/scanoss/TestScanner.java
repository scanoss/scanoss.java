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
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

@Slf4j
public class TestScanner {

    @Before
    public void Setup() {
        log.info("Starting Scanner test cases...");
        log.debug("Logging debug enabled" );
        log.trace("Logging trace enabled" );
    }
    @Test
    public void TestScannerPositive() {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info( "<-- Starting {}", methodName );

        Scanner scanner = Scanner.builder().build();
        Winnowing winnowing = scanner.getWinnowing();
        assertNotNull("Winnowing should be enabled", winnowing);
        assertFalse("Snippets should be enabled", winnowing.getSkipSnippets());

        Winnowing winnowing1 = Winnowing.builder().skipSnippets(true).build();
        Scanner scanner1 = Scanner.builder().winnowing(winnowing1).build();
        assertTrue("Winnowing skip snippets should be true", scanner1.getWinnowing().getSkipSnippets());

        log.info( "Finished {} -->", methodName );
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
        log.info( "Finished {} -->", methodName );
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
        } catch (ScannerException e ) {
            log.info("Got expected error: {}", e.getLocalizedMessage());
        }
        try {
            String file = "testing/data/does-not-exist.java";
            String wfp = scanner.wfpFile(file);
            assertNull("Should not have gotten a WFP", wfp);
        } catch (ScannerException e ) {
            log.info("Got expected error: {}", e.getLocalizedMessage());
        }
        log.info( "Finished {} -->", methodName );
    }

    @Test
    public void TestScannerWfpFolderPositive() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        Scanner scanner = Scanner.builder().build();

        String folder = ".";
        List<String> wfps = scanner.wfpFolder(folder);
        assertNotNull("Should've gotten a WFP", wfps);
        assertFalse("WFP should not be empty", wfps.isEmpty());
        log.info("Retrieved {} WFPs from {}", wfps.size(), folder);

        scanner = Scanner.builder().allExtensions(true).build();
        folder = "testing";
        wfps = scanner.wfpFolder(folder);
        assertNotNull("Should've gotten a WFP", wfps);
        assertFalse("WFP should not be empty", wfps.isEmpty());
        log.info("Retrieved {} WFPs from {}", wfps.size(), folder);

        log.info( "Finished {} -->", methodName );
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
        log.info( "Finished {} -->", methodName );
    }

    @Test
    public void TestScannerScanFilePositive() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        Scanner scanner = Scanner.builder().build();

        String file = "src/test/java/com/scanoss/TestScanner.java";
        String results = scanner.scanFile(file);
        assertNotNull("Should've gotten a response", results);
        assertFalse("Scan results should not be empty", results.isEmpty());

        log.info( "Finished {} -->", methodName );
    }

    @Test
    public void TestScannerScanFolderPositive() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        Scanner scanner = Scanner.builder().build();
        String folder = "/Users/egans/Downloads/vue-dev";
        List<String> results = scanner.scanFolder(folder);
        assertNotNull("Should've gotten a response", results);
        assertFalse("Scan results should not be empty", results.isEmpty());
        log.info("Received {} results", results.size());

        log.info("Res Data: {}", results);

        log.info( "Finished {} -->", methodName );
    }


    @Test
    public void TestScannerTemplate() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        log.info( "Finished {} -->", methodName );
    }
}
