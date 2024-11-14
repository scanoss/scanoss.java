package com.scanoss.settings;

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
        private List<Component> include;
        private List<Component> remove;
        private List<ReplaceComponent> replace;
    }

    @Data
    public static class Component {
        private String path;
        private String purl;

        public boolean doesMatchScanFileResult(ScanFileResult scanFileResult) {
            //TODO: Implement matching logic here
            return true;
        }
    }


    @EqualsAndHashCode(callSuper = true)            //NOTE: This will check both 'replaceWith' AND the parent class fields (path and purl) when comparing objects
    @Data
    public static class ReplaceComponent extends Component {
        @SerializedName("replace_with")
        private String replaceWith;
    }
}
