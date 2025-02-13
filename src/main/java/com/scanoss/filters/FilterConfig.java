package com.scanoss.filters;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class FilterConfig {

    @Builder.Default
    private List<String> gitIgnorePatterns = new ArrayList<>();

    @Builder.Default
    private List<String> antPatterns = new ArrayList<>();

    @Builder.Default
    private Boolean hiddenFilesFolders = false;

    @Builder.Default
    private Boolean allFilesFolders = false;

    @Builder.Default
    private Boolean allFolders = false;

    @Builder.Default
    private Boolean allExtensions = false;
}
