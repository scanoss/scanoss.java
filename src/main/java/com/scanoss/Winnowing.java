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
import com.scanoss.utils.Hpsm;
import com.scanoss.utils.WinnowingUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.CRC32C;
import java.util.zip.Checksum;

import static com.scanoss.ScanossConstants.MAX_LONG_LINE_CHARS;

/**
 * SCANOSS Winnowing Class
 * <p>
 * The Winnowing class provides all the necessary implementations to fingerprint a given file or contents.
 * </p>
 */
@Getter
@Setter
@Builder
@Slf4j
public class Winnowing {
    // Media type detection
    private static final Tika tika = new Tika();
    private static final MediaTypeRegistry mediaTypeRegistry = MediaTypeRegistry.getDefaultRegistry();

    /**
     * Shared counter for generating unique IDs.
     * idGenerator is shared across all Winnowing instances,
     * ensuring sequential and unique ID generation for path obfuscation
     * regardless of how many instances of Winnowing are created.
     */
    private static final AtomicLong idGenerator = new AtomicLong(0);

    @Builder.Default
    private Boolean skipSnippets = Boolean.FALSE; // Skip snippet generations
    @Builder.Default
    private Boolean allExtensions = Boolean.FALSE; // Fingerprint all file extensions
    @Builder.Default
    private Boolean obfuscate = Boolean.FALSE; // Obfuscate file path
    @Builder.Default
    private boolean hpsm = Boolean.FALSE; // Enable High Precision Snippet Matching data collection
    @Builder.Default
    private int snippetLimit = MAX_LONG_LINE_CHARS; // Enable limiting of size of a single line of snippet generation
    @Builder.Default
    private Map<String, String> obfuscationMap = new ConcurrentHashMap<>();

    /**
     * Resolves the real file path for a given obfuscated path.
     * This method is thread-safe and can be called concurrently from multiple threads.
     * If the provided path is not found in the obfuscation map, the original path is returned.
     *
     * @param obfuscatedPath the obfuscated path
     * @return the real file path corresponding to the provided obfuscated path, or the original path if no mapping exists
     */
    public String deobfuscateFilePath(@NonNull String obfuscatedPath) {
        String originalPath = obfuscationMap.get(obfuscatedPath);
        return originalPath != null ? originalPath : obfuscatedPath;
    }


    /**
     * Retrieves the size of the obfuscation map.
     *
     * @return the number of entries in the obfuscation map
     */
    public int getObfuscationMapSize() {
        return obfuscationMap.size();
    }

