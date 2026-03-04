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
package com.scanoss;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Source code header filter that identifies where real implementation begins.
 * <p>
 * Analyzes source code files and determines which lines at the beginning are:
 * license headers, documentation comments, imports/includes, or blank lines.
 * Returns the line offset where actual implementation code starts.
 * </p>
 */
@Slf4j
public class HeaderFilter {

    private static final int LICENSE_HEADER_MAX_LINES = 50;
    private static final int COMPLETE_DOCSTRING_QUOTE_COUNT = 2;

    // Comment patterns by style
    private static final Map<String, CommentPatterns> COMMENT_PATTERNS = new HashMap<>();
    // Import patterns by language
    private static final Map<String, List<Pattern>> IMPORT_PATTERNS = new HashMap<>();
    // File extension to language mapping
    private static final Map<String, String> EXT_MAP = new HashMap<>();
    // License keywords
    private static final List<String> LICENSE_KEYWORDS = Arrays.asList(
            "copyright", "license", "licensed", "all rights reserved",
            "permission", "redistribution", "warranty", "liability",
            "apache", "mit", "gpl", "bsd", "mozilla", "author:",
            "spdx-license", "contributors", "licensee"
    );

    // Language to comment style mapping
    private static final Map<String, String> LANGUAGE_COMMENT_STYLE = new HashMap<>();

    static {
        initCommentPatterns();
        initImportPatterns();
        initExtMap();
        initLanguageCommentStyles();
    }

    private final Integer maxLines;

    /**
     * Create a HeaderFilter with no line limit.
     */
    public HeaderFilter() {
        this.maxLines = null;
    }

    /**
     * Create a HeaderFilter with an optional maximum number of lines to skip.
     *
     * @param skipLimit maximum lines to skip (null or 0 = unlimited)
     */
    public HeaderFilter(Integer skipLimit) {
        this.maxLines = (skipLimit != null && skipLimit > 0) ? skipLimit : null;
    }

    /**
     * Filter file content and return the number of header lines to skip.
     *
     * @param file            file path (used to detect language from extension)
     * @param decodedContents file contents as string
     * @return number of lines to skip from the beginning (0 if no filtering)
     */
    public int filter(String file, String decodedContents) {
        if (decodedContents == null || decodedContents.isEmpty() || file == null || file.isEmpty()) {
            log.debug("No file or contents provided, skipping header filter");
            return 0;
        }
        String language = detectLanguage(file);
        if (language == null) {
            log.debug("Skipping header filter for unsupported language: {}", file);
            return 0;
        }

        String[] lines = decodedContents.split("\n", -1);
        if (lines.length == 0) {
            log.debug("No lines in file: {}", file);
            return 0;
        }
        log.trace("Analysing {} lines for file: {}", lines.length, file);

        Integer implementationStart = findFirstImplementationLine(lines, language);
        if (implementationStart == null) {
            log.debug("No implementation found in file: {}", file);
            return 0;
        }

        int lineOffset = implementationStart - 1;

        if (maxLines != null && maxLines > 0 && maxLines < lineOffset) {
            log.trace("Line offset {} exceeds maxLines {}, capping at {} for: {}",
                    lineOffset, maxLines, maxLines, file);
            lineOffset = maxLines;
        }

        if (lineOffset > 0) {
            log.debug("Filtered out {} lines from beginning of {} (language: {})", lineOffset, file, language);
        }
        return lineOffset;
    }

