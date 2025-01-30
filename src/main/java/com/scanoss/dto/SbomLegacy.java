package com.scanoss.dto;


import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SbomLegacy {
    private final List<Component> components;

    @Data
    public static class Component {
        private final String purl;
    }
}