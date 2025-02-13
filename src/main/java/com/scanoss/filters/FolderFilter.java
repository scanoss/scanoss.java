package com.scanoss.filters;

import java.nio.file.Path;
import java.util.function.Predicate;

import static com.scanoss.ScanossConstants.FILTERED_DIRS;
import static com.scanoss.ScanossConstants.FILTERED_DIR_EXT;

public class FolderFilter extends BaseFilter {

    public FolderFilter(FilterConfig filterConfig) {
        super(filterConfig);
    }

    public Predicate<Path> get() {
        // First call super's build to initialize base filters
        Predicate<Path> baseFilter = this.baseSkipFilter;

        if (!config.getHiddenFilesFolders()) {
            Predicate<Path> skipHiddenFilter = p -> p.getFileName().toString().startsWith(".");
            Predicate<Path> notSkipCurrentDirectoryFilter = p -> !p.getFileName().toString().equals(".");

            baseFilter = baseFilter
                    .or(skipHiddenFilter)
                    .and(notSkipCurrentDirectoryFilter);
        }

        if (!config.getAllFolders()) {
            Predicate<Path> filterDirs = p -> FILTERED_DIRS.stream()
                    .anyMatch(d -> p.getFileName().toString().toLowerCase().endsWith(d));
            Predicate<Path> filterDirExt = p -> FILTERED_DIR_EXT.stream()
                    .anyMatch(d -> p.getFileName().toString().toLowerCase().endsWith(d));

            baseFilter = baseFilter.or(filterDirs).or(filterDirExt);
        }

        return baseFilter;
    }
}
