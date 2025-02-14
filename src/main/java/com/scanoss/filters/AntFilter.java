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
package com.scanoss.filters;

import lombok.Builder;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.jetbrains.annotations.NotNull;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

/**
 * A filter implementation that uses Ant-style pattern matching for file paths.
 * This class provides functionality to match file paths against a list of Ant patterns,
 * using the SelectorUtils for pattern matching operations.
 *
 * <p>The class supports multiple patterns and returns a predicate that can be used
 * to test if a given path matches any of the specified patterns.
 *
 */
public class AntFilter {
    List<String> patterns;

    /**
     * Constructs an AntFilter with the specified list of patterns.
     *
     * @param patterns a non-null list of Ant-style patterns to match against paths
     */
    @Builder
    public AntFilter(@NotNull List<String> patterns) {
        this.patterns = patterns;
    }

    /**
     * Creates a predicate that tests if a path matches any of the specified patterns.
     * @return a predicate with the specified patterns
     */
    public Predicate<Path> get() {
        return p -> patterns.stream().anyMatch(
                pattern ->
                        SelectorUtils.matchPath(pattern, p.toString()));
    }

}
