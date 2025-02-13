package com.scanoss.filters;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.function.Predicate;

@Slf4j
public abstract class BaseFilter {
    protected final FilterConfig config;
    protected GitIgnoreFilter gitIgnoreFilter;
    protected  AntFilter antFilter;
    protected  Predicate<Path> baseSkipFilter;

    protected BaseFilter(FilterConfig config) {
        this.config = config;
        this.gitIgnoreFilter = GitIgnoreFilter.builder().patterns(config.getGitIgnorePatterns()).build();
        this.antFilter = AntFilter.builder().patterns(config.getAntPatterns()).build();
        this.baseSkipFilter = this.antFilter.get().or(this.gitIgnoreFilter.get());
    }


}
