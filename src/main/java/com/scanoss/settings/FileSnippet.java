// SPDX-License-Identifier: MIT
/*
 * Copyright (c) 2026, SCANOSS
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
package com.scanoss.settings;

import com.google.gson.annotations.SerializedName;
import lombok.*;

/**
 * Scan configuration parameters for engine tuning.
 * <p>
 * Holds parameters that control how the SCANOSS scanning engine processes files.
 * Used both for JSON deserialization from scanoss.json and for resolved CLI configuration.
 * </p>
 * Resolution priority (highest to lowest):
 * <ol>
 *   <li>settings.file_snippet section in scanoss.json</li>
 *   <li>CLI arguments</li>
 * </ol>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSnippet {

    @Builder.Default
    @SerializedName("min_snippet_hits")
    private Integer minSnippetHits = 0;

    @Builder.Default
    @SerializedName("min_snippet_lines")
    private Integer minSnippetLines = 0;

    @SerializedName("honour_file_exts")
    private Boolean honourFileExts;

    @SerializedName("ranking_enabled")
    private Boolean rankingEnabled;

    @Builder.Default
    @SerializedName("ranking_threshold")
    private Integer rankingThreshold = -1;

    @Builder.Default
    @SerializedName("skip_headers")
    private Boolean skipHeaders = false;

    @Builder.Default
    @SerializedName("skip_headers_limit")
    private Integer skipHeadersLimit = 0;

    public boolean isMinSnippetHitsSet() {
        return minSnippetHits != null && minSnippetHits > 0;
    }

    public boolean isMinSnippetLinesSet() {
        return minSnippetLines != null && minSnippetLines > 0;
    }

    public boolean isHonourFileExtsSet() {
        return honourFileExts != null;
    }

    public boolean isRankingEnabledSet() {
        return rankingEnabled != null;
    }

    public boolean isRankingThresholdSet() {
        return rankingThreshold != null && rankingThreshold >= 0;
    }

    public boolean isSkipHeadersLimitSet() {
        return skipHeadersLimit != null && skipHeadersLimit > 0;
    }

    /**
     * Resolves scan configuration by merging two priority layers.
     * Priority: fileSnippet (highest) &gt; cli (lowest).
     * <p>
     * When a setting is "unset" at a given level, it is not applied, allowing
     * lower-priority levels to provide the value.
     * </p>
     *
     * @param cli            CLI-provided config (lowest priority)
     * @param fileSnippet    File snippet config from scanoss.json (highest priority)
     * @return Resolved ScanConfig with highest-priority non-unset values
     */
    public static FileSnippet resolve(FileSnippet cli, FileSnippet fileSnippet) {
        FileSnippet resolved = FileSnippet.builder().build();

        if (cli != null) {
            applyNonDefault(cli, resolved);
        }
        if (fileSnippet != null) {
            applyNonDefault(fileSnippet, resolved);
        }

        return resolved;
    }

    private static void applyNonDefault(FileSnippet source, FileSnippet target) {
        if (source.isMinSnippetHitsSet()) {
            target.setMinSnippetHits(source.getMinSnippetHits());
        }
        if (source.isMinSnippetLinesSet()) {
            target.setMinSnippetLines(source.getMinSnippetLines());
        }
        if (source.isHonourFileExtsSet()) {
            target.setHonourFileExts(source.getHonourFileExts());
        }
        if (source.isRankingEnabledSet()) {
            target.setRankingEnabled(source.getRankingEnabled());
        }
        if (source.isRankingThresholdSet()) {
            target.setRankingThreshold(source.getRankingThreshold());
        }
        if (source.getSkipHeaders() != null) {
            target.setSkipHeaders(source.getSkipHeaders());
        }
        if (source.isSkipHeadersLimitSet()) {
            target.setSkipHeadersLimit(source.getSkipHeadersLimit());
        }
    }
}
