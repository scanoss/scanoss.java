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


import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for handling line range operations
 */
@Slf4j
public class LineRangeUtils {
    /**
     * Parses a line range string into a list of intervals
     *
     * @param lineRanges String in format "1-5,7-10"
     * @return List of LineInterval objects
     */
    public static List<LineRange> parseLineRanges(String lineRanges) {
        if (lineRanges == null || lineRanges.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String[] ranges = lineRanges.split(",");
        List<LineRange> intervals = new ArrayList<>(ranges.length);

        for (String range : ranges) {
            String[] bounds = range.trim().split("-");
            if (bounds.length == 2) {
                try {
                    int start = Integer.parseInt(bounds[0].trim());
                    int end = Integer.parseInt(bounds[1].trim());
                    intervals.add(new LineRange(start, end));
                } catch (NumberFormatException e) {
                    // Skip invalid intervals
                    log.debug("Invalid interval format: {} in range {}", range, e.getMessage());
                }
            }
        }

        return intervals;
    }

    /**
     * Checks if two sets of line ranges overlap
     *
     * @param ranges1 First set of line ranges
     * @param ranges2 Second set of line ranges
     * @return true if any intervals overlap
     */
    public static boolean hasOverlappingRanges(@NotNull List<LineRange> ranges1, @NotNull List<LineRange> ranges2) {
        for (LineRange interval1 : ranges1) {
            for (LineRange interval2 : ranges2) {
                if (interval1.overlaps(interval2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a list of line ranges overlaps with a single range
     *
     * @param ranges List of line ranges to check against
     * @param range Single line range to check for overlap
     * @return true if any interval from the list overlaps with the given range
     * @throws NullPointerException if either parameter is null
     */
    public static boolean hasOverlappingRanges(@NotNull List<LineRange> ranges, @NotNull LineRange range) {
        for (LineRange interval1 : ranges) {
            if (interval1.overlaps(range)) {
                return true;
            }
        }
        return false;
    }
}