package com.scanoss.utils;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for handling line range operations
 */
public class LineRangeUtils {
    /**
     * Parses a line range string into a list of intervals
     * @param lineRanges String in format "1-5,7-10"
     * @return List of LineInterval objects
     */
    public static List<LineRange> parseLineRanges(String lineRanges) {
        if (lineRanges == null || lineRanges.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<LineRange> intervals = new ArrayList<>();
        String[] ranges = lineRanges.split(",");

        for (String range : ranges) {
            String[] bounds = range.trim().split("-");
            if (bounds.length == 2) {
                try {
                    int start = Integer.parseInt(bounds[0].trim());
                    int end = Integer.parseInt(bounds[1].trim());
                    intervals.add(new LineRange(start, end));
                } catch (NumberFormatException e) {
                    // Skip invalid intervals
                    continue;
                }
            }
        }

        return intervals;
    }

    /**
     * Checks if two sets of line ranges overlap
     * @param ranges1 First set of line ranges
     * @param ranges2 Second set of line ranges
     * @return true if any intervals overlap
     */
    public static boolean hasOverlappingRanges(List<LineRange> ranges1, List<LineRange> ranges2) {
        for (LineRange interval1 : ranges1) {
            for (LineRange interval2 : ranges2) {
                if (interval1.overlaps(interval2)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasOverlappingRanges(List<LineRange> ranges, LineRange range) {
        for (LineRange interval1 : ranges) {
                if (interval1.overlaps(range)) {
                    return true;
                }
        }
        return false;
    }
}