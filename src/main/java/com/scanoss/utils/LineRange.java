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

import lombok.Getter;

/**
 * Represents a line range with start and end lines
 */
@Getter
public class LineRange {
    private final int start;
    private final int end;


    /**
     * Creates a new line range with the specified start and end lines.
     *
     * @param start the starting line number (inclusive)
     * @param end the ending line number (inclusive)
     */
    public LineRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Determines if this line range overlaps with another line range.
     * Two ranges overlap if any line numbers are shared between them.
     * For example:
     * - LineRange(1,5) overlaps with LineRange(3,7)
     * - LineRange(1,3) overlaps with LineRange(3,5)
     * - LineRange(1,3) does not overlap with LineRange(4,6)
     *
     * @param other the LineRange to check for overlap with this range
     * @return true if the ranges share any line numbers, false otherwise
     */
    public boolean overlaps(LineRange other) {
        return this.start <= other.end && this.end >= other.start;
    }
}