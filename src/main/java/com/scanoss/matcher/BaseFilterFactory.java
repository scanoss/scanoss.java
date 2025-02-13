package com.scanoss.matcher;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.function.Predicate;

@Slf4j
public abstract class BaseFilterFactory {
    protected final FilterConfig config;
    protected final GitIgnoreFilter gitIgnoreFilter;
    protected final AntFilter antFilter;
    protected final Predicate<Path> baseSkipFilter;

    protected BaseFilterFactory(FilterConfig config) {
        this.config = config;
        gitIgnoreFilter = GitIgnoreFilter.builder().patterns(config.getGitIgnorePatterns()).build();
        antFilter = AntFilter.builder().patterns(config.getAntPatterns()).build();

        this.baseSkipFilter = this.antFilter.get().or(this.gitIgnoreFilter.get());
    }



    public abstract Predicate<Path> build();

}
