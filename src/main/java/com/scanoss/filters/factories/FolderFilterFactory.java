package com.scanoss.filters.factories;
import com.scanoss.filters.FilterConfig;
import com.scanoss.filters.FolderFilter;

import java.nio.file.Path;
import java.util.function.Predicate;

public class FolderFilterFactory {
    public static Predicate<Path> build(FilterConfig config) {  // Static factory method
        return new FolderFilter(config).get();
    }
}
