// SPDX-License-Identifier: MIT
/*
 * Copyright (c) 2023, SCANOSS
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
package com.scanoss.dto;

import com.google.gson.annotations.SerializedName;
import com.scanoss.dto.enums.MatchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Scan File Result Detailed Information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanFileDetails {
    @SerializedName("id")
    private MatchType matchType;
    private String component;
    private String vendor;
    private String version;
    private String latest;
    private String url;
    private String status;
    private String matched;
    private String file;
    private String lines;
    @SerializedName("oss_lines")
    private String ossLines;
    @SerializedName("file_hash")
    private String fileHash;
    @SerializedName("file_url")
    private String fileUrl;
    @SerializedName("url_hash")
    private String urlHash;
    @SerializedName("release_date")
    private String releaseDate;
    @SerializedName("source_hash")
    private String sourceHash;
    @SerializedName("purl")
    private String[] purls;
    @SerializedName("server")
    private ServerDetails serverDetails;
    @SerializedName("licenses")
    private LicenseDetails[] licenseDetails;
    @SerializedName("quality")
    private QualityDetails[] qualityDetails;
    @SerializedName("vulnerabilities")
    private VulnerabilityDetails[] vulnerabilityDetails;
    @SerializedName("copyrights")
    private CopyrightDetails[] copyrightDetails;
}
