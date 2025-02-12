package com.scanoss.matcher;

import lombok.Builder;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;

public class AntPathMatcher implements PathMatcher {
    List<String> patterns;


    @Builder
    public AntPathMatcher(@NotNull List<String> patterns) {
        this.patterns = patterns;
    }

    @Override
    public boolean matches(Path path) {
        return patterns.stream().anyMatch(
                pattern ->
                        SelectorUtils.matchPath(pattern, path.toString()));
    }
}
