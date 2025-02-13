package com.scanoss.matcher;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.function.Predicate;

@Slf4j
public abstract class BaseFilter implements PathFilter {
    protected final FilterConfig config;
    protected final GitIgnoreMatcher gitIgnoreMatcher;
    protected final AntPathMatcher antPathMatcher;
    protected final Predicate<Path> predicate;


    public BaseFilter(Config config, List<String> gitIgnorePatterns, List<String> antPatterns, Predicate<Path> predicate) {
        gitIgnoreMatcher = GitIgnoreMatcher.builder().patterns(gitIgnorePatterns).build();
        antPathMatcher = AntPathMatcher.builder().patterns(antPatterns).build();
        this.predicate = predicate;
    }

    public BaseFilter(FilterConfig config) {
        this.config = config;
        gitIgnoreMatcher = GitIgnoreMatcher.builder().patterns(config.getGitIgnorePatterns()).build();
        antPathMatcher = AntPathMatcher.builder().patterns(config.getAntPatterns()).build();
        this.predicate = this.antPathMatcher.getPathFilter().or(this.gitIgnoreMatcher.getPathFilter();
    }


    public abstract Boolean evaluate(Path path);

}
