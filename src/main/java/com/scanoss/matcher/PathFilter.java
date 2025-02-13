package com.scanoss.matcher;

import java.nio.file.Path;
import java.util.function.Predicate;

public interface PathFilter {
    Boolean evaluate(Path path);
    Predicate<Path> buildFilter();
}

