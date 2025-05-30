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

import com.scanoss.dto.SbomLegacy;
import com.scanoss.exceptions.ScanApiException;
import com.scanoss.settings.Rule;
import com.scanoss.settings.ScanossSettings;
import com.scanoss.utils.JsonUtils;
import com.scanoss.utils.PackageDetails;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.tls.Certificates;
import okhttp3.tls.HandshakeCertificates;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Proxy;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.scanoss.ScanossConstants.DEFAULT_HTTP_RETRY_LIMIT;
import static com.scanoss.ScanossConstants.DEFAULT_TIMEOUT;

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
    private Duration timeout = Duration.ofSeconds(DEFAULT_TIMEOUT); // API POST timeout
    @Builder.Default
    private Integer retryLimit = DEFAULT_HTTP_RETRY_LIMIT; // Retry limit for posting scan requests
    private String url; // SCANOSS API URI
    private String apiKey; // SCANOSS premium API key
    private String flags; // SCANOSS Premium scanning flags
    private String sbomType; // SBOM type (identify/ignore)
    private String sbom;  // SBOM to supply while scanning
    private OkHttpClient okHttpClient; // okhttp3 client
    private Map<String, String> headers; // custom REST client headers
    private String customCert; // Custom certificate
    private Proxy proxy; // Proxy configuration
    private String baseUrl; // SCANOSS base API URI (to used instead of url)
    private ScanossSettings settings;
    @SuppressWarnings("unused")
    private ScanApi(String scanType, Duration timeout, Integer retryLimit, String url, String apiKey, String flags,
                    String sbomType, String sbom,
                    OkHttpClient okHttpClient, Map<String, String> headers, String customCert,
                    Proxy proxy, String baseUrl, ScanossSettings settings) {
        this.settings = settings;
        this.scanType = scanType;
        this.timeout = timeout;
        this.retryLimit = retryLimit;
        this.url = url;
        this.apiKey = apiKey;
        this.flags = flags;
        this.sbomType = sbomType;
        this.sbom = sbom;
        this.customCert = customCert;
        this.proxy = proxy;
        if ((url == null || url.isEmpty()) && (baseUrl != null && ! baseUrl.isEmpty())) {
            this.url = String.format( "%s/%s", baseUrl, DEFAULT_SCAN_URL);
        } else if (this.apiKey != null && !this.apiKey.isEmpty() && (this.url == null || this.url.isEmpty())) {
            this.url = DEFAULT_SCAN_URL2;  // Default premium SCANOSS endpoint
        } else if (url == null || url.isEmpty()) {
            this.url = DEFAULT_SCAN_URL;  // Default free SCANOSS endpoint
        }
        if (okHttpClient == null) {
            OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();
            okBuilder.callTimeout(this.timeout);  // Set default timeout
            // Build the HTTP client with a custom certificate (ignoring hostname verification)
            if (customCert != null && ! customCert.isEmpty()) {
                HandshakeCertificates certificates = new HandshakeCertificates.Builder()
                        .addTrustedCertificate(Certificates.decodeCertificatePem(customCert))
                        .build();
                okBuilder.hostnameVerifier((hostname, session) -> true);
                okBuilder.sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager());
            }
            if (proxy != null) {
                okBuilder.proxy(proxy);
            }
            this.okHttpClient = okBuilder.build();
        } else {
            this.okHttpClient = okHttpClient;
        }
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
            data.put("assets", sbom);
            data.put("type", type);
        }

        if (settings != null && !settings.getBom().getIgnore().isEmpty()) {
            List<Rule> rules = settings.getBom().getIgnore();
            SbomLegacy legacyIgnore = settings.getLegacySbom(rules);
            log.info("ignore rules detected. Converting to legacy rules {}", JsonUtils.toJson(legacyIgnore));
            data.put("assets", JsonUtils.toJson(legacyIgnore));
            data.put("type", "blacklist");
        }

        if (settings != null && !settings.getBom().getInclude().isEmpty()) {
            List<Rule> rules = settings.getBom().getInclude();
            SbomLegacy legacyInclude = settings.getLegacySbom(rules);
            log.info("include rules detected. Converting to legacy rules {}", JsonUtils.toJson(legacyInclude));
            data.put("assets", JsonUtils.toJson(legacyInclude));
            data.put("type", "identify");
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

    /**
     * Base URL for the SCANOSS OSSKB (Open Source Knowledge Base) free API.
     * This endpoint provides access to the free tier of SCANOSS scanning services.
     */
    public static final String DEFAULT_BASE_URL = "https://api.osskb.org";

    /**
     * Base URL for the SCANOSS Premium API.
     * This endpoint is used for premium/enterprise level scanning services.
     */
    public static final String DEFAULT_BASE_URL2 = "https://api.scanoss.com";

    static final String DEFAULT_SCAN_PATH = "scan/direct";
    static final String DEFAULT_SCAN_URL = String.format( "%s/%s", DEFAULT_BASE_URL, DEFAULT_SCAN_PATH ); // Free OSS OSSKB URL
    static final String DEFAULT_SCAN_URL2 = String.format( "%s/%s", DEFAULT_BASE_URL2, DEFAULT_SCAN_PATH ); // Standard SCANOSS Premium URL
}
