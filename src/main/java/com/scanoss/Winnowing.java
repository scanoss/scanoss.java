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
package com.scanoss;

import com.scanoss.exceptions.WinnowingException;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.CRC32C;
import java.util.zip.Checksum;

/**
 * SCANOSS Winnowing Class
 * <p/>
 * <p>
 * The Winnowing class provides all the necessary implementations to fingerprint a given file or contents.
 * </p>
 */
@Getter
@Setter
@Builder
@Slf4j
public class Winnowing {

    private static final FileNameMap fileNameMap = URLConnection.getFileNameMap();
    private static final Tika tika = new Tika();
    private static final MediaTypeRegistry mediaTypeRegistry = MediaTypeRegistry.getDefaultRegistry();

    @Builder.Default
    private Boolean skipSnippets = Boolean.FALSE; // Skip snippet generations
    @Builder.Default
    private Boolean allExtensions = Boolean.FALSE; // Fingerprint all file extensions
    @Builder.Default
    private Boolean obfuscate = Boolean.FALSE; // Obfuscate file path
    @Builder.Default
    private Boolean hpsm = Boolean.FALSE; // Enable High Precision Snippet Matching data collection

    /**
     * Calculate the WFP (fingerprint) for the given file
     *
     * @param filePath Full path of the file to fingerprint
     * @param path     name/path to record in the WFP
     * @return WFP or <code>null</code>
     * @throws WinnowingException Something went wrong with fingerprinting
     */
    public String wfpForFile(@NonNull String filePath, String path) throws WinnowingException {
        if (filePath.isEmpty()) {
            throw new WinnowingException("File Path needs to be specified to generate WFP");
        }
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new WinnowingException(String.format("%s does not exist, or is not a file", filePath));
        }
        Boolean isText = this.isTextFile(file); // Detect file type from name
        try {
            byte[] contents = Files.readAllBytes(file.toPath());
            if (isText == null) {
                isText = isTextContent(file, contents);  // Detect file type from contents
            }
            boolean isBinary = (isText == null || isText == false) ? true : false;
            return wfpForContents(path, isBinary, contents);
        } catch (IOException e) {
            throw new WinnowingException(String.format("Failed to load file contents for: %s", filePath), e);
        }
    }


    /**
     * Generate a WFP for the given file contents
     *
     * @param filename name of file to record in WFP
     * @param binFile  mark the file as binary or source
     * @param contents file contents
     * @return WFP string
     */
    public String wfpForContents(String filename, Boolean binFile, byte[] contents) {
        char[] fileContents = (new String(contents, Charset.defaultCharset())).toCharArray();
        String fileMD5 = DigestUtils.md5Hex(contents);
        StringBuilder wfpBuilder = new StringBuilder();
        // TODO add obfuscation of the filename here
        wfpBuilder.append(String.format("file=%s,%d,%s\n", fileMD5, contents.length, filename));
        if (binFile || this.skipSnippets || this.skipSnippets(filename, fileContents)) {
            return wfpBuilder.toString();
        }
        String gram = "";
        List<Long> window = new ArrayList<>();
        char normalized;
        long minHash;
        long lastHash = ScanossConstants.MAX_CRC32;
        int lastLine = 0;
        int line = 1;
        StringBuilder outputBuilder = new StringBuilder();
        for (char c : fileContents) {
            if (c == '\n') {
                line++;
                normalized = 0;
            } else {
                normalized = normalize(c);
            }
            if (normalized > 0) {
                gram += normalized;
                if (gram.length() >= ScanossConstants.GRAM) {
                    Long gramCRC32 = crc32c(gram);
                    window.add(gramCRC32);
                    if (window.size() >= ScanossConstants.WINDOW) {
                        minHash = min(window);
                        if (minHash != lastHash) {
                            String minHashHex = crc32cHex(minHash);
                            if (lastLine != line) {
                                if (outputBuilder.length() > 0) {
                                    wfpBuilder.append(outputBuilder).append("\n");
                                }
                                outputBuilder.delete(0, outputBuilder.length());
                                outputBuilder.append(String.format("%d=%s", line, minHashHex));
                            } else {
                                outputBuilder.append(",").append(minHashHex);
                            }
                            lastLine = line;
                            lastHash = minHash;
                        }
                        window.remove(0); // Shift window
                    }
                    gram = gram.substring(1); // Shift gram
                }
            }
        }
        if (outputBuilder.length() > 0) {
            wfpBuilder.append(outputBuilder).append("\n");
        }
        return wfpBuilder.toString();
    }

    /**
     * Determine if a file/contents should be skipped for snippet generation or not
     *
     * @param filename filename for the contents (optional)
     * @param contents file contents
     * @return <code>true</code> if we should skip snippets, <code>false</code> otherwise
     */
    private Boolean skipSnippets(@NonNull String filename, char[] contents) {
        // Force snippet collection on all files, regardless of ending or size
        if (this.allExtensions) {
            log.trace("Generating snippets for all extensions: {}", filename);
            return false;
        }
        if (!filename.isEmpty()) {
            String lowerFilename = filename.toLowerCase();
            for (String ending : ScanossConstants.SKIP_SNIPPET_EXT) {
                if (lowerFilename.endsWith(ending)) {
                    log.trace("Skipping snippets due to file ending: {} - {}", filename, ending);
                    return true;
                }
            }
        }
        if (contents.length <= ScanossConstants.MIN_FILE_SIZE) {
            log.trace("Skipping snippets as the file is too small: {} - {}", filename, contents.length);
            return true;
        }
        if (contents[0] == '{' || contents[0] == '<') {
            log.trace("Skipping snippets as the file appears to be JSON/XML/HTML: {}", filename);
            return true;
        }
        return false;
    }

    /**
     * Determine if a file is binary or not
     *
     * @param f File to check
     * @return <code>true</code> if binary, <code>false</code> otherwise
     */
    private boolean isBinaryFile1(File f) {
        try {
            String type = Files.probeContentType(f.toPath());
            if (type == null) {
                log.info("Trying Tika for {}", f);
                type = tika.detect(f);
                if (type == null || type.isEmpty()) {
                    log.info("Trying guessContentType for {}", f);
                    type = URLConnection.guessContentTypeFromName(f.getName());
                    if (type == null || type.isEmpty()) {
                        log.info("Trying filenameMap for {}", f);
                        type = fileNameMap.getContentTypeFor(f.getName());
                        if (type == null || type.isEmpty()) {
                            URLConnection connection = f.toURL().openConnection();
                            type = connection.getContentType();
                            if (type == null || type.isEmpty()) {
                                log.debug("Could not determine file type for: {}. Assuming it is not binary.", f);
                                return false;  // type couldn't be determined, assume not binary
                            }
                        }
                    }
                }
            }
            if (type.contains("octet-stream") || type.contains("application/java-archive")) {
                return true;
            }
            if (type != null || !type.isEmpty()) {
                log.trace("File type for: {} - {}", type, f);
                if (type.startsWith("text") || type.contains("javascript") || type.contains("content/unknown") ||
                        type.contains("application/x-sh") || type.contains("application/x-csh")
                ) {
                    return false;
                }
            }
        } catch (IOException | SecurityException e) {
            log.debug("Issue determining file type for: {} - {}", f, e.getLocalizedMessage());
        }
        return true; // Assume it's a binary file and skip snippet generation
    }

    private Boolean isTextFile(File f) {
        try {
            String type = tika.detect(f);
            if (type != null && !type.isEmpty()) {
                MediaType mediaType = MediaType.parse(type);
                return isTextMediaType(mediaType);
            } else {
                log.warn("Could not determine file type for: {}", f);
                return null;
            }
        } catch (IOException e) {
            log.warn("Issue determining file type for: {} - {}", f, e.getLocalizedMessage());
        }
        return null;
}

    private Boolean isTextContent(File f, byte[] contentBytes) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(contentBytes);
        try {
            MediaType mediaType = MediaType.parse(tika.detect(byteArrayInputStream));
            return isTextMediaType(mediaType);
        } catch (IOException e) {
            log.debug("Issue determining file type for: {} - {}", f, e.getLocalizedMessage());
        }
        return false;
    }

    private Boolean isTextMediaType(MediaType mediaType) {

        if (mediaType == null) {
            return false;
        }
        if (mediaType.getType().equals("text")) {
            return true;
        }
        Set<MediaType> mediaTypes = new HashSet<>();
        while (mediaType != null) {
            mediaTypes.addAll(mediaTypeRegistry.getAliases(mediaType));
            mediaTypes.add(mediaType);
            mediaType = mediaTypeRegistry.getSupertype(mediaType);
        }
        return mediaTypes.stream().anyMatch(mt -> mt.getType().equals("text"));
    }


    /**
     * Convert the give number to a Little Endian encoded byte
     *
     * @param number <code>long</code> number to convert
     * @return Little Endian encoded <code>byte</code>
     */
    private byte[] toLittleEndian(long number) {
        byte[] b = new byte[4];
        b[0] = (byte) (number & 0xFF);
        b[1] = (byte) ((number >> 8) & 0xFF);
        b[2] = (byte) ((number >> 16) & 0xFF);
        b[3] = (byte) ((number >> 24) & 0xFF);
        return b;
    }

    /**
     * Normalise the given character
     *
     * @param c character to normalise
     * @return normalised character
     */
    private char normalize(char c) {
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
     * Calculate the CRC32 for the given string
     *
     * @param s String to calculate
     * @return CRC32 value of the string
     */
    private long crc32c(@NonNull String s) {
        Checksum checksum = new CRC32C();
        checksum.update(s.getBytes());
        return checksum.getValue();
    }

    /**
     * Calculate the Hex for the CRC32 of the given number
     *
     * @param l Number to encode
     * @return Zero padded Hex value of the CRC32
     */
    private String crc32cHex(long l) {
        Checksum checksum = new CRC32C();
        checksum.update(toLittleEndian(l));
        return zeroPaddedString(Long.toHexString(checksum.getValue()));
    }

    /**
     * Zero pad the given Hex String to be 8 bytes wide
     *
     * @param hexString Hex String to pad
     * @return Padded Hex String
     */
    private String zeroPaddedString(@NonNull String hexString) {
        StringBuilder hexStringBuilder = new StringBuilder(hexString);
        while (hexStringBuilder.length() != 8) {
            hexStringBuilder.insert(0, "0");
        }
        return hexStringBuilder.toString();
    }

    /**
     * Return the smallest number of the given list
     *
     * @param l List of numbers to sort
     * @return smallest number
     */
    private long min(@NonNull List<Long> l) {
        List<Long> sortedList = new ArrayList<>(l);
        Collections.sort(sortedList);
        return sortedList.get(0);
    }
}
