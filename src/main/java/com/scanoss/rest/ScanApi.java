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

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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
    private Integer timeout = 120; // API POST timeout
    @Builder.Default
    private Integer retryLimit = 5; // Retry limit for posting scan requests
    private String url; // SCANOSS API URI
    private String apiKey; // SCANOSS premium API key
    private String flags; // SCANOSS Premium scanning flags
    private String sbomType; // SBOM type (identify/ignore)
    private String sbom;  // SBOM to supply while scanning
    private HttpClient httpClient;
    private Map<String, String> headers;

    @SuppressWarnings("unused")
    private ScanApi(String scanType, Integer timeout, Integer retryLimit, String url, String apiKey, String flags,
                    String sbomType, String sbom,
                    HttpClient httpClient, Map<String, String> headers) {
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


        this.httpClient = Objects.requireNonNullElseGet(httpClient, () ->
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(this.timeout)).build());

        //        if (httpClient == null) {
//            this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(this.timeout)).build();
//        } else {
//            this.httpClient = httpClient;
//        }
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
     * @throws ScanApiException     Scanning went wrong
     * @throws InterruptedException Scan API was interrupted
     */
    public String scan(String wfp, String context, int scanID) throws ScanApiException, InterruptedException {
        if (wfp == null || wfp.isEmpty()) {
            throw new ScanApiException("No WFP specified. Cannot scan.");
        }
        String boundary = new BigInteger(256, new Random()).toString();
        String uuid = UUID.randomUUID().toString();
        Map<String, String> postHeaders = new HashMap<>(this.headers.size() + 6);
        postHeaders.putAll(this.headers);
        postHeaders.put("x-request-id", uuid);
        postHeaders.put("Accept", "application/json");
        postHeaders.put("Content-Type", "multipart/form-data;boundary=" + boundary);
        Map<Object, Object> data = new HashMap<>();
        data.put("file", wfp);
        if (context != null && !context.isEmpty()) {
            data.put("context", context);
        }
        if (flags != null && !flags.isEmpty()) {
            data.put("flags", flags);
        }
        if (sbom != null && !sbom.isEmpty()) {
            String type = sbomType != null ? sbomType : "identify";
            data.put(type, sbom);
        }
        HttpRequest request;
        try {
            log.info("Setting timeout of {}", timeout);
            request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .headers(postHeaders.entrySet().stream()
                            .flatMap(entry -> Stream.of(entry.getKey(), entry.getValue()))
                            .toArray(String[]::new))
                    .timeout(Duration.ofSeconds(timeout))
                    .POST(ofMimeMultipartData(data, boundary, uuid))
                    .build();
        } catch (URISyntaxException | IllegalArgumentException e) {
            throw new ScanApiException(String.format("Problem with the URI: %s", url), e);
        }
        int retry = 0;
        do {
            retry++;
            try {
                log.trace("Sending request to: {} - {}", request.uri(), request.headers());
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int statusCode = response.statusCode();
                if (statusCode != HttpStatusCode.OK.getValue()) {
                    log.warn("Problem encountered sending WFP to API ({}): {}", HttpStatusCode.getByValueToString(response.statusCode()), response.body());
                    if (statusCode == HttpStatusCode.SERVICE_UNAVAILABLE.getValue()) {
                        log.error("SCANOSS API rejected the scan request ({}) for {} due to service limits being exceeded", uuid, url);
                        throw new ScanApiException("Service Limits exceeded");
                    }
                    return null;
                }
                return response.body();
            } catch (HttpTimeoutException e) {
                if (retry > retryLimit) {
                    log.error("Error: SCANOSS API request timed out");
                    throw new ScanApiException("SCANOSS API request timed out for " + url, e);
                }
                log.debug("Connection timeout {} (retry {}). Sleeping, then trying again...", timeout, retry);
                TimeUnit.SECONDS.sleep(RETRY_FAIL_SLEEP_TIME); // Sleep ? seconds before trying again
            } catch (IOException | InterruptedException | NullPointerException e) {
                throw new ScanApiException(String.format("Problem encountered scanning: %d - %s against %s", scanID, uuid, url), e);
            }
        } while (retry <= retryLimit);

        throw new ScanApiException(String.format("Something went wrong scanning request %s against %s.", uuid, url));
    }

    /**
     * Return a multipart encoded Body Publisher for the given data
     *
     * @param data     data to put into the multipart message
     * @param boundary boundary to use for each multipart
     * @param uuid     UUID to use for the WFP filename
     * @return Multipart Body Publisher
     */
    private HttpRequest.BodyPublisher ofMimeMultipartData(Map<Object, Object> data, String boundary, String uuid) {
        var byteArrays = new ArrayList<byte[]>();
        byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=").getBytes(StandardCharsets.UTF_8);
        // Cycle through each data entry and add to the multipart body
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            byteArrays.add(separator);
            if (entry.getKey().equals("file")) {  // Setup WFP contents
                var wfp = (String) entry.getValue();
                byteArrays.add(("\"" + entry.getKey() + "\"; filename=\"" + uuid + ".wfp\"\r\n" +
                        "Content-Type: text/plain" + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                byteArrays.add(wfp.getBytes(StandardCharsets.UTF_8));
                byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
            } else {  // Add other form data
                byteArrays.add(("\"" + entry.getKey() + "\"\r\n\r\n" + entry.getValue() + "\r\n").getBytes(StandardCharsets.UTF_8));
            }
        }
        byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

    private static final int RETRY_FAIL_SLEEP_TIME = 5; // Time to sleep between failed scan requests
    static final String DEFAULT_SCAN_URL = "https://osskb.org/api/scan/direct"; // Free OSS OSSKB URL
    static final String DEFAULT_SCAN_URL2 = "https://scanoss.com/api/scan/direct"; // Standard SCANOSS Premium URL
//    static final String SCANOSS_SCAN_URL = System.getenv("SCANOSS_SCAN_URL");
}
