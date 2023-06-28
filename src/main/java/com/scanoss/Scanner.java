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
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

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
    private Winnowing winnowing;// = Winnowing.builder().build();

    @SuppressWarnings("unused")
    private Scanner(Boolean skipSnippets, Boolean allExtensions, Boolean obfuscate, Boolean hpsm,
                    Boolean hiddenFilesFolders, Winnowing winnowing
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
    }

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

    public List<String> wfpFolder(@NonNull String directory) throws ScannerException, WinnowingException {
        if (directory.isEmpty()) {
            throw new ScannerException("No folder/directory specified. Cannot fingerprint");
        }
        File dir = new File(directory);
        if (!dir.exists() || ! dir.isDirectory()) {
            throw new ScannerException(String.format("Folder/directory does not exist or is not a folder: %s", directory));
        }
        Set<String> fileList = new HashSet<>();
        try {
            Files.walkFileTree(Paths.get(directory), new SimpleFileVisitor<>() {
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
                    if (attrs.isRegularFile() && !filterFile(nameLower)) {
                        fileList.add(file.toString());  // Found a file to fingerprint
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (SecurityException | InvalidPathException | IOException e) {
            throw new ScannerException(String.format("Problem encountered fingerprinting %s", directory), e);
        }
        log.debug("Found {} files to fingerprint...", fileList.size());
        List<String> wfps = new ArrayList<>(fileList.size());
        for(String file : fileList) {
            String wfp = this.winnowing.wfpForFile(file, file);
            if (wfp != null && ! wfp.isEmpty()) {
                wfps.add(wfp);
            }
        }
        return wfps;
    }
}