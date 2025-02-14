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

import com.scanoss.dto.ScanFileResult;
import com.scanoss.exceptions.ScannerException;
import com.scanoss.exceptions.WinnowingException;
import com.scanoss.filters.FilterConfig;
import com.scanoss.filters.factories.FileFilterFactory;
import com.scanoss.filters.factories.FolderFilterFactory;
import com.scanoss.processor.*;
import com.scanoss.rest.ScanApi;
import com.scanoss.settings.Settings;
import com.scanoss.utils.JsonUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;

import static com.scanoss.ScanossConstants.*;

/**
 * SCANOSS Scanner Class
 * <p>
 * This class provides helpers to Fingerprint (WFP) or Scan a given folder or file.
 * </p>
 */
@Getter
@Builder
@Slf4j
public class Scanner {
    @Builder.Default
    private Boolean skipSnippets = Boolean.FALSE;  // Skip snippet generations

    @Builder.Default
    private Boolean allExtensions = Boolean.FALSE; // Fingerprint all file extensions

    @Builder.Default
    private Boolean obfuscate = Boolean.FALSE; // Obfuscate file path

    @Builder.Default
    private Boolean hpsm = Boolean.FALSE; // Enable High Precision Snippet Matching data collection

    @Builder.Default
    private Boolean hiddenFilesFolders = Boolean.FALSE; // Enable Scanning of hidden files/folders

    @Builder.Default
    private Boolean allFolders = Boolean.FALSE; // Enable Scanning of all folders (except hidden)

    @Builder.Default
    private Integer numThreads = DEFAULT_WORKER_THREADS;  // Number of parallel threads to use when processing a folder

    @Builder.Default
    private Duration timeout = Duration.ofSeconds(DEFAULT_TIMEOUT); // API POST timeout

    @Builder.Default
    private Integer retryLimit = DEFAULT_HTTP_RETRY_LIMIT; // Retry limit for posting scan requests

    private final String url;  // Alternative scanning URL
    private final String apiKey; // API key
    private final String scanFlags; // Scan flags to pass to the APIç
    private final String sbomType; // SBOM type (identify/ignore)
    private final String sbom;  // SBOM to supply while scanning
    private final int snippetLimit; // Size limit for a single line of generated snippet
    private final String customCert; // Custom certificate
    private final Proxy proxy; // Proxy
    private final Winnowing winnowing;
    private final ScanApi scanApi;
    private final ScanFileProcessor scanFileProcessor;
    private final WfpFileProcessor wfpFileProcessor;
    private final Settings settings;
    private final ScannerPostProcessor postProcessor;
    private final FilterConfig filterConfig;

    @Getter(AccessLevel.PRIVATE)
    private Predicate<Path> fileFilter;
    @Getter(AccessLevel.PRIVATE)
    private Predicate<Path> folderFilter;

    //TODO: Once this Lombok PR is merged  https://github.com/projectlombok/lombok/pull/3723#pullrequestreview-2617412643
    // Update Lombok dependency
    public static class ScannerBuilder {
        private ScannerBuilder folderFilter(Predicate<Path> folderFilter) {
            return this;
        }
        private ScannerBuilder fileFilter(Predicate<Path> fileFilter) {
            return this;
        }
    }

