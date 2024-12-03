package com.scanoss.settings;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class RemoveRule extends Rule {
        @SerializedName("start_line")
        private final Integer startLine;
        @SerializedName("end_line")
        private final Integer endLine;
}