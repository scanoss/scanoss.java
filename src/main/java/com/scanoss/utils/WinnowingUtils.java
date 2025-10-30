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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SCANOSS Winnowing Utils Class
 * <p>
 *     This class provides methods for normalizing characters and other text processing utilities.
 * </p>
 */
public class WinnowingUtils {

    /**
     * Inner class to hold line ending detection results.
     */
    @Getter
    @AllArgsConstructor
    public static class LineEndingInfo {
        private final boolean hasCrlf;
        private final boolean hasStandaloneLf;
        private final boolean hasStandaloneCr;
    }

    /**
     * Normalise the given character
     *
     * @param c character to normalise
     * @return normalised character
     */
    public static char normalize(char c) {
        if (c < '0' || c > 'z') {
            return 0;
        } else if (c <= '9' || c >= 'a') {
            return c;
        } else if (c >= 'A' && c <= 'Z') {
            return (char) (c + 32);
        } else {
            return 0;
        }
    }


    /**
     * Extracts the first/primary file path from a WFP block.
     * This is a convenience method for single-file scenarios.
     *
     * @param wfpBlock the WFP block containing file entries
     * @return the first extracted file path, or null if none found
     */
    public static String extractFilePathFromWFPBlock(@NonNull String wfpBlock) {
        Set<String> paths = extractFilePathsFromWFPBlock(wfpBlock);
        return paths.isEmpty() ? null : paths.iterator().next();
    }


    /**
     * Extract all file paths from a multi-file WFP block using regex.
     * A multi-file WFP block contains multiple entries each starting with "file=".
     *
     * @param wfpBlock the WFP block containing multiple file entries
     * @return a Set of extracted file paths, empty if none found
     */
    public static Set<String> extractFilePathsFromWFPBlock(@NonNull String wfpBlock) {
        Set<String> paths = new HashSet<>();

        // Pattern to match file=<md5>,<size>,<path> format and capture the path
        // This regex matches: "file=" followed by any characters until a comma,
        // then any characters until another comma, then captures everything after that comma until end of line
        Pattern pattern = Pattern.compile("^file=[^,]+,[^,]+,(.+)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(wfpBlock);

        // Find all matches and add the captured paths to the result set
        while (matcher.find()) {
            String path = matcher.group(1);
            if (path != null && !path.isEmpty()) {
                paths.add(path);
            }
        }

        return paths;
    }

    /**
     * Calculate hash for contents with opposite line endings.
     * If the file is primarily Unix (LF), calculates Windows (CRLF) hash.
     * If the file is primarily Windows (CRLF), calculates Unix (LF) hash.
     *
     * @param contents File contents as bytes
     * @return Hash with opposite line endings as hex string, or null if no line endings detected
     */
    public static String calculateOppositeLineEndingHash(byte[] contents) {
        LineEndingInfo lineEndingInfo = detectLineEndings(contents);

        // If no line endings detected, return null
        if (!lineEndingInfo.hasCrlf && !lineEndingInfo.hasStandaloneLf && !lineEndingInfo.hasStandaloneCr) {
            return null;
        }

        // Normalize all line endings to LF first
        byte[] normalized = replaceSequence(contents, new byte[]{'\r', '\n'}, new byte[]{'\n'});
        normalized = replaceSequence(normalized, new byte[]{'\r'}, new byte[]{'\n'});

        byte[] oppositeContents;

        // Determine the dominant line ending type
        if (lineEndingInfo.hasCrlf && !lineEndingInfo.hasStandaloneLf && !lineEndingInfo.hasStandaloneCr) {
            // File is Windows (CRLF) - produce Unix (LF) hash
            oppositeContents = normalized;
        } else {
            // File is Unix (LF/CR) or mixed - produce Windows (CRLF) hash
            oppositeContents = replaceSequence(normalized, new byte[]{'\n'}, new byte[]{'\r', '\n'});
        }

        return DigestUtils.md5Hex(oppositeContents);
    }

    /**
     * Detect the types of line endings present in file contents.
     *
     * @param contents File contents as bytes
     * @return LineEndingInfo indicating which line ending types are present
     */
    private static LineEndingInfo detectLineEndings(byte[] contents) {
        // Check for CRLF (Windows line endings)
        boolean hasCrlf = containsSequence(contents, new byte[]{'\r', '\n'});

        // Remove all CRLF sequences to check for standalone LF and CR
        byte[] contentWithoutCrlf = replaceSequence(contents, new byte[]{'\r', '\n'}, new byte[]{});

        // Check for standalone LF (not part of CRLF)
        boolean hasStandaloneLf = containsSequence(contentWithoutCrlf, new byte[]{'\n'});

        // Check for standalone CR (not part of CRLF)
        boolean hasStandaloneCr = containsSequence(contentWithoutCrlf, new byte[]{'\r'});

        return new LineEndingInfo(hasCrlf, hasStandaloneLf, hasStandaloneCr);
    }

    /**
     * Check if a byte array contains a specific sequence of bytes.
     *
     * @param data     The byte array to search in
     * @param sequence The sequence to search for
     * @return true if the sequence is found, false otherwise
     */
    private static boolean containsSequence(byte[] data, byte[] sequence) {
        if (sequence.length == 0 || data.length < sequence.length) {
            return false;
        }

        for (int i = 0; i <= data.length - sequence.length; i++) {
            boolean found = true;
            for (int j = 0; j < sequence.length; j++) {
                if (data[i + j] != sequence[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return true;
            }
        }
        return false;
    }

    /**
     * Replace all occurrences of a byte sequence with another sequence.
     * Uses ByteArrayOutputStream for better performance compared to List<Byte>.
     *
     * @param data        The original byte array
     * @param search      The sequence to search for
     * @param replacement The sequence to replace with
     * @return A new byte array with replacements made
     */
    private static byte[] replaceSequence(byte[] data, byte[] search, byte[] replacement) {
        if (search.length == 0) {
            return data;
        }

        ByteArrayOutputStream result = new ByteArrayOutputStream(data.length);
        int i = 0;

        while (i < data.length) {
            boolean found = false;

            // Check if we have a match at current position
            if (i <= data.length - search.length) {
                found = true;
                for (int j = 0; j < search.length; j++) {
                    if (data[i + j] != search[j]) {
                        found = false;
                        break;
                    }
                }
            }

            if (found) {
                // Add replacement bytes
                result.write(replacement, 0, replacement.length);
                i += search.length;
            } else {
                // Add current byte
                result.write(data[i]);
                i++;
            }
        }

        return result.toByteArray();
    }
}
