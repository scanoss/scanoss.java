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

import com.scanoss.exceptions.ScanApiException;
import com.scanoss.rest.HttpStatusCode;
import com.scanoss.rest.ScanApi;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.scanoss.TestConstants.SCAN_RESP_SUCCESS;
import static com.scanoss.TestConstants.customSelfSignedCertificate;
import static org.junit.Assert.*;

@Slf4j
public class TestScanApi {
    private MockWebServer server;

    @Before
    public void Setup() throws IOException {
        log.info("Starting ScanApi test cases...");
        log.debug("Logging debug enabled");
        log.trace("Logging trace enabled");
        log.info("Starting Mock Server...");
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
    public void TestScanApiPositive() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        ScanApi scanApi = ScanApi.builder().build();
        assertFalse("Should have pre-configured headers", scanApi.getHeaders().isEmpty());

        scanApi = ScanApi.builder().apiKey("test-key").okHttpClient(new OkHttpClient()).build();
        assertFalse("API Key should be set", scanApi.getApiKey().isEmpty());

        Map<String, String> headers = new HashMap<>(1);
        headers.put("test-header-key", "test-header-value");
        scanApi = ScanApi.builder().headers(headers).build();
        assertTrue("Should have custom header key", scanApi.getHeaders().containsKey("test-header-key"));

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestScanApiScanPositive() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        String sbomIdentify = "pkg:github/scanoss/scanoss.py\n" + "pkg:pypi/scanoss\n";
        ScanApi scanApi = ScanApi.builder().flags("8").scanType("identify").sbom(sbomIdentify)
                .url(server.url("/api/scan/direct").toString()).build();
        server.enqueue(new MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(SCAN_RESP_SUCCESS).setResponseCode(200));
        String result = scanApi.scan("file=....", "pkg:github/scanoss/scanoss.py", 1);
        assertNotNull(result);
        assertFalse("Should've gotten a response JSON", result.isEmpty());
        log.info("Scan response: {}", result);

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestScanApiScanCustomCertPositive() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        String sbomIdentify = "pkg:github/scanoss/scanoss.py\n" + "pkg:pypi/scanoss\n";
        ScanApi scanApi = ScanApi.builder().flags("8").scanType("identify").sbom(sbomIdentify)
                .url(server.url("/api/scan/direct").toString())
                .customCert(customSelfSignedCertificate)
                .build();
        server.enqueue(new MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(SCAN_RESP_SUCCESS).setResponseCode(200));
        String result = scanApi.scan("file=....", "pkg:github/scanoss/scanoss.py", 1);
        assertNotNull(result);
        assertFalse("Should've gotten a response JSON", result.isEmpty());
        log.info("Scan response: {}", result);

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestScanApiScanNegative() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);
        // Empty WFP
        try {
            ScanApi scanApi = ScanApi.builder().build();
            String result = scanApi.scan("", "", 1);
            assertNull("Scan result should be null", result);
        } catch (ScanApiException e) {
            log.info("Got expected Exception: {}", e.getLocalizedMessage());
        }
        // Invalid URL
        try {
            ScanApi scanApi = ScanApi.builder().url("invalid-url").build();
            String result = scanApi.scan("file=...", "", 1);
            assertNull("Scan result should be null", result);
        } catch (ScanApiException e) {
            log.info("Got expected Exception: {}", e.getLocalizedMessage());
        }
        // Internal Server Error
        ScanApi scanApi = ScanApi.builder().url(server.url("/api/scan/direct").toString()).build();

        server.enqueue(new MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("Scan failed").setResponseCode(HttpStatusCode.INTERNAL_SERVER_ERROR.getValue())
        );
        String result = scanApi.scan("file=....", "", 1);
        log.info("Scan response: {}", result);
        assertNull("Should've gotten a null response to this scan", result);
        // Service Limit exceeded
        try {
            server.enqueue(new MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                    .setBody("Scan failed. Service limit exceeded.").setResponseCode(HttpStatusCode.SERVICE_UNAVAILABLE.getValue())
            );
            String result2 = scanApi.scan("file=...", "", 1);
            assertNull("Scan result should be null", result2);
        } catch (ScanApiException e) {
            log.info("Got expected Exception: {}", e.getLocalizedMessage());
        }
        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestScanApiScanTimeoutNegative() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        ScanApi scanApi = ScanApi.builder().url(server.url("/api/scan/direct").toString())
                .timeout(Duration.ofSeconds(2)).retryLimit(2).build();
        // Setup 3 time-delayed responses
        for (int i = 0; i < 3; i++) {
            server.enqueue(new MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                    .setBodyDelay(10, TimeUnit.SECONDS)
                    .setBody(SCAN_RESP_SUCCESS).setResponseCode(200));
        }
        try {
            String result = scanApi.scan("file=....", "", 1);
            log.info("Scan response: {}", result);
            assertNull("Should've gotten a null response to this scan", result);
        } catch (ScanApiException e) {
            log.info("Got expected Exception: {}", e.getLocalizedMessage());
        }
        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestScanApiTemplate() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        log.info("Finished {} -->", methodName);
    }

}