    /**
     * Calculate the WFP (fingerprint) for the given file
     *
     * @param filePath Full path of the file to fingerprint
     * @param path     name/path to record in the WFP
     * @return WFP or <code>null</code>
     * @throws WinnowingException Something went wrong with fingerprinting
     */
    public String wfpForFile(@NonNull String filePath, @NonNull String path) throws WinnowingException {
        if (filePath.isEmpty() || path.isEmpty()) {
            throw new WinnowingException("File and Path need to be specified to generate WFP");
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
            return wfpForContents(path, !isText, contents);
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
    public String wfpForContents(@NonNull String filename, Boolean binFile, byte[] contents) {
        if (filename.isEmpty()) {
            throw new WinnowingException("Filename cannot be empty for WFP");
        }
        char[] fileContents = (new String(contents, Charset.defaultCharset())).toCharArray();
        String fileMD5 = DigestUtils.md5Hex(contents);
        StringBuilder wfpBuilder = new StringBuilder();

        if (obfuscate) {
            filename = obfuscateFilePath(filename);
        }

        wfpBuilder.append(String.format("file=%s,%d,%s\n", fileMD5, contents.length, filename));

        String fh2 = WinnowingUtils.calculateOppositeLineEndingHash(contents);
        if (fh2 != null){
            wfpBuilder.append(String.format("fh2=%s\n",fh2));
        }

        if (binFile || this.skipSnippets || this.skipSnippets(filename, fileContents)) {
            return wfpBuilder.toString();
        }

        if(this.isHpsm()){
            wfpBuilder.append(String.format("hpsm=%s\n", Hpsm.calcHpsm(contents)));
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
                normalized = WinnowingUtils.normalize(c);
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
                                int obLength = outputBuilder.length();
                                if (obLength > 0) {
                                    wfpBuilder.append(outputBuilder).append("\n");
                                }
                                outputBuilder.delete(0, obLength);
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
        int obLength = outputBuilder.length();
        if (obLength > 0) {
            wfpBuilder.append(outputBuilder).append("\n");
        }
        return wfpBuilder.toString();
    }

    /**
     * Obfuscates the given file path by replacing it with a generated unique identifier while
     * retaining its original file extension.
     * This method is thread-safe and can be called concurrently from multiple threads.
     *
     * @param originalPath the original file path to be obfuscated; must not be null
     * @return the obfuscated file path with a unique identifier and the original file extension
     */
    private String obfuscateFilePath(@NonNull String originalPath) {
        final String extension = extractExtension(originalPath);

        // Generate a unique identifier for the obfuscated file using a thread-safe approach
        final String obfuscatedPath = idGenerator.getAndIncrement() + extension;
        this.obfuscationMap.put(obfuscatedPath, originalPath);
        return obfuscatedPath;
    }

    /**
     * Extracts file extension from the given path, including the leading dot.
     *
     * @param path the file path or name (must not be null)
     * @return the file extension with leading dot (e.g., ".txt") or empty string if no extension
     */
    private String extractExtension(@NonNull String path) {
        try {
            String extractedExtension = FilenameUtils.getExtension(path).trim();
            return extractedExtension.isEmpty() ? "" : "." + extractedExtension;
        } catch (IllegalArgumentException e) {
            log.debug("Could not extract extension from filename '{}': {}",
                    path, e.getMessage());
            return "";
        }
    }

    /**
     * Determine if a file/contents should be skipped for snippet generation or not
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
        if (contents.length <= ScanossConstants.MIN_FILE_SIZE) {
            log.trace("Skipping snippets as the file is too small: {} - {}", filename, contents.length);
            return true;
        }
        //See https://github.com/scanoss/scanoss.py/blob/ede0477f3ea1b13a0147154b565b1bf6a72a6843/src/scanoss/winnowing.py#L248-L260
        //for python implementation reference

        // Create prefix from first MIN_FILE_SIZE-1 characters, lowercase and trimmed
        String prefix = new String(contents, 0, ScanossConstants.MIN_FILE_SIZE - 1).toLowerCase().strip();

        // Check for JSON files (starts with { or [)
        if (prefix.charAt(0) == '{' || prefix.charAt(0) == '[') {
            log.trace("Skipping snippets as the file appears to be JSON: {}", filename);
            return true;
        }
        // Check for XML/HTML/AC3D files with explicit prefix matching
        if (prefix.startsWith("<?xml") || prefix.startsWith("<html") ||
            prefix.startsWith("<ac3d") || prefix.startsWith("<!doc")) {
            log.trace("Skipping snippets as the file appears to be xml/html/binary: {}", filename);
            return true;
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
        // Check if first line is too long (matches Python implementation)
        int firstLineEnd = 0;
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] == '\n') {
                firstLineEnd = i;
                break;
            }
        }
        if (firstLineEnd == 0) {
            firstLineEnd = contents.length - 1;  // No newline found, use length-1 (matching Python)
        }
        if (snippetLimit > 0 && firstLineEnd > snippetLimit) {
            log.trace("Skipping snippets due to first line being too long: {} - {} chars", filename, firstLineEnd);
            return true;
        }
        return false;
    }

    /**
     * Try to detect if this is a text file or not
     *
     * @param f File to check
     * @return <code>true/false</code> if is/is not a text file, <code>null</code> if something went wrong
     */
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

    /**
     * Check if the file contents is a text file
     *
     * @param f File being checked
     * @param contentBytes File Contents
     * @return <code>true</code> if a text file, <code>false</code> otherwise
     */
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

    /**
     * Check if this media type is a text based
     *
     * @param mediaType Media Type
     * @return <code>true</code> if a text file, <code>false</code> otherwise
     */
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
