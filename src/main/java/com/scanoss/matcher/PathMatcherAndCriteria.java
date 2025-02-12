package com.scanoss.matcher;

import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;

public class PathMatcherAndCriteria implements PathMatcher {
    @Override
    public boolean matches(Path path) {
        return false;
    }
/*    private final PathMatcher pathMatcher;

    @Builder
    public PathMatcherOrCriteria(@NotNull List<PathMatcher> matchers){this.matchers = matchers;
    }

    @Override
    public boolean matches(Path path) {
        return false;
    }*/
}
