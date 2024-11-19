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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.scanoss.utils.JsonUtils.checkBooleanString;

/**
 * Scan Results Match License Details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseDetails {
    private String name;
    private String source;
    private String copyleft;
    @SerializedName("patent_hints")
    private String patentHints;
    private String url;
    @SerializedName("checklist_url")
    private String checklistUrl;
    @SerializedName("osadl_updated")
    private String osadlUpdated;

    /**
     * Determine if the license is Copyleft or not
     *
     * @return <code>true</code> if copyleft, <code>false</code> otherwise
     */
    public boolean isCopyleft() {
        return checkBooleanString(copyleft);
    }

    /**
     * Determine if the license is Copyleft or not
     *
     * @return <code>true</code> if copyleft, <code>false</code> otherwise
     */
    public boolean hasPatentHints() {
        return checkBooleanString(patentHints);
    }
}
