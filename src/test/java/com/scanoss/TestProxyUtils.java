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

import com.scanoss.utils.ProxyUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.net.Proxy;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@Slf4j
public class TestProxyUtils {

    @Before
    public void Setup() {
        log.info("Starting Proxy Utils test cases...");
        log.debug("Logging debug enabled");
        log.trace("Logging trace enabled");
    }

    @Test
    public void TestProxyPositive() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        Proxy proxy = ProxyUtils.createProxyFromString("https://localhost:9090");
        assertNotNull(proxy);
        log.info("Proxy1: {}", proxy);
        proxy = ProxyUtils.createProxyFromString("http://localhost:8080");
        assertNotNull(proxy);
        log.info("Proxy2: {}", proxy);

        proxy = ProxyUtils.createProxyFromHostPort("localhost", 7070);
        assertNotNull(proxy);
        log.info("Proxy3: {}", proxy);

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestProxyNegative() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        Proxy proxy = ProxyUtils.createProxyFromString("localhost:6060");
        assertNull(proxy);

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestEnvProxyPositive() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        String existProxy = System.setProperty("http_proxy", "https://localhost:8080");
        Proxy proxy = ProxyUtils.createProxyFromEnv();
        assertNotNull(proxy);
        log.info("Proxy Env: {}", proxy);
        if (existProxy != null && !existProxy.isEmpty()) {
            System.setProperty("http_proxy", existProxy);
        } else {
            System.clearProperty("http_proxy");
        }

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void TestEnvProxyNegative() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        String existProxy = System.setProperty("http_proxy", "localhost:8080");
        Proxy proxy = ProxyUtils.createProxyFromEnv();
        assertNull(proxy);
        if (existProxy != null && !existProxy.isEmpty()) {
            System.setProperty("http_proxy", existProxy);
        } else {
            System.clearProperty("http_proxy");
        }

        log.info("Finished {} -->", methodName);
    }
}
