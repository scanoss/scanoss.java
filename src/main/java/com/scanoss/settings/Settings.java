// SPDX-License-Identifier: MIT
/*
 * Copyright (c) 2024, SCANOSS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.scanoss.settings;

import com.google.gson.Gson;
import com.scanoss.dto.SbomLegacy;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the SCANOSS scanner settings configuration.
 * Provides functionality to load and manage scanner settings from JSON files or strings.
 * Settings include BOM (Bill of Materials) rules for modifying components before and after scanning.
 */
@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Settings {
    /**
     * The Bill of Materials (BOM) configuration containing rules for component handling.
     * Includes rules for including, ignoring, removing, and replacing components
     * during and after the scanning process.
     */
    private final @Builder.Default Bom bom = Bom.builder().build();
    private final @Builder.Default Skip skip = Skip.builder().build();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Skip {
        private final @Builder.Default Patterns patterns = Patterns.builder().build();
        private final @Builder.Default Sizes sizes = Sizes.builder().build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Patterns {
        private final @Builder.Default List<String> scanning = new ArrayList<>();
        private final @Builder.Default List<String> fingerprinting = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sizes {
        private final @Builder.Default List<SizeRule> scanning = new ArrayList<>();
        private final @Builder.Default List<SizeRule> fingerprinting = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SizeRule {
        private List<String> patterns;
        private long min;
        private long max;
    }


    /**
     * Converts modern rule-based configuration to legacy SBOM format.
     * This method is needed as a temporary solution because the current engine
     * does not yet support the new settings.json format with 'include' and 'ignore' properties.
     * Instead, it converts these rules to the legacy include/blacklist style.
     *
     * @param rules List of inclusion/exclusion rules to be converted
     * @return SbomLegacy object containing the converted rules as components
     */
    public SbomLegacy getLegacySbom(List<Rule> rules) {
        List<SbomLegacy.Component> c = new ArrayList<>();
        rules.forEach(rule -> c.add(new SbomLegacy.Component( rule.getPurl())));
        return SbomLegacy.builder().components(c).build();
    }

    /**
     * Creates a Settings object from a JSON string
     *
     * @param json The JSON string to parse
     * @return A new Settings object
     */
    public static Settings createFromJsonString(@NonNull String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Settings.class);
    }

    /**
     * Creates a Settings object from a JSON file
     *
     * @param path The path to the JSON file
     * @return A new Settings object
     */
    public static Settings createFromPath(@NonNull Path path) {
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            return createFromJsonString(json);
        } catch (IOException e) {
            log.error("Cannot read settings file - {}", e.getMessage());
            return null;
        }

    }


    public List<String> getScanningIgnorePattern()  {
        return this.skip.getPatterns().getScanning();
    }

}

