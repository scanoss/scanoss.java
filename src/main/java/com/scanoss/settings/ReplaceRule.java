package com.scanoss.settings;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class ReplaceRule extends Rule {
        @SerializedName("replace_with")
        private String replaceWith;
        private String license;
}