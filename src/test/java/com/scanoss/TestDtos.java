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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.scanoss.utils.JsonUtils;
import dto.ScanFileResult;
import dto.ServerDetails;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.scanoss.TestConstants.jsonResultNoMatchString;
import static com.scanoss.TestConstants.jsonResultsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@Slf4j
public class TestDtos {
    @Before
    public void Setup() {
        log.info("Starting DTO test cases...");
        log.debug("Logging debug enabled");
        log.trace("Logging trace enabled");
    }

    @Test
    public void TestDtoServerDetails() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        ServerDetails serverDetails = new ServerDetails("version", new ServerDetails.KbVersion("monthly", "daily"));
        assertNotNull(serverDetails);
        assertFalse("Monthly value should be set", serverDetails.getKbVersion().getMonthly().isEmpty());
        log.info("Server Details: {}", serverDetails);

        String jsonString = "{\"version\": \"5.2.5\",\"kb_version\": {\"monthly\":\"23.06\", \"daily\":\"23.06.20\"}}";
        Gson gson = new Gson();
        serverDetails = gson.fromJson(jsonString, ServerDetails.class);
        assertNotNull(serverDetails);
        assertFalse("Server version should be set", serverDetails.getVersion().isEmpty());
//        assertFalse("Monthly value should be set", serverDetails.getKbVersion().getMonthly().isEmpty());
        log.info("Parsed Server Details: {}", serverDetails);

        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        assertNotNull(jsonObject);
        assertNotNull(jsonObject.get("version"));
        assertNotNull(jsonObject.getAsJsonObject("kb_version").get("monthly"));
        log.info("Parsed Object: {}", jsonObject);

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestFileJsonObject() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        JsonObject jsonObject = JsonUtils.toJsonObject(jsonResultNoMatchString);
        assertNotNull(jsonObject);
        log.info("Parsed Object: {}", jsonObject);
        log.info("KeySet: {}", jsonObject.keySet());

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestFileResults() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        JsonObject jsonObject = JsonUtils.toJsonObject(jsonResultsString);
        assertNotNull(jsonObject);

        List<ScanFileResult> results = JsonUtils.toScanFileResultsFromObject(jsonObject);
        assertNotNull(results);
        assertFalse("Should have results list", results.isEmpty());
        log.info("Scan results: {}", results);

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestDtoTemplate() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        log.info("Finished {} -->", methodName);
    }
}
