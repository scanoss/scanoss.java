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
package com.scanoss.utils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.net.*;

/**
 * SCANOSS Proxy Utils Class
 * <p>
 *     This class provides utility methods for setting up Proxy objects to be used by the HTTP Client.
 * </p>
 */
@Slf4j
public class ProxyUtils {

    /**
     * Create basic proxy from the supplied HTTP Proxy strting
     *
     * @param proxyString Proxy string
     * @return Proxy or <code>null</code>
     */
    public static Proxy createProxyFromString(@NonNull String proxyString) {
        try {
            URI uri = new URI(proxyString);
            return createProxyFromHostPort(uri.getHost(), uri.getPort());
        } catch (IllegalArgumentException | URISyntaxException | IllegalStateException | SecurityException | NullPointerException e) {
            log.warn("Issue encountered creating HTTP proxy from string: {} - {}", proxyString, e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * Create basic proxy from the given host/ort
     *
     * @param host hostname
     * @param port port number
     * @return Proxy or <code>null</code>
     * @throws IllegalArgumentException missing details for the proxy host/port
     * @throws SecurityException unable to resolve the hostname
     */
    public static Proxy createProxyFromHostPort(String host, int port) throws IllegalArgumentException, SecurityException {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
    }

    /**
     * Create a basic proxy from the System Environment variables
     * <p>
     *     First check the <code>https_proxy</code> value, then <code>http_proxy</code>
     * </p>
     * @return Proxy or <code>null</code>
     */
    public static Proxy createProxyFromEnv() {
        try {
            String proxyString = System.getProperty("https_proxy", "");
            if (proxyString.isEmpty()) {
                proxyString = System.getProperty("http_proxy", "");
            }
            if (!proxyString.isEmpty()) {
                return createProxyFromString(proxyString);
            }
        } catch(IllegalArgumentException | SecurityException | NullPointerException e) {
            log.warn("Issue looking up 'https_proxy' and/or 'http_proxy' environment variables: {}", e.getLocalizedMessage());
        }
        return null;
    }
}