    /**
     * Detect programming language from file extension.
     *
     * @param filePath file path
     * @return language string or null if unsupported
     */
    String detectLanguage(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filePath.length() - 1) {
            return null;
        }
        String extension = filePath.substring(dotIndex).toLowerCase();
        return EXT_MAP.get(extension);
    }

    /**
     * Find the 1-indexed line number where implementation begins.
     *
     * @param lines    array of source lines
     * @param language detected programming language
     * @return 1-indexed line number, or null if no implementation found
     */
    Integer findFirstImplementationLine(String[] lines, String language) {
        if (lines == null || lines.length == 0 || language == null) {
            return null;
        }

        boolean inMultilineComment = false;
        boolean inLicenseSection = false;
        boolean inImportBlock = false;

        String commentStyle = getCommentStyle(language);
        CommentPatterns commentPatterns = COMMENT_PATTERNS.get(commentStyle);
        List<Pattern> importPatterns = IMPORT_PATTERNS.getOrDefault(language, Collections.emptyList());

        for (int i = 0; i < lines.length; i++) {
            int lineNumber = i + 1;
            String stripped = lines[i].trim();

            // Shebang (only first line) or blank line
            if ((i == 0 && stripped.startsWith("#!")) || stripped.isEmpty()) {
                continue;
            }

            // Check if it's a comment
            CommentResult commentResult = isComment(lines[i], inMultilineComment, commentPatterns);
            inMultilineComment = commentResult.inMultiline;

            if (commentResult.isComment) {
                if (isLicenseHeader(lines[i])) {
                    if (!inLicenseSection) {
                        log.trace("Line {}: Detected license header section", lineNumber);
                    }
                    inLicenseSection = true;
                } else if (inLicenseSection && lineNumber < LICENSE_HEADER_MAX_LINES) {
                    // Still in the license section. Keep looking.
                } else {
                    if (inLicenseSection) {
                        log.trace("Line {}: End of license header section", lineNumber);
                    }
                    inLicenseSection = false;
                }
                continue;
            }

            // If not a comment, end license section
            inLicenseSection = false;

            // If we're inside a multi-line import block, continue until closing delimiter
            if (inImportBlock) {
                if (stripped.contains(")") || stripped.contains("}")) {
                    log.trace("Line {}: Multi-line import block end", lineNumber);
                    inImportBlock = false;
                }
                continue;
            }

            // Check if it's an import
            if (isImport(lines[i], importPatterns)) {
                // Detect start of multi-line import block (e.g. "from x import (", "import {")
                if ((stripped.contains("(") && !stripped.contains(")")) ||
                        (stripped.contains("{") && !stripped.contains("}"))) {
                    log.trace("Line {}: Multi-line import block start", lineNumber);
                    inImportBlock = true;
                }
                continue;
            }

            // If we get here, it's implementation code
            log.trace("Line {}: First implementation line detected", lineNumber);
            return lineNumber;
        }
        return null;
    }

    /**
     * Check if a line is a comment and track multiline comment state.
     *
     * @param line             the source line
     * @param inMultiline      whether we're currently inside a multiline comment
     * @param commentPatterns  comment patterns for the current language style
     * @return result containing whether the line is a comment and the new multiline state
     */
    private CommentResult isComment(String line, boolean inMultiline, CommentPatterns commentPatterns) {
        if (commentPatterns == null) {
            return new CommentResult(false, inMultiline);
        }

        // If we're in a multiline comment
        if (inMultiline) {
            if (commentPatterns.multiEnd != null && commentPatterns.multiEnd.matcher(line).find()) {
                return new CommentResult(true, false);
            }
            if (commentPatterns.docStringEnd != null && commentPatterns.docStringEnd.matcher(line).find()) {
                return new CommentResult(true, false);
            }
            return new CommentResult(true, true);
        }

        // Single-line comment
        if (commentPatterns.singleLine != null && commentPatterns.singleLine.matcher(line).matches()) {
            return new CommentResult(true, false);
        }

        // Multiline comment complete in one line
        if (commentPatterns.multiSingle != null && commentPatterns.multiSingle.matcher(line).matches()) {
            return new CommentResult(true, false);
        }

        // Start of multiline comment (C-style)
        if (commentPatterns.multiStart != null && commentPatterns.multiStart.matcher(line).find()) {
            if (commentPatterns.multiEnd != null && commentPatterns.multiEnd.matcher(line).find()) {
                return new CommentResult(true, false);
            }
            return new CommentResult(true, true);
        }

        // Start of docstring (Python)
        if (commentPatterns.docStringStart != null && line.contains("\"\"\"")) {
            int count = countOccurrences(line, "\"\"\"");
            if (count == COMPLETE_DOCSTRING_QUOTE_COUNT) {
                return new CommentResult(true, false);
            }
            if (count == 1) {
                return new CommentResult(true, true);
            }
        }

        return new CommentResult(false, inMultiline);
    }

    /**
     * Check if a line matches any import pattern for the language.
     *
     * @param line    source line
     * @param patterns import patterns for the language
     * @return true if the line is an import/include statement
     */
    private boolean isImport(String line, List<Pattern> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        return patterns.stream().anyMatch(p -> p.matcher(line).find());
    }

    /**
     * Check if a line contains license-related keywords.
     *
     * @param line source line
     * @return true if the line appears to be part of a license header
     */
    boolean isLicenseHeader(String line) {
        String lineLower = line.toLowerCase();
        return LICENSE_KEYWORDS.stream().anyMatch(lineLower::contains);
    }

    /**
     * Get the comment style for a given language.
     *
     * @param language programming language
     * @return comment style key
     */
    private String getCommentStyle(String language) {
        return LANGUAGE_COMMENT_STYLE.getOrDefault(language, "c_style");
    }

    /**
     * Count occurrences of a substring in a string.
     */
    private int countOccurrences(String str, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    // --- Static initialization methods ---

    private static void initCommentPatterns() {
        // C-style: C, C++, Java, JavaScript, TypeScript, Go, Rust, C#, PHP, Kotlin, Scala, Dart
        COMMENT_PATTERNS.put("c_style", new CommentPatterns(
                Pattern.compile("^\\s*//.*$"),
                Pattern.compile("^\\s*/\\*"),
                Pattern.compile("\\*/\\s*$"),
                Pattern.compile("^\\s*/\\*.*\\*/\\s*$"),
                null, null
        ));

        // Python, shell scripts, Ruby, Perl, R
        COMMENT_PATTERNS.put("python_style", new CommentPatterns(
                Pattern.compile("^\\s*#.*$"),
                null, null, null,
                Pattern.compile("^\\s*\"\"\""),
                Pattern.compile("\"\"\"\\s*$")
        ));

        // Lua, Haskell
        COMMENT_PATTERNS.put("lua_style", new CommentPatterns(
                Pattern.compile("^\\s*--.*$"),
                Pattern.compile("^\\s*--\\[\\["),
                Pattern.compile("\\]\\]\\s*$"),
                null, null, null
        ));

        // HTML, XML
        COMMENT_PATTERNS.put("html_style", new CommentPatterns(
                null,
                Pattern.compile("^\\s*<!--"),
                Pattern.compile("-->\\s*$"),
                Pattern.compile("^\\s*<!--.*-->\\s*$"),
                null, null
        ));
    }

    private static void initImportPatterns() {
        IMPORT_PATTERNS.put("python", compilePatterns(
                "^\\s*import\\s+",
                "^\\s*from\\s+.*\\s+import\\s+"
        ));
        IMPORT_PATTERNS.put("javascript", compilePatterns(
                "^\\s*import\\s+.*\\s+from\\s+",
                "^\\s*import\\s+[\"']",
                "^\\s*import\\s+type\\s+",
                "^\\s*export\\s+\\*\\s+from\\s+",
                "^\\s*export\\s+\\{.*\\}\\s+from\\s+",
                "^\\s*const\\s+.*\\s*=\\s*require\\(",
                "^\\s*var\\s+.*\\s*=\\s*require\\(",
                "^\\s*let\\s+.*\\s*=\\s*require\\("
        ));
        IMPORT_PATTERNS.put("typescript", compilePatterns(
                "^\\s*import\\s+",
                "^\\s*export\\s+.*\\s+from\\s+",
                "^\\s*import\\s+type\\s+",
                "^\\s*import\\s+\\{.*\\}\\s+from\\s+"
        ));
        IMPORT_PATTERNS.put("java", compilePatterns(
                "^\\s*import\\s+",
                "^\\s*package\\s+"
        ));
        IMPORT_PATTERNS.put("kotlin", compilePatterns(
                "^\\s*import\\s+",
                "^\\s*package\\s+"
        ));
        IMPORT_PATTERNS.put("scala", compilePatterns(
                "^\\s*import\\s+",
                "^\\s*package\\s+"
        ));
        IMPORT_PATTERNS.put("go", compilePatterns(
                "^\\s*import\\s+\\(",
                "^\\s*import\\s+\"",
                "^\\s*package\\s+",
                "^\\s*\"[^\"]*\"\\s*$",
                "^\\s*[a-zA-Z_][a-zA-Z0-9_]*\\s+\"[^\"]*\"\\s*$",
                "^\\s*_\\s+\"[^\"]*\"\\s*$"
        ));
        IMPORT_PATTERNS.put("rust", compilePatterns(
                "^\\s*use\\s+",
                "^\\s*extern\\s+crate\\s+",
                "^\\s*mod\\s+"
        ));
        IMPORT_PATTERNS.put("cpp", compilePatterns(
                "^\\s*#include\\s+",
                "^\\s*#pragma\\s+",
                "^\\s*#ifndef\\s+.*_H.*",
                "^\\s*#define\\s+.*_H.*",
                "^\\s*#endif\\s+(//.*)?(\\s*)$"
        ));
        IMPORT_PATTERNS.put("csharp", compilePatterns(
                "^\\s*using\\s+",
                "^\\s*namespace\\s+"
        ));
        IMPORT_PATTERNS.put("php", compilePatterns(
                "^\\s*use\\s+",
                "^\\s*require\\s+",
                "^\\s*require_once\\s+",
                "^\\s*include\\s+",
                "^\\s*include_once\\s+",
                "^\\s*namespace\\s+"
        ));
        IMPORT_PATTERNS.put("swift", compilePatterns(
                "^\\s*import\\s+"
        ));
        IMPORT_PATTERNS.put("ruby", compilePatterns(
                "^\\s*require\\s+",
                "^\\s*require_relative\\s+",
                "^\\s*load\\s+"
        ));
        IMPORT_PATTERNS.put("perl", compilePatterns(
                "^\\s*use\\s+",
                "^\\s*require\\s+"
        ));
        IMPORT_PATTERNS.put("r", compilePatterns(
                "^\\s*library\\(",
                "^\\s*require\\(",
                "^\\s*source\\("
        ));
        IMPORT_PATTERNS.put("lua", compilePatterns(
                "^\\s*require\\s+",
                "^\\s*local\\s+.*\\s*=\\s*require\\("
        ));
        IMPORT_PATTERNS.put("dart", compilePatterns(
                "^\\s*import\\s+",
                "^\\s*export\\s+",
                "^\\s*part\\s+"
        ));
        IMPORT_PATTERNS.put("haskell", compilePatterns(
                "^\\s*import\\s+",
                "^\\s*module\\s+"
        ));
        IMPORT_PATTERNS.put("elixir", compilePatterns(
                "^\\s*import\\s+",
                "^\\s*alias\\s+",
                "^\\s*require\\s+",
                "^\\s*use\\s+"
        ));
        IMPORT_PATTERNS.put("clojure", compilePatterns(
                "^\\s*\\(\\s*ns\\s+",
                "^\\s*\\(\\s*require\\s+",
                "^\\s*\\(\\s*import\\s+"
        ));
    }

    private static void initExtMap() {
        EXT_MAP.put(".py", "python");
        EXT_MAP.put(".js", "javascript");
        EXT_MAP.put(".mjs", "javascript");
        EXT_MAP.put(".cjs", "javascript");
        EXT_MAP.put(".ts", "typescript");
        EXT_MAP.put(".tsx", "typescript");
        EXT_MAP.put(".jsx", "javascript");
        EXT_MAP.put(".java", "java");
        EXT_MAP.put(".kt", "kotlin");
        EXT_MAP.put(".kts", "kotlin");
        EXT_MAP.put(".scala", "scala");
        EXT_MAP.put(".sc", "scala");
        EXT_MAP.put(".go", "go");
        EXT_MAP.put(".rs", "rust");
        EXT_MAP.put(".cpp", "cpp");
        EXT_MAP.put(".cc", "cpp");
        EXT_MAP.put(".cxx", "cpp");
        EXT_MAP.put(".c", "cpp");
        EXT_MAP.put(".h", "cpp");
        EXT_MAP.put(".hpp", "cpp");
        EXT_MAP.put(".hxx", "cpp");
        EXT_MAP.put(".cs", "csharp");
        EXT_MAP.put(".php", "php");
        EXT_MAP.put(".swift", "swift");
        EXT_MAP.put(".rb", "ruby");
        EXT_MAP.put(".pl", "perl");
        EXT_MAP.put(".pm", "perl");
        EXT_MAP.put(".r", "r");
        EXT_MAP.put(".lua", "lua");
        EXT_MAP.put(".dart", "dart");
        EXT_MAP.put(".hs", "haskell");
        EXT_MAP.put(".ex", "elixir");
        EXT_MAP.put(".exs", "elixir");
        EXT_MAP.put(".clj", "clojure");
        EXT_MAP.put(".cljs", "clojure");
        EXT_MAP.put(".m", "cpp");
        EXT_MAP.put(".mm", "cpp");
        EXT_MAP.put(".sh", "python");
        EXT_MAP.put(".bash", "python");
        EXT_MAP.put(".zsh", "python");
        EXT_MAP.put(".fish", "python");
    }

    private static void initLanguageCommentStyles() {
        for (String lang : new String[]{"cpp", "java", "kotlin", "scala", "javascript", "typescript",
                "go", "rust", "csharp", "php", "swift", "dart"}) {
            LANGUAGE_COMMENT_STYLE.put(lang, "c_style");
        }
        for (String lang : new String[]{"python", "ruby", "perl", "r"}) {
            LANGUAGE_COMMENT_STYLE.put(lang, "python_style");
        }
        for (String lang : new String[]{"lua", "haskell"}) {
            LANGUAGE_COMMENT_STYLE.put(lang, "lua_style");
        }
    }

    private static List<Pattern> compilePatterns(String... regexes) {
        List<Pattern> patterns = new ArrayList<>();
        for (String regex : regexes) {
            patterns.add(Pattern.compile(regex));
        }
        return patterns;
    }

    /**
     * Internal record for comment detection results.
     */
    private static class CommentResult {
        final boolean isComment;
        final boolean inMultiline;

        CommentResult(boolean isComment, boolean inMultiline) {
            this.isComment = isComment;
            this.inMultiline = inMultiline;
        }
    }

    /**
     * Comment patterns for a given language style.
     */
    private static class CommentPatterns {
        final Pattern singleLine;
        final Pattern multiStart;
        final Pattern multiEnd;
        final Pattern multiSingle;
        final Pattern docStringStart;
        final Pattern docStringEnd;

        CommentPatterns(Pattern singleLine, Pattern multiStart, Pattern multiEnd,
                        Pattern multiSingle, Pattern docStringStart, Pattern docStringEnd) {
            this.singleLine = singleLine;
            this.multiStart = multiStart;
            this.multiEnd = multiEnd;
            this.multiSingle = multiSingle;
            this.docStringStart = docStringStart;
            this.docStringEnd = docStringEnd;
        }
    }
}