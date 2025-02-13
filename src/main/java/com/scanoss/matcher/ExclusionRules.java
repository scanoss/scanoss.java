package com.scanoss.matcher;

import com.scanoss.ScanossConstants;

import java.nio.file.Path;
import java.util.function.Predicate;

// Static utility class
public final class ExclusionRules {
    private ExclusionRules() {} // Prevent instantiation

    public static Predicate<Path> forFiles(FilterConfig config) {
        Predicate<Path> exclusions = commonRules(config);

        if (!config.getAllFolders()) {
            exclusions = exclusions.or(fileSpecificRules());
        }

        return exclusions;
    }

    public static Predicate<Path> forDirectories(FilterConfig config) {
        Predicate<Path> exclusions = commonRules(config);

        if (!config.getAllFolders()) {
            exclusions = exclusions.or(directorySpecificRules());
        }

        return exclusions;
    }

    private static Predicate<Path> commonRules(FilterConfig config) {
        Predicate<Path> exclusions = p -> false;

        if (!config.getAllFolders()) {
            exclusions = exclusions.or(p -> p.getFileName().toString().startsWith("."));
        }

        if (!config.getGitIgnorePatterns().isEmpty()) {
//            exclusions = exclusions.or(matchesGitIgnore(config.getGitIgnorePatterns()));
        }

        if (!config.getAntPatterns().isEmpty()) {
//            exclusions = exclusions.or(matchesAntPattern(config.getAntPatterns()));
        }

        return exclusions;
    }

    private static Predicate<Path> fileSpecificRules() {
        return path -> ScanossConstants.FILTERED_DIRS.stream()
                .anyMatch(ext -> path.toString().endsWith(ext));
    }

    private static Predicate<Path> directorySpecificRules() {
        return path -> ScanossConstants.FILTERED_DIRS.stream()
                .anyMatch(dir -> path.getFileName().toString().equals(dir));
    }
}
