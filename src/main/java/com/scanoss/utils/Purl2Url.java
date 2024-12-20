package com.scanoss.utils;

import com.github.packageurl.PackageURL;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Package URLs (purls) to their corresponding browsable web URLs for
 * different package management systems and source code repositories.
 */
public class Purl2Url {
    private static final Logger log = LoggerFactory.getLogger(Purl2Url.class);

    @Getter
    public enum PurlType {
        GITHUB("github", "https://github.com/%s"),
        NPM("npm", "https://www.npmjs.com/package/%s"),
        MAVEN("maven", "https://mvnrepository.com/artifact/%s"),
        GEM("gem", "https://rubygems.org/gems/%s"),
        PYPI("pypi", "https://pypi.org/project/%s"),
        GOLANG("golang", "https://pkg.go.dev/%s"),
        NUGET("nuget", "https://www.nuget.org/packages/%s");


        private final String type;
        private final String urlPattern;

        PurlType(String type, String urlPattern) {
            this.type = type;
            this.urlPattern = urlPattern;
        }
    }

    /**
     * Checks if the given PackageURL is supported for conversion.
     *
     * @param purl The PackageURL to check
     * @return true if the PackageURL can be converted to a browsable URL
     */
    public static boolean isSupported(@NotNull PackageURL purl) {
        try {
            findPurlType(purl.getType());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Converts a PackageURL to its browsable web URL.
     * Returns null if the conversion is not possible.
     *
     * @param purl The PackageURL to convert
     * @return The browsable web URL or null if conversion fails
     */
    @Nullable
    public static String convert(@NotNull PackageURL purl) {
        try {
            PurlType purlType = findPurlType(purl.getType());
            String fullName = purl.getNamespace() != null ?
                    purl.getNamespace() + "/" + purl.getName() :
                    purl.getName();
            return String.format(purlType.getUrlPattern(), fullName);
        } catch (Exception e) {
            log.debug("Failed to convert purl to URL: {}", purl, e);
            return null;
        }
    }

    private static PurlType findPurlType(String type) {
        for (PurlType purlType : PurlType.values()) {
            if (purlType.getType().equals(type)) {
                return purlType;
            }
        }
        throw new IllegalArgumentException(
                String.format("Unsupported package type: %s", type)
        );
    }
}