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
import lombok.Data;

/**
 * Scan File Result Detailed Information
 */
@Data
public class ScanFileDetails {
    private final String id;
    private String component;
    private final String vendor;
    private final String version;
    private final String latest;
    private final String url;
    private final String status;
    private final String matched;
    private final String file;
    private final String lines;
    @SerializedName("oss_lines")
    private final String ossLines;
    @SerializedName("file_hash")
    private final String fileHash;
    @SerializedName("file_url")
    private final String fileUrl;
    @SerializedName("url_hash")
    private final String urlHash;
    @SerializedName("release_date")
    private final String releaseDate;
    @SerializedName("source_hash")
    private final String sourceHash;
    @SerializedName("purl")
    private String[] purls;
    @SerializedName("server")
    private final ServerDetails serverDetails;
    @SerializedName("licenses")
    private final LicenseDetails[] licenseDetails;
    @SerializedName("quality")
    private final QualityDetails[] qualityDetails;
    @SerializedName("vulnerabilities")
    private final VulnerabilityDetails[] vulnerabilityDetails;
    @SerializedName("copyrights")
    private final CopyrightDetails[] copyrightDetails;
}
