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

import com.google.gson.JsonObject;
import com.scanoss.utils.JsonUtils;
import com.scanoss.dto.ScanFileResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static com.scanoss.TestConstants.*;
import static org.junit.Assert.*;

@Slf4j
public class TestJsonUtils {

    @Before
    public void Setup() {
        log.info("Starting JSON Utils test cases...");
        log.debug("Logging debug enabled");
        log.trace("Logging trace enabled");
    }

    @Test
    public void TestRawResultsPositive() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        JsonObject jsonObject = JsonUtils.toJsonObject(jsonResultWithMatchString);
        assertNotNull(jsonObject);
        JsonUtils.writeJsonPretty(jsonObject, new PrintWriter(System.err));

        List<String> rawResults = new ArrayList<>(2);
        rawResults.add(jsonResultNoMatchString);
        rawResults.add(jsonResultWithMatchString);

        List<JsonObject> jsonObjects = JsonUtils.toJsonObjects(rawResults);
        assertNotNull(jsonObjects);
        assertFalse("Should have decoded JSON Objects", jsonObjects.isEmpty());
        log.info("JSON Objects: {}", jsonObjects);

        jsonObject = JsonUtils.joinJsonObjects(jsonObjects);
        assertNotNull(jsonObject);
        log.info("JSON Object: {}", jsonObject);
        JsonUtils.writeJsonPretty(jsonObject, null);

        List<ScanFileResult> scanFileResults = JsonUtils.toScanFileResults(rawResults);
        assertNotNull(scanFileResults);
        assertFalse("Should have decode Scan File Results", scanFileResults.isEmpty());
        log.info("Scan File Results: {}", scanFileResults);

        jsonObject = JsonUtils.toJsonObject(jsonResultsString);
        assertNotNull(jsonObject);
        log.info("JSON Object: {}", jsonObject);

        scanFileResults = JsonUtils.toScanFileResultsFromObject(jsonObject);
        assertNotNull(scanFileResults);
        assertFalse("Should have decode Scan File Results", scanFileResults.isEmpty());
        log.info("Scan File Results: {}", scanFileResults);

        log.info("Finished {} -->", methodName);
    }

}
