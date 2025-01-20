// SPDX-License-Identifier: MIT
/*
 * Copyright (c) 2024, SCANOSS
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

import com.github.packageurl.PackageURL;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

/**
 * Converts Package URLs (purls) to their corresponding browsable web URLs for
 * different package management systems and source code repositories.
 */
@Slf4j
public class Purl2Url {

    /**
     * PURL type to URL enum
     */
    @Getter
    public enum PurlType {
        /**
         * GitHub URL
         */
        GITHUB("github", "https://github.com/%s"),
        /**
         * Node URL
         */
        NPM("npm", "https://www.npmjs.com/package/%s"),
        /**
         * Maven Central URL
         */
        MAVEN("maven", "https://mvnrepository.com/artifact/%s"),
        /**
         * Ruby Gems URL
         */
        GEM("gem", "https://rubygems.org/gems/%s"),
        /**
         * Python PyPI URL
         */
        PYPI("pypi", "https://pypi.org/project/%s"),
        /**
         * Golang URL
         */
        GOLANG("golang", "https://pkg.go.dev/%s"),
        /**
         * MS Nuget URL
         */
        NUGET("nuget", "https://www.nuget.org/packages/%s");

        private final String type;
        private final String urlPattern;

        /**
         * Setup PURL type/URL enum
         *
         * @param type PURL Type
         * @param urlPattern URL pattern
         */
        PurlType(String type, String urlPattern) {
            this.type = type;
            this.urlPattern = urlPattern;
        }
    }

    /**
     * Checks if the given PackageURL is supported for conversion.
     *
     * @param purl The PackageURL to check
     * @return <code>true</code> if the PackageURL can be converted to a browsable URL
     */
    public static boolean isSupported(@NonNull PackageURL purl) {
        try {
            findPurlType(purl.getType());
            return true;
        } catch (RuntimeException e) {
            log.warn("Failed to find PURL type {} from {}: {}", purl.getType(), purl, e.getLocalizedMessage());
        }
        return false;
    }

    /**
     * Converts a PackageURL to its browsable web URL.
     * Returns null if the conversion is not possible.
     *
     * @param purl The PackageURL to convert
     * @return The browsable web URL or <code>null</code> if conversion fails
     */
    @Nullable
    public static String convert(@NonNull PackageURL purl) {
        try {
            PurlType purlType = findPurlType(purl.getType());
            String nameSpace = purl.getNamespace();
            String fullName = nameSpace != null ? nameSpace + "/" + purl.getName() : purl.getName();
            return String.format(purlType.getUrlPattern(), fullName);
        } catch (RuntimeException e) {
            log.debug("Failed to convert purl to URL for {}: {}", purl, e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Determine if we have a supported PURL Type or not
     *
     * @param type PURL type string
     * @return Supported PURL Type
     * @throws IllegalArgumentException if type cannot be found on supported list
     */
    private static PurlType findPurlType(@NonNull String type) throws IllegalArgumentException {
        for (PurlType purlType : PurlType.values()) {
            if (purlType.getType().equals(type)) {
                return purlType;
            }
        }
        throw new IllegalArgumentException(String.format("Unsupported package type: %s", type));
    }
}