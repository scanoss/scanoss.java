package com.scanoss.matcher;

import lombok.Builder;

import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.jetbrains.annotations.NotNull;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

public class AntPathMatcher implements PathFilter {
    List<String> patterns;

    @Builder
    public AntPathMatcher(@NotNull List<String> patterns) {
        this.patterns = patterns;
    }

    public Predicate<Path> getPathFilter() {
        return p -> patterns.stream().anyMatch(
                pattern ->
                        SelectorUtils.matchPath(pattern, p.toString()));
    }

}
