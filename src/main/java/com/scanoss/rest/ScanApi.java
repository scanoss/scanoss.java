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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

@Getter
@Builder
@Slf4j
public class ScanApi {

    static final String DEFAULT_SCAN_URL = "https://osskb.org/api/scan/direct";
    static final String DEFAULT_SCAN_URL2 = "https://scanoss.com/api/scan/direct";
//    static final String SCANOSS_SCAN_URL = System.getenv("SCANOSS_SCAN_URL");

    @Builder.Default
    private String scanType = "identify";
    @Builder.Default
    private Integer timeout = 120;
    @Builder.Default
    private Integer retryLimit = 5; // Retry limit for posting scan requests
    private String url;
    private String apiKey;
    private String flags;
    private HttpClient httpClient;
    private Map<String,String> headers;

    private ScanApi(String scanType, Integer timeout, Integer retryLimit, String url, String apiKey, String flags,
                   HttpClient httpClient, Map<String, String> headers) {
        this.scanType = scanType;
        this.timeout = timeout;
        this.retryLimit = retryLimit;
        this.url = url;
        this.apiKey = apiKey;
        this.flags = flags;
        if (this.apiKey != null && ! this.apiKey.isEmpty() && (url == null || url.isEmpty())) {
            this.url = DEFAULT_SCAN_URL2;
        } else if (url == null || url.isEmpty()) {
            this.url = DEFAULT_SCAN_URL;
        }
        if (httpClient == null) {
            this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(this.timeout)).build();
        } else {
            this.httpClient = httpClient;
        }
        if (headers == null) {
            this.headers = new HashMap<>(1);
            this.headers.put("user-agent", "scanoss-java/0.0.0");
            if(this.apiKey != null && ! this.apiKey.isEmpty()) {
                this.headers.put("x-api-key", this.apiKey);
            }
        } else {
            this.headers = headers;
        }
    }

    public String scan(String wfp, String context, int scanID)  {

        String boundary = new BigInteger(256, new Random()).toString();
        String uuid = UUID.randomUUID().toString();
        Map<String,String> postHeaders = new HashMap<>(this.headers.size() + 6);
        postHeaders.putAll(this.headers);
        postHeaders.put("x-request-id", uuid);
        postHeaders.put("Accept", "application/json");
        postHeaders.put("Content-Type", "multipart/form-data;boundary=" + boundary);
        Map<Object, Object> data = new HashMap<>();
        data.put("file", wfp);
        // TODO add type, assets, flags, context support here also

        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .headers(postHeaders.entrySet().stream()
                            .flatMap(entry -> Stream.of(entry.getKey(), entry.getValue()))
                            .toArray(String[]::new))
                    .POST(ofMimeMultipartData(data, boundary, uuid))
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        try {
            log.trace("Sending request to: {} - {}", request.uri(), request.headers());
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != HttpStatusCode.OK.getValue())
            {
                log.warn("Problem encountered sending WFP to API ({}): {}", HttpStatusCode.getByValue(response.statusCode()), response.body());
                return null;
            }
            log.info("Response body: {}", response.body());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpRequest.BodyPublisher ofMimeMultipartData(Map<Object, Object> data, String boundary, String uuid) {
        var byteArrays = new ArrayList<byte[]>();
        byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=").getBytes(StandardCharsets.UTF_8);
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
//        for(byte[] bytes: byteArrays) {
//            log.info("Body Data: {}", new String(bytes));
//        }
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

}
