package com.scanoss.settings;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@SuperBuilder
public class Rule {
    private String path;
    private String purl;

    public void setPurl(String purl) throws MalformedPackageURLException {
        new PackageURL(purl);
        this.purl = purl;
    }

}


