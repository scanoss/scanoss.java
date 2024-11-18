package com.scanoss.settings;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.google.gson.annotations.SerializedName;
import com.scanoss.dto.ScanFileResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
public class BomConfiguration {
    private Bom bom;

    @Data
    public static class Bom {
        private List<Rule> include;
        private List<Rule> remove;
        private List<ReplaceRule> replace;
    }

    @Data
    public static class Rule {
        private String path;
        private String purl;

        public void setPurl(String purl) {
            validatePurl(purl);
            this.purl = purl;
        }

        private void validatePurl(String purl) {
            if (purl != null && !purl.trim().isEmpty()) {
                try {
                    new PackageURL(purl);
                } catch (MalformedPackageURLException e) {
                    throw new IllegalArgumentException("Invalid PURL: " + e.getMessage());
                }
            }
        }

    }

    @EqualsAndHashCode(callSuper = true)
    //NOTE: This will check both 'replaceWith' AND the parent class fields (path and purl) when comparing objects
    @Data
    public static class ReplaceRule extends Rule {
        @SerializedName("replace_with")
        private String replaceWith;
        private String license;
    }

}
