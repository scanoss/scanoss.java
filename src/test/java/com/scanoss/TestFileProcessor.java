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

import com.scanoss.processor.FileProcessor;
import com.scanoss.processor.ScanFileProcessor;
import com.scanoss.processor.WfpFileProcessor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@Slf4j
public class TestFileProcessor {
    @Before
    public void Setup() {
        log.info("Starting File Processor test cases...");
        log.debug("Logging debug enabled");
        log.trace("Logging trace enabled");
    }

    @Test
    public void TestWfpFileProcessorPositive() throws InterruptedException {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        FileProcessor wfpProcessor = WfpFileProcessor.builder().build();

        String file = "src/test/java/com/scanoss/TestWinnowing.java";
        String wfp = wfpProcessor.process(file, file);
        assertNotNull("Expected a WFP here", wfp);
        assertFalse("Expected a basic WFP here", wfp.isEmpty());
        log.info("WFP contents - {}: {}", file, wfp);

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestScanFileProcessorPositive() throws InterruptedException {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        FileProcessor scanProcessor = ScanFileProcessor.builder().build();

        String file = "src/test/java/com/scanoss/TestWinnowing.java";
        String wfp = scanProcessor.process(file, file);
        assertNotNull("Expected a scan result here", wfp);
        assertFalse("Expected a scan result here", wfp.isEmpty());
        log.info("Scan contents - {}: {}", file, wfp);

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestFileProcessorTemplate() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        log.info("Finished {} -->", methodName);
    }
}
