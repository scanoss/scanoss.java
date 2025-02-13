package com.scanoss.matcher;

import lombok.Builder;

import java.nio.file.Path;
import java.util.function.Predicate;


@Builder
public class FileFilter extends BaseFilter {

    @Builder.Default
    private final FilterConfig config = null;

    public FileFilter(FilterConfig config) {
        super(config.getGitIgnorePatterns(), config.getAntPatterns());
        this.config = config;
    }

    @Override
    public Boolean evaluate(Path path) {
        return null;
    }

    @Override
    public Predicate<Path> buildFilter() {
        return null;
    }
}
