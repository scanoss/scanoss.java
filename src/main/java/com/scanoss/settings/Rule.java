package com.scanoss.settings;
import com.github.packageurl.PackageURL;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@SuperBuilder()
public class Rule {
    private String path;
    private String purl;    //TODO: Add validation with PackageURL
}


