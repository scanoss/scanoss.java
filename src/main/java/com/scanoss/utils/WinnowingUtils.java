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

import org.jetbrains.annotations.NotNull;

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
    public static String extractFilePathFromWFPBlock(@NotNull String wfpBlock) {
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
    public static Set<String> extractFilePathsFromWFPBlock(@NotNull String wfpBlock) {
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
}
