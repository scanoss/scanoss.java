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
import com.github.packageurl.PackageURL;
import com.scanoss.utils.Purl2Url;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@Slf4j
public class TestPurl2Url {

    @Test
    public void testValidGithubUrl() throws Exception {
        log.info("<-- Starting testValidGithubUrl");

        // Create test data - using React repository
        PackageURL purl = new PackageURL("pkg:github/facebook/react");

        // Verify support
        assertTrue("React GitHub repo should be supported", Purl2Url.isSupported(purl));

        // Generate URL
        String url = Purl2Url.convert(purl);

        // Verify
        assertNotNull("Generated URL should not be null", url);
        assertEquals("URL should match React GitHub repo",
                "https://github.com/facebook/react", url);

        log.info("Finished testValidGithubUrl -->");
    }

    @Test
    public void testValidNpmUrl() throws Exception {
        log.info("<-- Starting testValidNpmUrl");

        // Create test data - using Express.js
        PackageURL purl = new PackageURL("pkg:npm/express");

        // Verify support
        assertTrue("Express npm package should be supported", Purl2Url.isSupported(purl));

        // Generate URL
        String url = Purl2Url.convert(purl);

        // Verify
        assertNotNull("Generated URL should not be null", url);
        assertEquals("URL should match Express npm package",
                "https://www.npmjs.com/package/express", url);

        log.info("Finished testValidNpmUrl -->");
    }

    @Test
    public void testScopedNpmPackage() throws Exception {
        log.info("<-- Starting testScopedNpmPackage");

        // Create test data - using Angular core package
        PackageURL purl = new PackageURL("pkg:npm/%40angular/core");

        // Verify support
        assertTrue("Angular core package should be supported", Purl2Url.isSupported(purl));

        // Generate URL
        String url = Purl2Url.convert(purl);

        // Verify
        assertNotNull("Generated URL should not be null", url);
        assertEquals("URL should match Angular core package",
                "https://www.npmjs.com/package/@angular/core", url);

        log.info("Finished testScopedNpmPackage -->");
    }

    @Test
    public void testValidMavenUrl() throws Exception {
        log.info("<-- Starting testValidMavenUrl");

        // Create test data - using Spring Boot
        PackageURL purl = new PackageURL("pkg:maven/org.springframework.boot/spring-boot");

        // Verify support
        assertTrue("Spring Boot Maven artifact should be supported", Purl2Url.isSupported(purl));

        // Generate URL
        String url = Purl2Url.convert(purl);

        // Verify
        assertNotNull("Generated URL should not be null", url);
        assertEquals("URL should match Spring Boot Maven artifact",
                "https://mvnrepository.com/artifact/org.springframework.boot/spring-boot", url);

        log.info("Finished testValidMavenUrl -->");
    }

    @Test
    public void testValidPypiUrl() throws Exception {
        log.info("<-- Starting testValidPypiUrl");

        // Create test data - using Django
        PackageURL purl = new PackageURL("pkg:pypi/django");

        // Verify support
        assertTrue("Django PyPI package should be supported", Purl2Url.isSupported(purl));

        // Generate URL
        String url = Purl2Url.convert(purl);

        // Verify
        assertNotNull("Generated URL should not be null", url);
        assertEquals("URL should match Django PyPI package",
                "https://pypi.org/project/django", url);

        log.info("Finished testValidPypiUrl -->");
    }

    @Test
    public void testValidNugetUrl() throws Exception {
        log.info("<-- Starting testValidNugetUrl");

        // Create test data - using Newtonsoft.Json
        PackageURL purl = new PackageURL("pkg:nuget/Newtonsoft.Json");

        // Verify support
        assertTrue("Newtonsoft.Json NuGet package should be supported", Purl2Url.isSupported(purl));

        // Generate URL
        String url = Purl2Url.convert(purl);

        // Verify
        assertNotNull("Generated URL should not be null", url);
        assertEquals("URL should match Newtonsoft.Json NuGet package",
                "https://www.nuget.org/packages/Newtonsoft.Json", url);

        log.info("Finished testValidNugetUrl -->");
    }

    @Test
    public void testUnsupportedType() throws Exception {
        log.info("<-- Starting testUnsupportedType");

        // Create test data - using an unsupported type
        PackageURL purl = new PackageURL("pkg:unknown/test-package");

        // Verify non-support
        assertFalse("Unknown package type should not be supported", Purl2Url.isSupported(purl));

        // Generate URL
        String url = Purl2Url.convert(purl);

        // Verify
        assertNull("Generated URL should be null for unsupported type", url);

        log.info("Finished testUnsupportedType -->");
    }

