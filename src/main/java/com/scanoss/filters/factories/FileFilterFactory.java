// SPDX-License-Identifier: MIT
/*
 * Copyright (c) 2025, SCANOSS
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
package com.scanoss.filters.factories;

import com.scanoss.filters.FileFilter;
import com.scanoss.filters.FilterConfig;
import java.nio.file.Path;
import java.util.function.Predicate;

/**
A factory class that provides a static method for creating file filtering predicates.
*/
 public class FileFilterFactory {
    /**
     * Creates a file filtering predicate based on the provided configuration.
     *
     * <p>This method uses the builder pattern through {@link FileFilter} to construct
     * a predicate that can be used to filter file paths. The filtering criteria are
     * determined by the settings in the provided configuration object.
     *
     * @param config the configuration object containing filter settings and criteria
     * @return a predicate that evaluates file paths according to the specified configuration
     * @throws NullPointerException if the config parameter is null
     * @see FileFilter
     * @see FilterConfig
     */
    public static Predicate<Path> build(FilterConfig config) {  // Static factory method
        return FileFilter.builder().filterConfig(config).build().get();
    }
}