    @SuppressWarnings("unused")
    private Scanner(Boolean skipSnippets, Boolean allExtensions, Boolean obfuscate, Boolean hpsm,
                    Boolean hiddenFilesFolders, Boolean allFolders, Integer numThreads, Duration timeout,
                    Integer retryLimit, String url, String apiKey, String scanFlags, String sbomType, String sbom,
                    Integer snippetLimit, String customCert, Proxy proxy,
                    Winnowing winnowing, ScanApi scanApi,
                    ScanFileProcessor scanFileProcessor, WfpFileProcessor wfpFileProcessor,
                    Settings settings, ScannerPostProcessor postProcessor, FilterConfig filterConfig,
                    Predicate<Path> fileFilter,
                    Predicate<Path> folderFilter
    ) {
        this.skipSnippets = skipSnippets;
        this.allExtensions = allExtensions;
        this.obfuscate = obfuscate;
        this.hpsm = hpsm;
        this.hiddenFilesFolders = hiddenFilesFolders;
        this.allFolders = allFolders;
        this.numThreads = numThreads;
        this.timeout = timeout;
        this.retryLimit = retryLimit;
        this.url = url;
        this.apiKey = apiKey;
        this.scanFlags = scanFlags;
        this.sbomType = sbomType;
        this.sbom = sbom;
        this.snippetLimit = snippetLimit;
        this.customCert = customCert;
        this.proxy = proxy;
        this.winnowing = Objects.requireNonNullElseGet(winnowing, () ->
                Winnowing.builder().skipSnippets(skipSnippets).allExtensions(allExtensions).obfuscate(obfuscate)
                        .hpsm(hpsm).snippetLimit(snippetLimit)
                        .build());
        this.scanApi = Objects.requireNonNullElseGet(scanApi, () ->
                ScanApi.builder().url(url).apiKey(apiKey).timeout(timeout).retryLimit(retryLimit).flags(scanFlags)
                        .sbomType(sbomType).sbom(sbom).customCert(customCert).proxy(proxy).settings(settings)
                        .build());
        this.scanFileProcessor = Objects.requireNonNullElseGet(scanFileProcessor, () ->
                ScanFileProcessor.builder().winnowing(this.winnowing).scanApi(this.scanApi).build());
        this.wfpFileProcessor = Objects.requireNonNullElseGet(wfpFileProcessor, () -> WfpFileProcessor.builder()
                .winnowing(this.winnowing)
                .build());
        this.settings = Objects.requireNonNullElseGet(settings, () -> Settings.builder().build());
        this.postProcessor = Objects.requireNonNullElseGet(postProcessor, () ->
                ScannerPostProcessor.builder().build());

        this.filterConfig = Objects.requireNonNullElseGet(filterConfig, () -> FilterConfig.builder()
                .allFolders(allFolders)
                .allExtensions(allExtensions)
                .hiddenFilesFolders(hiddenFilesFolders)
                .gitIgnorePatterns(this.settings.getScanningIgnorePattern())
                .build());

        this.fileFilter = FileFilterFactory.build(this.filterConfig);
        this.folderFilter = FolderFilterFactory.build(this.filterConfig);
    }

    /**
     * Generate a WFP/Fingerprint for the given file
     *
     * @param filename file to fingerprint
     * @return WFP
     * @throws ScannerException   Something in Scanning failed
     * @throws WinnowingException Something in Winnowing failed
     */
    public String wfpFile(@NonNull String filename) throws ScannerException, WinnowingException {
        if (filename.isEmpty()) {
            throw new ScannerException("No filename specified. Cannot fingerprint");
        }
        File file = new File(filename);
        if (!file.exists() || !file.isFile()) {
            throw new ScannerException(String.format("File does not exist or is not a file: %s", filename));
        }
        return this.winnowing.wfpForFile(filename, filename);
    }

    /**
     * Strip the leading string from the specified path
     *
     * @param scanDir Root path
     * @param path    Path to strip
     * @return Updated (if necessary) path
     */
    private String stripDirectory(String scanDir, String path) {
        int length = scanDir.endsWith(File.separator) ? scanDir.length() : scanDir.length() + 1;
        if (length > 0 && path.startsWith(scanDir)) {
            return path.substring(length);
        }
        return path;
    }

