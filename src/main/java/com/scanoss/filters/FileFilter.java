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
import com.scanoss.ScanossConstants;
import lombok.Builder;

import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * A concrete implementation of BaseFilter that provides specific file filtering functionality
 * for the SCANOSS scanning system. This filter combines multiple filtering strategies including
 * base patterns from BaseFilter, hidden file filtering, extension filtering, and specific file
 * name filtering.
 */
@Builder
public class FileFilter extends BaseFilter {

    @Builder.Default  // Tells Lombok this field has a default initialization
    private final FilterConfig filterConfig = FilterConfig.builder().build();

    // Use @Builder constructor annotation to tell Lombok to use this constructor
    protected FileFilter(FilterConfig filterConfig) {
        super(filterConfig);
        this.filterConfig = filterConfig;
    }

    /**
     * Creates a composite predicate that combines all file filtering rules based on the
     * current configuration settings and SCANOSS filtering constants.
     * <p>Each predicate in the chain is combined using logical OR operations, meaning a file
     * will be filtered if it matches any of the filtering rules. The filters are case-insensitive
     * for consistent matching across different operating systems.
     * @return a predicate that evaluates true if a path should be filtered according to any
     *         of the configured rules or SCANOSS constants
     * @see ScanossConstants#FILTERED_FILES for the list of specifically filtered filenames
     * @see ScanossConstants#FILTERED_EXTENSIONS for the list of filtered file extensions
     */
    public Predicate<Path> get() {
        // First call super's build to initialize base filters
        Predicate<Path> baseFilter = this.baseSkipFilter;

        if(config.getAllExtensions()){
            return p -> false;
        }

        if (!config.getHiddenFilesFolders()) {
            Predicate<Path> skipHiddenFilter = p -> p.getFileName().toString().startsWith(".");
            baseFilter = baseFilter
                    .or(skipHiddenFilter);
        }

        Predicate<Path> filterFiles = p -> {
            String fileNameToLower = p.getFileName().toString().toLowerCase();
            return ScanossConstants.FILTERED_FILES.stream()
                    .anyMatch(fileNameToLower::equals);
        };
        baseFilter = baseFilter.or(filterFiles);

        Predicate<Path> filterExtensions = p ->{
            String fileNameToLower = p.getFileName().toString().toLowerCase();
            return ScanossConstants.FILTERED_EXTENSIONS.stream()
                    .anyMatch(fileNameToLower::endsWith);
        };
        baseFilter = baseFilter.or(filterExtensions);

        return baseFilter;
    }
}