package com.scanoss.matcher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PathMatcherOrCriteria implements PathMatcher {
    private final @Builder.Default List<PathMatcher> matchers = new ArrayList<>();

    @Override
    public boolean matches(Path path) {
        return matchers.stream().anyMatch(matcher -> matcher.matches(path));
    }
}
