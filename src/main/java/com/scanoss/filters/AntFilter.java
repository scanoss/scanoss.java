package com.scanoss.filters;

import lombok.Builder;

import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.jetbrains.annotations.NotNull;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

public class AntFilter {
    List<String> patterns;

    @Builder
    public AntFilter(@NotNull List<String> patterns) {
        this.patterns = patterns;
    }

    public Predicate<Path> get() {
        return p -> patterns.stream().anyMatch(
                pattern ->
                        SelectorUtils.matchPath(pattern, p.toString()));
    }

}
