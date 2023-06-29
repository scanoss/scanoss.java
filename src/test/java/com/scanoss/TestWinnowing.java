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


import com.scanoss.exceptions.WinnowingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

@Slf4j
public class TestWinnowing {

    // Pattern to test for generated snippet IDs in a WFP
    private static final Pattern snippetPat = Pattern.compile("^\\d+=\\w+.*$", Pattern.MULTILINE);

    @Before
    public void Setup() throws IOException {
        log.info("Starting Winnowing test cases...");
        log.debug("Logging debug enabled" );
        log.trace("Logging trace enabled" );
        for(String filename: Arrays.asList("tmp/cannot-read.java", "tmp/cannot-read.extension")) {
            File file = new File(filename);
            if (!file.exists()) {
                if ( file.createNewFile() ) {
                    log.debug("Created test file: {}", filename);
                }
            }
            if (file.exists()) {
                if ( file.setReadable(false) ) {
                    log.debug("Removed read bit from: {}", filename);
                }
            }
        }
    }

    @After
    public void Teardown() {
        log.info("Finishing Winnowing test cases...");
        for(String filename: Arrays.asList("tmp/cannot-read.java", "tmp/cannot-read.extension")) {
            File file = new File(filename);
            if (file.exists()) {
                if (! file.delete()) {
                    log.warn("Failed to remove temporary file: {}", filename);
                }
            }
        }
    }

    @Test
    public void TestWinnowingPositive() {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info( "<-- Starting {}", methodName );

        Winnowing winnowing = Winnowing.builder().build();
        assertFalse("Snippets should be enabled", winnowing.getSkipSnippets());

        Winnowing win2 = Winnowing.builder()
                .skipSnippets(true).allExtensions(true).obfuscate(true).hpsm(true)
                .build();
        assertTrue("All Extensions should be enabled", win2.getAllExtensions());

        log.info( "Finished {} -->", methodName );
    }

    @Test
    public void TestWinnowingContents() {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info( "<-- Starting {}", methodName );
        Winnowing winnowing = Winnowing.builder().build();

        byte[] contents = "sample c code with lots of code that we should analyse\nAnd even more code to get connected.\nAnd we need to get this as long as possible, in order to trigger snippet matching.\nHere comes more code to help get this working.\nPlease help get this across the line. We need all the help we can get.\n".getBytes();
        String wfp = winnowing.wfpForContents("local-file.c", false, contents);
        assertNotNull(wfp);
        assertTrue(wfp.length() > 0);
        log.info("TestWinnowingContents - WFP contents: {}", wfp);

        log.info( "Finished {} -->", methodName );
    }
    @Test
    public void TestWinnowingFiles() {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info( "<-- Starting {}", methodName );
        Winnowing winnowing = Winnowing.builder().build();

        String file = "testing/data/empty.java";
        String wfp = winnowing.wfpForFile(file, "testing/data/empty.java");
        assertNotNull("Expected a basic WFP here", wfp);
        assertTrue("Expected a basic WFP here", wfp.length() > 0);
        log.info("TestWinnowingFiles - WFP contents - {}: {}", file, wfp);

        file = "src/test/java/com/scanoss/TestWinnowing.java";
        wfp = winnowing.wfpForFile("src/test/java/com/scanoss/TestWinnowing.java", "TestWinnowing.java");
        log.info("Winnowing.java WFP: {}", wfp);
        assertNotNull("Expected a WFP here", wfp);
        assertTrue("Expected a basic WFP here",wfp.length() > 0);
        assertTrue("Should have snippets here", snippetPat.matcher(wfp).find());
        log.info("TestWinnowingFiles - WFP contents - {}: {}", file, wfp);

        log.info( "Finished {} -->", methodName );
    }

