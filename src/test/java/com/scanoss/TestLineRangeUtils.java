package com.scanoss;

import com.scanoss.utils.LineRange;
import com.scanoss.utils.LineRangeUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

@Slf4j
public class TestLineRangeUtils {

    @Before
    public void Setup() {
        log.info("Starting Line Range Utils test cases...");
        log.debug("Logging debug enabled");
        log.trace("Logging trace enabled");
    }

    @Test
    public void testSingleRangeOverlap() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        LineRange range1 = new LineRange(1, 10);
        LineRange range2 = new LineRange(5, 15);

        assertTrue("Overlapping ranges should return true", range1.overlaps(range2));
        assertTrue("Overlap should be commutative", range2.overlaps(range1));

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void testNonOverlappingRanges() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        LineRange range1 = new LineRange(1, 5);
        LineRange range2 = new LineRange(6, 10);

        assertFalse("Non-overlapping ranges should return false", range1.overlaps(range2));
        assertFalse("Non-overlap should be commutative", range2.overlaps(range1));

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void testAdjacentRanges() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        LineRange range1 = new LineRange(1, 5);
        LineRange range2 = new LineRange(5, 10);

        assertTrue("Adjacent ranges should be considered overlapping", range1.overlaps(range2));
        assertTrue("Adjacent overlap should be commutative", range2.overlaps(range1));

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void testParseValidLineRanges() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        String rangesStr = "11-52,81-123";
        List<LineRange> ranges = LineRangeUtils.parseLineRanges(rangesStr);

        assertEquals("Should parse two ranges", 2, ranges.size());
        assertEquals("First range should start at 11", 11, ranges.get(0).getStart());
        assertEquals("First range should end at 52", 52, ranges.get(0).getEnd());
        assertEquals("Second range should start at 81", 81, ranges.get(1).getStart());
        assertEquals("Second range should end at 123", 123, ranges.get(1).getEnd());

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void testParseEmptyInput() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        List<LineRange> ranges = LineRangeUtils.parseLineRanges("");
        assertTrue("Empty input should return empty list", ranges.isEmpty());

        ranges = LineRangeUtils.parseLineRanges(null);
        assertTrue("Null input should return empty list", ranges.isEmpty());

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void testParseInvalidFormat() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        List<LineRange> ranges = LineRangeUtils.parseLineRanges("11-52-81");
        assertTrue("Invalid format should be skipped", ranges.isEmpty());

        ranges = LineRangeUtils.parseLineRanges(",,,");
        assertTrue("Invalid format should be skipped", ranges.isEmpty());

        ranges = LineRangeUtils.parseLineRanges("abc-def");
        assertTrue("Non-numeric ranges should be skipped", ranges.isEmpty());

        ranges = LineRangeUtils.parseLineRanges("11-52,invalid,81-123");
        assertEquals("Should parse valid ranges and skip invalid ones", 2, ranges.size());

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void testHasOverlappingRanges() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        String ranges1Str = "1-10,20-30";
        String ranges2Str = "5-15,25-35";

        List<LineRange> ranges1 = LineRangeUtils.parseLineRanges(ranges1Str);
        List<LineRange> ranges2 = LineRangeUtils.parseLineRanges(ranges2Str);

        assertTrue("Should detect overlapping ranges",
                LineRangeUtils.hasOverlappingRanges(ranges1, ranges2));

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void testNoOverlappingRanges() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        String ranges1Str = "1-10,20-30";
        String ranges2Str = "40-50,60-70";

        List<LineRange> ranges1 = LineRangeUtils.parseLineRanges(ranges1Str);
        List<LineRange> ranges2 = LineRangeUtils.parseLineRanges(ranges2Str);

        assertFalse("Should not detect overlapping ranges",
                LineRangeUtils.hasOverlappingRanges(ranges1, ranges2));

        log.info("Finished {} -->", methodName);
    }

    @Test
    public void testSingleLineRanges() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        String rangesStr = "5-5,10-10";
        List<LineRange> ranges = LineRangeUtils.parseLineRanges(rangesStr);

        assertEquals("Should parse two single-line ranges", 2, ranges.size());
        assertEquals("First range should be single line",
                ranges.get(0).getStart(), ranges.get(0).getEnd());
        assertEquals("Second range should be single line",
                ranges.get(1).getStart(), ranges.get(1).getEnd());

        log.info("Finished {} -->", methodName);
    }
}