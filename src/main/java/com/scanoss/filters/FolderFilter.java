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

import java.nio.file.Path;
import java.util.function.Predicate;
import static com.scanoss.ScanossConstants.FILTERED_DIRS;
import static com.scanoss.ScanossConstants.FILTERED_DIR_EXT;

/**
 * A concrete implementation of BaseFilter that provides directory-specific filtering functionality
 * for the SCANOSS scanning system. This filter specializes in filtering folders based on
 * configured patterns, hidden folder rules, and predefined directory filters.
 */
public class FolderFilter extends BaseFilter {

    public FolderFilter(FilterConfig filterConfig) {
        super(filterConfig);
    }

    /**
     * Creates a composite predicate that combines all folder filtering rules based on
     * the current configuration settings.
     *
     * <p>The method constructs a chain of predicates that:
     * <ul>
     *   <li>Applies base filters from BaseFilter
     *   <li>Optionally adds hidden folder filtering (excluding the current directory)
     *   <li>Optionally adds filtering for specific directory names and extensions
     * </ul>
     *
     * <p>The filter chain is constructed using logical OR operations for exclusion patterns
     * and AND operations for inclusion patterns.
     *
     * @return a predicate that combines all configured folder filtering rules
     * @see ScanossConstants#FILTERED_DIRS
     * @see ScanossConstants#FILTERED_DIR_EXT
     */
    public Predicate<Path> get() {
        // First call super's build to initialize base filters
        Predicate<Path> baseFilter = this.baseSkipFilter;

        if (!config.getHiddenFilesFolders()) {
            Predicate<Path> skipHiddenFilter = p -> p.getFileName().toString().startsWith(".");
            Predicate<Path> notSkipCurrentDirectoryFilter = p -> !p.getFileName().toString().equals(".");

            baseFilter = baseFilter
                    .or(skipHiddenFilter)
                    .and(notSkipCurrentDirectoryFilter);
        }

        if (!config.getAllFolders()) {
            Predicate<Path> filterDirs = p -> FILTERED_DIRS.stream()
                    .anyMatch(d -> p.getFileName().toString().toLowerCase().endsWith(d));
            Predicate<Path> filterDirExt = p -> FILTERED_DIR_EXT.stream()
                    .anyMatch(d -> p.getFileName().toString().toLowerCase().endsWith(d));

            baseFilter = baseFilter.or(filterDirs).or(filterDirExt);
        }

        return baseFilter;
    }
}
