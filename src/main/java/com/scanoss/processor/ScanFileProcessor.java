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
package com.scanoss.processor;

import com.scanoss.Winnowing;
import com.scanoss.exceptions.ScanApiException;
import com.scanoss.exceptions.WinnowingException;
import com.scanoss.rest.ScanApi;
import lombok.Builder;

/**
 * SCANOSS File Scan Process Implementation
 * <p></p>
 * <p>
 *     This class provides the implementation to fingerprint and scan the requested file
 * </p>
 */
@Builder
public class ScanFileProcessor implements FileProcessor{
    @Builder.Default
    private final ScanApi scanApi = ScanApi.builder().build();
    @Builder.Default
    private final Winnowing winnowing = Winnowing.builder().build();

    /**
     * Scan the given file and return results
     *
     * @param file File to scan
     * @param folder root folder of the file to scan
     * @return Scan result
     *
     * @throws WinnowingException if something went wrong while fingerprinting
     * @throws ScanApiException if something went wrong with the scan API
     */
    @Override
    public String process(String file, String folder) throws WinnowingException, ScanApiException {
        return scanApi.scan(winnowing.wfpForFile(file, folder),"", 1);
    }
}
