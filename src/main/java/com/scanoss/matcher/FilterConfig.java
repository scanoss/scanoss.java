package com.scanoss.matcher;

import com.scanoss.settings.Settings;
import lombok.Builder;
import lombok.Getter;

import java.nio.file.PathMatcher;
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
}
