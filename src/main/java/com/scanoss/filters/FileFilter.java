package com.scanoss.filters;
import com.scanoss.ScanossConstants;
import lombok.Builder;

import java.nio.file.Path;
import java.util.function.Predicate;

@Builder
public class FileFilter extends BaseFilter {

    @Builder.Default  // Tells Lombok this field has a default initialization
    private final FilterConfig filterConfig = FilterConfig.builder().build();

    // Use @Builder constructor annotation to tell Lombok to use this constructor
    protected FileFilter(FilterConfig filterConfig) {
        super(filterConfig);
        this.filterConfig = filterConfig;
    }

    public Predicate<Path> get() {
        // First call super's build to initialize base filters
        Predicate<Path> baseFilter = this.baseSkipFilter;

        if (!config.getHiddenFilesFolders()) {
            Predicate<Path> skipHiddenFilter = p -> p.getFileName().toString().startsWith(".");
            baseFilter = baseFilter
                    .or(skipHiddenFilter);
        }

        if(config.getAllExtensions()){
            Predicate <Path> allExtensionsFilter = p -> false;
            baseFilter = baseFilter.or(allExtensionsFilter);
        }

       Predicate<Path> filterFiles = p -> ScanossConstants.FILTERED_FILES.stream()
              .anyMatch(d -> p.getFileName().toString().toLowerCase().equals(d));

        baseFilter = baseFilter.or(filterFiles);

        Predicate<Path> filterExtensions = p -> ScanossConstants.FILTERED_EXTENSIONS.stream()
                .anyMatch(d -> p.getFileName().toString().toLowerCase().endsWith(d));
        baseFilter = baseFilter.or(filterExtensions);


        return baseFilter;
    }
}