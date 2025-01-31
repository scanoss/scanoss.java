package com.scanoss.dto;


import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Legacy representation of a Software Bill of Materials (SBOM).
 * This class exists to support backward compatibility with older include/blacklist style
 * configurations while the engine is updated to support the newer include/ignore format
 * from settings.json.
 */
@Data
@Builder
public class SbomLegacy {
    private final List<Component> components;

    /**
     * Represents a component within the legacy SBOM structure.
     * Each component is identified by its Package URL (PURL).
     */
    @Data
    public static class Component {
        private final String purl;
    }
}