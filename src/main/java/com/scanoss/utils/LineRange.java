package com.scanoss.utils;

import lombok.Getter;

/**
 * Represents a line range with start and end lines
 */
@Getter
public class LineRange {
    private final int start;
    private final int end;

    public LineRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Checks if this interval overlaps with another interval
     */
    public boolean overlaps(LineRange other) {
        return this.start <= other.end && this.end >= other.start;
    }
}