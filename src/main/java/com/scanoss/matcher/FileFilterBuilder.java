package com.scanoss.matcher;


import lombok.Builder;

import java.nio.file.Path;
import java.util.function.Predicate;

import static com.scanoss.ScanossConstants.FILTERED_DIRS;
import static com.scanoss.ScanossConstants.FILTERED_DIR_EXT;


public class FileFilterBuilder extends BaseFilterFactory {

    public FileFilterBuilder(FilterConfig filterConfig) {
        super(filterConfig);
    }

    public Predicate<Path> build(FilterConfig filterConfig) {
        Predicate<Path> filter = p -> false;
        //README: https://scanoss.readthedocs.io/projects/scanoss-py/en/latest/
        if (!this.config.getHiddenFilesFolders()) {

            Predicate<Path> skipHiddenFilter = p -> p.startsWith(".");
            Predicate<Path> notSkipcurrentDirectoryFilter = p -> !p.getFileName().toString().equals(".");

            this.baseSkipFilter.or(skipHiddenFilter).and(notSkipcurrentDirectoryFilter);
        }

        if (!this.config.getAllFolders()) {
            log.debug("All folders: {}", this.allFolders);
            Predicate<Path> filterDirs = p -> FILTERED_DIRS.stream()
                    .anyMatch(d -> p.getFileName().toString().toLowerCase().endsWith(d));
            filter = filter.or(filterDirs);
            Predicate<Path> filterDirExt = p -> FILTERED_DIR_EXT.stream()
                    .anyMatch(d -> p.getFileName().toString().toLowerCase().endsWith(d));
            filter = filter.or(filterDirExt);

        }
}