    @Test
    public void TestWinnowingFileSkipSnippets() {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info( "<-- Starting {}", methodName );
        Winnowing winnowing = Winnowing.builder().build();

        String file = "testing/data/non-source.json";
        String wfp = winnowing.wfpForFile(file, file);
        log.info("WFP for Json: {}", wfp );
        assertNotNull("Expected a result from WFP", wfp);
        assertEquals("file=f8d52217f24ea77ff80a6b1f421d0959,229084,testing/data/non-source.json", wfp.trim());

        file = "testing/data/test-file.txt";
        wfp = winnowing.wfpForFile(file, file);
        log.info("WFP for Json: {}", wfp );
        assertNotNull("Expected a result from WFP", wfp);
        assertEquals("file=e3dd1a7915d51c8cd1498585e6cea41e,183,testing/data/test-file.txt", wfp.trim());

        file = "testing/data/too-small.c";
        wfp = winnowing.wfpForFile(file, file);
        log.info("WFP for Json: {}", wfp );
        assertNotNull("Expected a result from WFP", wfp);
        assertEquals("file=8af27a287d55608a75cca12a794a6508,38,testing/data/too-small.c", wfp.trim());

        file = "testing/data/json-file.c";
        wfp = winnowing.wfpForFile(file, file);
        log.info("WFP for Json: {}", wfp );
        assertNotNull("Expected a result from WFP", wfp);
        assertEquals("file=d7cfce9cff6d109c6b0249233ee26368,345,testing/data/json-file.c", wfp.trim());

        winnowing.setSkipSnippets(true);
        file = "src/test/java/com/scanoss/TestWinnowing.java";
        wfp = winnowing.wfpForFile(file, file);
        log.info("WFP for Json: {}", wfp );
        assertNotNull("Expected a result from WFP", wfp);
        assertFalse("Should NOT have snippets here", snippetPat.matcher(wfp).matches());

        log.info( "Finished {} -->", methodName );
    }

    @Test
    public void TestWinnowingAllExtensions() {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info( "<-- Starting {}", methodName );
        Winnowing winnowing = Winnowing.builder().allExtensions(true).build();

        String file = "testing/data/test-file.txt";
        String wfp = winnowing.wfpForFile(file, file);
        log.info("WFP for Json: {}", wfp );
        assertNotNull("Expected a result from WFP", wfp);

        log.info( "Finished {} -->", methodName );
    }

    @Test
    public void TestWinnowingSkipSnippets() {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info( "<-- Starting {}", methodName );
        Winnowing winnowing = Winnowing.builder().allExtensions(true).build();

        String file = "testing/data/test-file.txt";
        String wfp = winnowing.wfpForFile(file, file);
        log.info("WFP for Json: {}", wfp );
        assertNotNull("Expected a result from WFP", wfp);

        log.info( "Finished {} -->", methodName );
    }
    @Test
    public void TestWinnowingFileFailures() {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info( "<-- Starting {}", methodName );
        Winnowing winnowing = Winnowing.builder().build();
        try {
            winnowing.wfpForFile("", "");
            fail("Should've generated an exception");
        } catch (WinnowingException e ) {
            log.info("Got expected error: {}", e.getLocalizedMessage());
        }
        try {
            winnowing.wfpForFile("testing/data/does-not-exist.java", "does-not-exist.java");
            fail("Should've generated an exception");
        } catch (WinnowingException e ) {
            log.info("Got expected error: {}", e.getLocalizedMessage());
        }
        try {
            winnowing.wfpForFile("tmp/cannot-read.java", "tmp/cannot-read.java");
            fail("Should've generated an exception");
        } catch (WinnowingException e ) {
            log.info("Got expected error: {}", e.getLocalizedMessage());
        }
        try {
            winnowing.wfpForFile("tmp/cannot-read.extension", "tmp/cannot-read.extension");
            fail("Should've generated an exception");
        } catch (WinnowingException e ) {
            log.info("Got expected error: {}", e.getLocalizedMessage());
        }

        String wfp = winnowing.wfpForFile("testing/data/empty.java", "testing/data/empty.java");
        assertNotNull("Empty file should generate a WFP", wfp);

        log.info( "Finished {} -->", methodName );
    }
}
