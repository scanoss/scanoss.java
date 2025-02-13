package com.scanoss.filters.factories;

import com.scanoss.filters.FileFilter;
import com.scanoss.filters.FilterConfig;

import java.nio.file.Path;
import java.util.function.Predicate;

public class FileFilterFactory {
    public static Predicate<Path> build(FilterConfig config) {  // Static factory method
        return FileFilter.builder().filterConfig(config).build().get();
    }
}