    /**
     * Generate Search the specified folder and pass to the given processor
     *
     * @param folder    folder/directory to fingerprint
     * @param processor processor to take action on the filtered file
     * @return List of results
     * @throws ScannerException   Something in Scanning failed
     * @throws WinnowingException Something in Winnowing failed
     */
    public List<String> processFolder(@NonNull String folder, FileProcessor processor) throws ScannerException, WinnowingException {
        if (processor == null) {
            throw new ScannerException("No file processor object specified.");
        }
        if (folder.isEmpty()) {
            throw new ScannerException("No folder/directory specified. Cannot process request.");
        }
        File dir = new File(folder);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new ScannerException(String.format("Folder/directory does not exist or is not a folder: %s", folder));
        }
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Future<String>> futures = new ArrayList<>();
        try {
            Files.walkFileTree(Paths.get(folder), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path file, BasicFileAttributes attrs) {
                    if(folderFilter.test(file)) {
                        log.debug("Processing file: {}", file.getFileName().toString());
                        return FileVisitResult.SKIP_SUBTREE; // Skip the rest of this directory tree
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String nameLower = file.getFileName().toString().toLowerCase();
                    if (attrs.isRegularFile() && !fileFilter.test(file) && attrs.size() > 0) {
                        String filename = file.toString();
                        Future<String> future = executorService.submit(() -> processor.process(filename, stripDirectory(folder, filename)));
                        futures.add(future);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (SecurityException | InvalidPathException | IOException e) {
            throw new ScannerException(String.format("Problem encountered processing folder %s", folder), e);
        } finally {
            executorService.shutdown();
        }
        log.debug("Found {} files to process.", futures.size());
        return processFutures(futures);
    }


    /**
     * Process the given list of files (including paths)
     * <p>
     *     This method still applies the file/folder filtering to remove unwanted files from the scan list.
     *     Configure the Scanner object to skip these filters before calling this method if desired.
     * </p>
     * @param root root folder for the files
     * @param files list of files
     * @param processor processor to take action on the filtered file
     * @return List of results
     * @throws ScannerException   Something in Scanning failed
     * @throws WinnowingException Something in Winnowing failed
     */
    public List<String> processFileList(@NonNull String root, @NonNull List<String> files, FileProcessor processor) throws ScannerException, WinnowingException {
        if (processor == null) {
            throw new ScannerException("No file processor object specified.");
        }
        File rootDir = new File(root);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            throw new ScannerException(String.format("Folder/directory does not exist or is not a folder: %s", root));
        }
        if (files.isEmpty()) {
            throw new ScannerException("No file list specified. Cannot process request.");
        }
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Future<String>> futures = new ArrayList<>();
        try {
            for(String file : files) {
                Path path = Path.of(file);
                boolean skipDir = false;
                for (Path p : path) {
                    // should we skip this folder or not
                    if (this.folderFilter.test(p)) {  // should we skip this folder or not
                        skipDir = true;
                        break;
                    }
                }
                if (skipDir) {
                    continue; // skip this file as the folder is not allowed
                }
                String nameLower = path.getFileName().toString().toLowerCase();
                if (!this.fileFilter.test(path)) {
                    Path fullPath = Path.of(root, file);
                    File f = fullPath.toFile();
                    if (f.exists() && f.isFile() && f.length() > 0 && ! Files.isSymbolicLink(fullPath)) {
                        String filename = f.toString();
                        log.debug("Adding file to processing list: {} - {}", file, filename);
                        Future<String> future = executorService.submit(() -> processor.process(filename, stripDirectory(root, filename)));
                        futures.add(future);
                    }
                }
            }
        } catch (SecurityException | InvalidPathException e) {
            throw new ScannerException(String.format("Problem encountered processing folder %s", root), e);
        } finally {
            executorService.shutdown();
        }
        log.debug("Found {} list files to process.", futures.size());
        return processFutures(futures);
    }

    private List<String> processFutures(@NonNull List<Future<String>> futures) throws ScannerException {
        List<String> results = new ArrayList<>(futures.size());
        for (Future<String> future : futures) {
            try {
                String result = future.get();
                if (result != null && !result.isEmpty()) {
                    results.add(result);
                } else {
                    log.warn("something went wrong processing result: {}", future);
                }
            } catch (InterruptedException | ExecutionException e) {
                if (log.isDebugEnabled()) {
                    log.error("Issue encountered processing subtask futures: {}", e.getLocalizedMessage(), e);
                }
                throw new ScannerException("File processing subtask failed", e);
            }
        }
        return results;
    }

    /**
     * Generate WFPs/Fingerprints for the given folder
     *
     * @param folder folder/directory to fingerprint
     * @return List of WFPs
     * @throws ScannerException   Something in Scanning failed
     * @throws WinnowingException Something in Winnowing failed
     */
    public List<String> wfpFolder(@NonNull String folder) throws ScannerException, WinnowingException {
        return processFolder(folder, wfpFileProcessor);
    }

    /**
     * Scan the given file
     *
     * @param filename file to scan
     * @return scan results string (in JSON format)
     * @throws ScannerException     Something in Scanning failed
     * @throws WinnowingException   Something in Winnowing failed
     */
    public String scanFile(@NonNull String filename) throws ScannerException, WinnowingException {
        String wfp = wfpFile(filename);
        if (wfp != null && !wfp.isEmpty()) {
            String response = this.scanApi.scan(wfp, "", 1);
            if (response != null && !response.isEmpty()) {
                return response;
            }
        }
        return "";
    }

    /**
     * Scan the given folder
     *
     * @param folder folder to scan
     * @return List of scan result strings (in JSON format)
     */
    public List<String> scanFolder(@NonNull String folder) {
        List<String> results = processFolder(folder, scanFileProcessor);
        return postProcessResults(results);
    }

    /**
     * Scan the given list of files
     *
     * @param folder root folder
     * @param files list of files to scan
     * @return List of scan result strings (in JSON format)
     */
    public List<String> scanFileList(@NonNull String folder, @NonNull List<String> files) {
        List<String> results = processFileList(folder, files, scanFileProcessor);
        return postProcessResults(results);
    }

    /**
     * Post-processes scan results based on BOM (Bill of Materials) settings if available.
     * @param results List of raw scan results in JSON string format
     * @return Processed results, either modified based on BOM or original results if no BOM exists
     */
    private List<String> postProcessResults(List<String> results) {
        if (settings.getBom() != null) {
            List<ScanFileResult> scanFileResults = JsonUtils.toScanFileResults(results);
            List <ScanFileResult>  newScanFileResults = this.postProcessor.process(scanFileResults, this.settings.getBom());
            return JsonUtils.toRawJsonString(newScanFileResults);
        }
        return results;
    }

}