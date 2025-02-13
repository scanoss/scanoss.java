package com.scanoss.matcher;

import lombok.Builder;

import java.nio.file.Path;
import java.util.function.Predicate;

import static com.scanoss.ScanossConstants.FILTERED_DIRS;
import static com.scanoss.ScanossConstants.FILTERED_DIR_EXT;


@Builder
public class FolderFilter extends BaseFilter {

    private final FilterConfig config;

//    private final Predicate<Path> filter;

    public FolderFilter(FilterConfig config) {
        //super(config.getGitIgnorePatterns(),config.getAntPatterns(), this.buildFilter());
        super(config);
        this.buildFilter();
        this.config = config;
    }

    private void buildFilter(){
        Predicate<Path> filter = p -> false;
        //README: https://scanoss.readthedocs.io/projects/scanoss-py/en/latest/
        if (!this.config.getHiddenFilesFolders()) {

            Predicate<Path> hiddenFiles = p -> p.startsWith(".");
            Predicate<Path> notCurrentDirectory = p -> !p.getFileName().toString().equals(".");
            this.predicate.or(hiddenFiles).and(notCurrentDirectory);
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



        if (this.gitIgnoreMatcher.evaluate())

    }


    @Override
    public Boolean evaluate(Path path) {
        return this.predicate.test(path);
    }
}
