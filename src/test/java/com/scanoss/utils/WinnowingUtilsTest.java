
package com.scanoss.utils;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class WinnowingUtilsTest {

    // Test file format: file=<md5>,<file_size>,<path>
    private static final String FILE1 = "file=90ebac4735d345fde0d05d939321d8fc,15878,/path/to/file1";
    private static final String FILE2 = "file=a7c31f87d23c42af732f57d39a9b05ac,24680,/path/to/file2";
    private static final String FILE3 = "file=e8585d8740d6664fda9e242a1d68b0f0,1815,/path/to/file3";
    private static final String FILE_SAME_PATH = "file=b1a89f4c5b0de974ad9846108c6d093a,9876,/path/to/file1";
    private static final String FILE_WITH_COMMA = "file=72a9e90d423b92dba36f78acc9bbecc7,12345,/path/with,comma";
    private static final String INVALID_FILE_NO_COMMAS = "file=invalid";

    // WFP hash entries
    private static final String WFP_ENTRY1 = "4=30777ca8,e9227657\n9=831bd2c5,701a2c74";
    private static final String WFP_ENTRY2 = "5=12345678,abcdefgh";

    @Test
    public void testExtractFilePathsFromWFPBlock_SingleFile_ReturnsSinglePath() {
        String wfpBlock = FILE1 + "\n" + WFP_ENTRY1;
        Set<String> result = WinnowingUtils.extractFilePathsFromWFPBlock(wfpBlock);
        assertEquals(1, result.size());
        assertTrue(result.contains("/path/to/file1"));
    }

    @Test
    public void testExtractFilePathsFromWFPBlock_MultipleFiles_ReturnsAllPaths() {
        String wfpBlock = FILE1 + "\n" + WFP_ENTRY1 + "\n" + FILE2 + "\n" + WFP_ENTRY2 + "\n" + FILE3 + "\n";

        Set<String> result = WinnowingUtils.extractFilePathsFromWFPBlock(wfpBlock);
        assertEquals(3, result.size());
        assertTrue(result.contains("/path/to/file1"));
        assertTrue(result.contains("/path/to/file2"));
        assertTrue(result.contains("/path/to/file3"));
    }

    @Test
    public void testExtractFilePathsFromWFPBlock_DuplicatePaths_ReturnsUniqueSet() {
        String wfpBlock = FILE1 + "\n" + WFP_ENTRY1 + "\n" + FILE_SAME_PATH + "\n" + WFP_ENTRY2 + "\n";

        Set<String> result = WinnowingUtils.extractFilePathsFromWFPBlock(wfpBlock);
        assertEquals(1, result.size());
        assertTrue(result.contains("/path/to/file1"));
    }

    @Test
    public void testExtractFilePathsFromWFPBlock_EmptyString_ReturnsEmptySet() {
        String wfpBlock = "";
        Set<String> result = WinnowingUtils.extractFilePathsFromWFPBlock(wfpBlock);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testExtractFilePathsFromWFPBlock_NoValidFileLines_ReturnsEmptySet() {
        String wfpBlock = "not_file=90ebac4735d345fde0d05d939321d8fc,15878,something\nanother=line\n";
        Set<String> result = WinnowingUtils.extractFilePathsFromWFPBlock(wfpBlock);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testExtractFilePathsFromWFPBlock_WithPathsContainingCommas_ParsesCorrectly() {
        String wfpBlock = FILE_WITH_COMMA + "\n" + WFP_ENTRY1 + "\n" + FILE2 + "\n";

        Set<String> result = WinnowingUtils.extractFilePathsFromWFPBlock(wfpBlock);
        assertEquals(2, result.size());
        assertTrue(result.contains("/path/with,comma"));
        assertTrue(result.contains("/path/to/file2"));
    }

    @Test
    public void testExtractFilePathsFromWFPBlock_ComplexCase_HandlesCorrectly() {
        String wfpBlock = "not_a_file=something\n" + FILE1 + "\n" + WFP_ENTRY1 + "\n" + INVALID_FILE_NO_COMMAS + "\n"
                + FILE2 + "\n" + WFP_ENTRY2 + "\n" + "random line\n" + FILE3 + "\n";

        Set<String> result = WinnowingUtils.extractFilePathsFromWFPBlock(wfpBlock);
        assertEquals(3, result.size());
        assertTrue(result.contains("/path/to/file1"));
        assertTrue(result.contains("/path/to/file2"));
        assertTrue(result.contains("/path/to/file3"));
    }
}
