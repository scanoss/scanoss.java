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

import com.scanoss.exceptions.ScannerException;
import com.scanoss.exceptions.WinnowingException;
import com.scanoss.rest.ScanApi;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * SCANOSS Scanner Class
 * <p/>
 * <p>
 *     This class provides helpers to Fingerprint (WFP) or Scan a given folder or file.
 * </p>
 */
@Getter
@Builder
@Slf4j
public class Scanner {

    @Builder.Default
    private Boolean skipSnippets = Boolean.FALSE; // Skip snippet generations
    @Builder.Default
    private Boolean allExtensions = Boolean.FALSE; // Fingerprint all file extensions
    @Builder.Default
    private Boolean obfuscate = Boolean.FALSE; // Obfuscate file path
    @Builder.Default
    private Boolean hpsm = Boolean.FALSE; // Enable High Precision Snippet Matching data collection
    @Builder.Default
    private Boolean hiddenFilesFolders = Boolean.FALSE; // Enable Scanning of hidden files/folders
//    @Builder.Default
    private Winnowing winnowing;
    private ScanApi scanApi;

    @SuppressWarnings("unused")
    private Scanner(Boolean skipSnippets, Boolean allExtensions, Boolean obfuscate, Boolean hpsm,
                    Boolean hiddenFilesFolders, Winnowing winnowing, ScanApi scanApi
    ) {
        this.skipSnippets = skipSnippets;
        this.allExtensions = allExtensions;
        this.obfuscate = obfuscate;
        this.hpsm = hpsm;
        this.hiddenFilesFolders = hiddenFilesFolders;
        if (winnowing == null) {
            this.winnowing = Winnowing.builder().skipSnippets(skipSnippets).allExtensions(allExtensions).obfuscate(obfuscate).hpsm(hpsm).build();
        } else {
            this.winnowing = winnowing;
        }
        if (scanApi == null) {
            this.scanApi = ScanApi.builder().build();
        } else {
            this.scanApi = scanApi;
        }
    }

    /**
     * Generate a WFP/Fingerprint for the given file
     * @param filename file to fingerprint
     * @return WFP
     * @throws ScannerException Something in Scanning failed
     * @throws WinnowingException Something in Winnowing failed
     */
    public String wfpFile(@NonNull String filename) throws ScannerException, WinnowingException {
        if (filename.isEmpty()) {
            throw new ScannerException("No filename specified. Cannot fingerprint");
        }
        File file = new File(filename);
        if (!file.exists() || ! file.isFile()) {
            throw new ScannerException(String.format("File does not exist or is not a file: %s", filename));
        }
        return this.winnowing.wfpForFile(filename, filename);
    }

