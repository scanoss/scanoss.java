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

import lombok.extern.slf4j.Slf4j;
import java.nio.file.Path;
import java.util.function.Predicate;


/**
 * An abstract base class that provides core filtering functionality for the SCANOSS filtering system.
 */
@Slf4j
public abstract class BaseFilter {
    protected final FilterConfig config;
    protected final GitIgnoreFilter gitIgnoreFilter;
    protected final AntFilter antFilter;
    protected final Predicate<Path> baseSkipFilter;

    /**
     * Constructs a BaseFilter with the specified configuration.
     * Initializes both Git ignore and Ant pattern filters, and combines them
     * into a single base skip filter using logical OR operation.
     *
     * @param config the configuration object containing filter patterns and settings
     */
    protected BaseFilter(FilterConfig config) {
        this.config = config;
        this.gitIgnoreFilter = GitIgnoreFilter.builder().patterns(config.getGitIgnorePatterns()).build();
        this.antFilter = AntFilter.builder().patterns(config.getAntPatterns()).build();
        this.baseSkipFilter = this.antFilter.get().or(this.gitIgnoreFilter.get());
    }
}
