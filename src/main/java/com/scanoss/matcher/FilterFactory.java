package com.scanoss.matcher;

import lombok.Builder;

import java.nio.file.Path;
import java.util.function.Predicate;

import static com.scanoss.ScanossConstants.FILTERED_DIRS;
import static com.scanoss.ScanossConstants.FILTERED_DIR_EXT;


@Builder
class FilterFactory {


    private static Predicate<Path> buildBaseFilter()  {

    }

    public static Predicate<Path> buildFileFilter(FilterConfig filterConfig) {
         filter = buildFolderFilter()

         if (filterConfig.getAllFolders())
             filter.or()
    }

    public static Predicate<Path> buildFolderFilter(FilterConfig filterConfig) {
        Predicate<Path> filter = p -> false;
        //README: https://scanoss.readthedocs.io/projects/scanoss-py/en/latest/
        if (!this.config.getHiddenFilesFolders()) {

            Predicate<Path> hiddenFiles = p -> p.startsWith(".");
            Predicate<Path> notCurrentDirectory = p -> !p.getFileName().toString().equals(".");
            this.shouldSkip.or(hiddenFiles).and(notCurrentDirectory);
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


}