    /**
     * Determine if a folder should be processed or not
     * @param name folder/directory to review
     * @return <code>true</code> if the folder should be skipped, <code>false</code> otherwise
     */
    private Boolean filterFolder(String name) {

        if (! this.hiddenFilesFolders && name.startsWith(".") && ! name.equals(".")) {
            log.trace("Skipping hidden folder: {}", name);
            return true;
        }
        for (String ending : ScanossConstants.FILTERED_DIRS) {
            if (name.endsWith(ending)) {
                log.trace("Skipping folder due to ending: {} - {}", name, ending);
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if a file should be processed or not
     * @param name filename to review
     * @return <code>true</code> if the file should be skipped, <code>false</code> otherwise
     */
    private Boolean filterFile(String name) {
        // Skip hidden files unless explicitly asked to read them
        if (! this.hiddenFilesFolders && name.startsWith(".")) {
            log.trace("Skipping hidden file: {}", name);
            return true;
        }
        // Process all file extensions if requested
        if (this.allExtensions) {
            log.trace("Processing all file extensions: {}", name );
            return false;
        }
        // Skip some specific files
        if (ScanossConstants.FILTERED_FILES.contains(name)) {
            log.trace("Skipping specific file: {}", name);
            return true;
        }
        // Skip specific file endings/extensions
        for (String ending : ScanossConstants.FILTERED_EXTENSIONS ) {
            if (name.endsWith(ending)) {
                log.trace("Skipping file due to ending: {} - {}", name, ending);
                return true;
            }
        }
        return false;
    }

    /**
     * Strip the leading string from the specified path
     *
     * @param scanDir Root path
     * @param path Path to strip
     * @return Updated (if necessary) path
     */
    private String stripDirectory(String scanDir, String path) {
        int length = scanDir.endsWith(File.pathSeparator) ? scanDir.length() : scanDir.length() + 1;
        if (length > 0 && path.startsWith(scanDir)) {
            return path.substring(length);
        }
        return path;
    }

    /**
     * Generate WFPs/Fingerprints for the given folder
     * @param folder folder/directory to fingerprint
     * @return List of WFPs
     * @throws ScannerException Something in Scanning failed
     * @throws WinnowingException Something in Winnowing failed
     */
    public List<String> wfpFolder(@NonNull String folder) throws ScannerException, WinnowingException {
        if (folder.isEmpty()) {
            throw new ScannerException("No folder/directory specified. Cannot fingerprint");
        }
        File dir = new File(folder);
        if (!dir.exists() || ! dir.isDirectory()) {
            throw new ScannerException(String.format("Folder/directory does not exist or is not a folder: %s", folder));
        }
        Set<String> fileList = new HashSet<>();
        try {
            Files.walkFileTree(Paths.get(folder), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path file, BasicFileAttributes attrs) {
                    String nameLower = file.getFileName().toString().toLowerCase();
                    if (attrs.isDirectory() && filterFolder(nameLower)) {
                        return FileVisitResult.SKIP_SUBTREE; // Skip the rest of this directory tree
                    }
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String nameLower = file.getFileName().toString().toLowerCase();
                    if (attrs.isRegularFile() && !filterFile(nameLower) && attrs.size() > 0) {
                        fileList.add(file.toString());  // Found a file to fingerprint
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (SecurityException | InvalidPathException | IOException e) {
            throw new ScannerException(String.format("Problem encountered fingerprinting %s", folder), e);
        }
        log.debug("Found {} files to fingerprint...", fileList.size());

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        List<Future<String>> futures = new ArrayList<>(fileList.size());
        for(String file : fileList) {
            Future<String> future = executorService.submit(() -> this.winnowing.wfpForFile(file, stripDirectory(folder, file)));
            futures.add(future);
        }
        List<String> wfps = new ArrayList<>(fileList.size());
        for( Future<String> future : futures) {
            try {
                String wfp = future.get();
                if (wfp != null && ! wfp.isEmpty()) {
                    wfps.add(wfp);
                } else {
                    log.warn("something wrong generating WFP: {}", wfp);
                }
            } catch (InterruptedException e) {
                throw new ScannerException( "Winnowing subtask failed", e);
            } catch (ExecutionException e) {
                throw new ScannerException( "Winnowing subtask failed", e);
            }}
        return wfps;
    }

    /**
     * Scan the given file
     * @param filename file to scan
     * @return scan results string (in JSON format)
     */
    public String scanFile(@NonNull String filename) {
        String wfp = wfpFile(filename);
        if (wfp != null && ! wfp.isEmpty()) {
            String response = this.scanApi.scan(wfp, "", 1);
            if (response != null && !response.isEmpty()) {
                return response;
            }
        }
        return "";
    }

    /**
     * Scan the given folder
     * @param folder folder to scan
     * @return List of scan result strings (in JSON format)
     */
    public List<String> scanFolder(@NonNull String folder) {

        List<String> wfps = wfpFolder(folder);
        if (wfps != null && ! wfps.isEmpty()) {
            List<String> responses = new ArrayList<>(wfps.size());
            for (String wfp: wfps) {
                String response = this.scanApi.scan(wfp, "", 1);
                if (response != null && !response.isEmpty()) {
                    responses.add(response);
                }
            }
            return responses;
        }
        return new ArrayList<>();
    }
}