    @Test
    public void testAllPackageTypes() throws Exception {
        log.info("<-- Starting testAllPackageTypes");

        class PackageTestCase {
            final String purl;
            final String expectedUrl;
            final String description;

            PackageTestCase(String purl, String expectedUrl, String description) {
                this.purl = purl;
                this.expectedUrl = expectedUrl;
                this.description = description;
            }
        }

        List<PackageTestCase> testCases = Arrays.asList(
                // GitHub Cases
                new PackageTestCase(
                        "pkg:github/facebook/react",
                        "https://github.com/facebook/react",
                        "Standard GitHub repository"
                ),
                new PackageTestCase(
                        "pkg:github/apache/kafka",
                        "https://github.com/apache/kafka",
                        "GitHub repo with no special chars"
                ),
                new PackageTestCase(
                        "pkg:github/spring-projects/spring-boot",
                        "https://github.com/spring-projects/spring-boot",
                        "GitHub repo with hyphen"
                ),

                // NPM Cases
                new PackageTestCase(
                        "pkg:npm/express",
                        "https://www.npmjs.com/package/express",
                        "Simple NPM package"
                ),
                new PackageTestCase(
                        "pkg:npm/%40angular/core",
                        "https://www.npmjs.com/package/@angular/core",
                        "Scoped NPM package"
                ),
                new PackageTestCase(
                        "pkg:npm/%40types/node",
                        "https://www.npmjs.com/package/@types/node",
                        "TypeScript definitions package"
                ),

                // Maven Cases
                new PackageTestCase(
                        "pkg:maven/org.springframework.boot/spring-boot",
                        "https://mvnrepository.com/artifact/org.springframework.boot/spring-boot",
                        "Standard Maven artifact"
                ),
                new PackageTestCase(
                        "pkg:maven/com.google.guava/guava",
                        "https://mvnrepository.com/artifact/com.google.guava/guava",
                        "Google Guava Maven artifact"
                ),
                new PackageTestCase(
                        "pkg:maven/io.quarkus/quarkus-core",
                        "https://mvnrepository.com/artifact/io.quarkus/quarkus-core",
                        "Quarkus Maven artifact"
                ),

                // PyPI Cases
                new PackageTestCase(
                        "pkg:pypi/requests",
                        "https://pypi.org/project/requests",
                        "Simple PyPI package"
                ),
                new PackageTestCase(
                        "pkg:pypi/django",
                        "https://pypi.org/project/django",
                        "Django Python package"
                ),
                new PackageTestCase(
                        "pkg:pypi/python-dateutil",
                        "https://pypi.org/project/python-dateutil",
                        "PyPI package with hyphen"
                ),

                // Gem Cases
                new PackageTestCase(
                        "pkg:gem/rails",
                        "https://rubygems.org/gems/rails",
                        "Simple Ruby gem"
                ),
                new PackageTestCase(
                        "pkg:gem/activerecord",
                        "https://rubygems.org/gems/activerecord",
                        "ActiveRecord Ruby gem"
                ),
                new PackageTestCase(
                        "pkg:gem/devise",
                        "https://rubygems.org/gems/devise",
                        "Devise authentication gem"
                ),

                // Golang Cases
                new PackageTestCase(
                        "pkg:golang/golang.org/x/text",
                        "https://pkg.go.dev/golang.org/x/text",
                        "Official Go package"
                ),
                new PackageTestCase(
                        "pkg:golang/github.com/gin-gonic/gin",
                        "https://pkg.go.dev/github.com/gin-gonic/gin",
                        "Gin web framework"
                ),
                new PackageTestCase(
                        "pkg:golang/google.golang.org/grpc",
                        "https://pkg.go.dev/google.golang.org/grpc",
                        "gRPC Go package"
                ),

                // NuGet Cases
                new PackageTestCase(
                        "pkg:nuget/Newtonsoft.Json",
                        "https://www.nuget.org/packages/Newtonsoft.Json",
                        "Popular JSON library"
                ),
                new PackageTestCase(
                        "pkg:nuget/Microsoft.AspNetCore.App",
                        "https://www.nuget.org/packages/Microsoft.AspNetCore.App",
                        "Microsoft ASP.NET Core package"
                ),
                new PackageTestCase(
                        "pkg:nuget/Serilog.Sinks.Console",
                        "https://www.nuget.org/packages/Serilog.Sinks.Console",
                        "NuGet package with dots"
                )
        );

        // Run all test cases
        for (PackageTestCase tc : testCases) {
            log.info("Testing: {}", tc.description);

            PackageURL purl = new PackageURL(tc.purl);

            // Verify support
            assertTrue(
                    String.format("%s should be supported", tc.description),
                    Purl2Url.isSupported(purl)
            );

            // Generate URL
            String url = Purl2Url.convert(purl);

            // Verify
            assertNotNull(
                    String.format("Generated URL should not be null for %s", tc.description),
                    url
            );
            assertEquals(
                    String.format("URL should match expected for %s", tc.description),
                    tc.expectedUrl,
                    url
            );
        }

        log.info("Finished testAllPackageTypes -->");
    }

}