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
package com.scanoss.rest;

import com.scanoss.exceptions.ScanApiException;
import com.scanoss.utils.PackageDetails;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * SCANOSS Scanning REST API Implementation
 * <p>
 * Provides the ability to issue scan requests for a WFP
 * </p>
 */
@Getter
@Builder
@Slf4j
public class ScanApi {
    @Builder.Default
    private String scanType = "identify";
    @Builder.Default
    private Duration timeout = Duration.ofSeconds(120); // API POST timeout
    @Builder.Default
    private Integer retryLimit = 5; // Retry limit for posting scan requests
    private String url; // SCANOSS API URI
    private String apiKey; // SCANOSS premium API key
    private String flags; // SCANOSS Premium scanning flags
    private String sbomType; // SBOM type (identify/ignore)
    private String sbom;  // SBOM to supply while scanning
    private OkHttpClient okHttpClient;
    private Map<String, String> headers;

    @SuppressWarnings("unused")
    private ScanApi(String scanType, Duration timeout, Integer retryLimit, String url, String apiKey, String flags,
                    String sbomType, String sbom,
                    OkHttpClient okHttpClient, Map<String, String> headers) {
        this.scanType = scanType;
        this.timeout = timeout;
        this.retryLimit = retryLimit;
        this.url = url;
        this.apiKey = apiKey;
        this.flags = flags;
        this.sbomType = sbomType;
        this.sbom = sbom;
        if (this.apiKey != null && !this.apiKey.isEmpty() && (url == null || url.isEmpty())) {
            this.url = DEFAULT_SCAN_URL2;  // Default premium SCANOSS endpoint
        } else if (url == null || url.isEmpty()) {
            this.url = DEFAULT_SCAN_URL;  // Default free SCANOSS endpoint
        }
        this.okHttpClient = Objects.requireNonNullElseGet(okHttpClient, () ->
                new OkHttpClient.Builder().callTimeout(timeout).build()
        );
        this.headers = Objects.requireNonNullElseGet(headers, () -> new HashMap<>(2));
        // Add the user agent to the headers if it's not already there
        if (!this.headers.containsKey("user-agent")) {
            String version = PackageDetails.getVersion();
            if (version == null || version.isEmpty()) {
                version = "0.0.0"; // nothing found. set a default value
            }
            this.headers.put("user-agent", String.format("scanoss-java/%s", version));
        }
        // Add the API key to the headers if it's not already there
        if (!this.headers.containsKey("x-api-key") && this.apiKey != null && !this.apiKey.isEmpty()) {
            this.headers.put("x-api-key", this.apiKey);
        }
    }

    /**
     * Scan the given WFP
     *
     * @param wfp     Fingerprint to scan
     * @param context Context for the scan (optional)
     * @param scanID  ID of the requesting scanner (usually thread ID)
     * @return Scan results (in JSON format)
     * @throws ScanApiException Scanning went wrong
     */
    public String scan(String wfp, String context, int scanID) throws ScanApiException {
        if (wfp == null || wfp.isEmpty()) {
            throw new ScanApiException("No WFP specified. Cannot scan.");
        }
        String uuid = UUID.randomUUID().toString();
        // Copy & setup headers
        Map<String, String> postHeaders = new HashMap<>(this.headers.size() + 2);
        postHeaders.putAll(this.headers);
        postHeaders.put("x-request-id", uuid);
        postHeaders.put("Accept", "application/json");
        // Setup multipart data to post
        Map<String, String> data = new HashMap<>(1);
        data.put("file", wfp);
        if (context != null && !context.isEmpty()) {
            data.put("context", context);
        }
        if (flags != null && !flags.isEmpty()) {
            data.put("flags", flags);
        }
        if (sbom != null && !sbom.isEmpty()) {
            String type = sbomType != null ? sbomType : "identify";  // Set SBOM type or default to 'identify'
            data.put(type, sbom);
        }
        Request request;  // Create multipart request
        try {
            request = new Request.Builder().url(url).headers(Headers.of(postHeaders))
                    .post(multipartData(data, uuid))
                    .build();
        } catch (IllegalArgumentException e) {
            throw new ScanApiException(String.format("Problem with the URI: %s", url), e);
        }
        // Post request body and return response, retrying where necessary
        int retry = 0;
        Response response = null;
        ResponseBody body = null;
        do {
            try {
                if (retry > 0) {
                    log.debug("Connection timeout {} (retry {}) for {}. Sleeping, then trying again...", timeout.getSeconds(), retry, uuid);
                    TimeUnit.SECONDS.sleep(RETRY_FAIL_SLEEP_TIME); // Sleep ? seconds before trying again
                }
                response = okHttpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    body = response.body();
                    if (body == null) {
                        log.error("Empty response body received for {} - {} against {}. Response {}", scanID, uuid, url, response.code());
                    } else {
                        return body.string();
                    }
                } else if (response.code() == HttpStatusCode.SERVICE_UNAVAILABLE.getValue()) {
                    log.error("SCANOSS API rejected the scan request ({}) for {} due to service limits being exceeded", uuid, url);
                    throw new ScanApiException("Service Limits exceeded");
                } else {
                    log.error("Something went wrong scanning: {} - {} against {}. Response {} ({}): {}",
                            scanID, uuid, url, response.code(),
                            HttpStatusCode.getByValueToString(response.code()), response.message());
                }
                return null;
            } catch (InterruptedIOException e) {
                if (retry >= retryLimit) {
                    log.error("Error: SCANOSS API request timed out");
                    throw new ScanApiException("SCANOSS API request timed out for " + url, e);
                }
            } catch (IOException | InterruptedException | NullPointerException e) {
                throw new ScanApiException(String.format("Problem encountered scanning: %d - %s against %s", scanID, uuid, url), e);
            } finally {
                if (body != null) {
                    body.close();
                }
                if (response != null) {
                    response.close();
                }
            }
            retry++;
        } while (retry <= retryLimit);
        throw new ScanApiException(String.format("Something went wrong scanning request %s against %s.", uuid, url));
    }

    /**
     * Return a Multipart Request Body for the given data
     *
     * @param data data to put into the multipart body
     * @param uuid UUID to use for the WFP filename
     * @return Multipart Request Body
     */
    private RequestBody multipartData(Map<String, String> data, String uuid) {

        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();
            if (key.equals("file")) {  // Setup WFP contents
                builder.addFormDataPart(key, uuid + ".wfp",
                        RequestBody.create(entry.getValue(), MediaType.parse("text/plain"))
                );
            } else {
                builder.addFormDataPart(key, entry.getValue());
            }
        }
        return builder.build();
    }

    private static final int RETRY_FAIL_SLEEP_TIME = 5; // Time to sleep between failed scan requests
    static final String DEFAULT_SCAN_URL = "https://osskb.org/api/scan/direct"; // Free OSS OSSKB URL
    static final String DEFAULT_SCAN_URL2 = "https://scanoss.com/api/scan/direct"; // Standard SCANOSS Premium URL
//    static final String SCANOSS_SCAN_URL = System.getenv("SCANOSS_SCAN_URL");
}
