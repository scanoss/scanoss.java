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
package com.scanoss;

import com.scanoss.settings.Settings;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

@Slf4j
public class TestSettings {
    private Path existingSettingsPath;
    private Path nonExistentSettingsPath;

    @Before
    public void Setup() throws URISyntaxException {
        log.info("Starting Settings test cases...");
        log.debug("Logging debug enabled");
        log.trace("Logging trace enabled");

        // Check if resource exists before attempting to get its path
        var resource = getClass().getClassLoader().getResource("scanoss.json");
        if (resource == null) {
            throw new IllegalStateException(
                    "Required test resource 'scanoss.json' not found. Please ensure it exists in src/test/resources/data/"
            );
        }

        existingSettingsPath = Paths.get(resource.toURI());
        nonExistentSettingsPath = Paths.get("non-existent-settings.json");

        // Verify the file actually exists
        if (!Files.exists(existingSettingsPath)) {
            throw new IllegalStateException(
                    "Test file exists as resource but cannot be accessed at path: " +
                            existingSettingsPath
            );
        }
    }


    @Test
    public void testSettingsFromExistingFile() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);


        Settings settings = Settings.createFromPath(existingSettingsPath);
        assertNotNull("Settings should not be null", settings);

        assertEquals("scanner.c", settings.getBom().getRemove().get(0).getPath());
        assertEquals("pkg:github/scanoss/scanner.c", settings.getBom().getRemove().get(0).getPurl());



        log.info("Finished {} -->", methodName);
    }

    @Test
    public void testSettingsFromNonExistentFile() throws IOException {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("<-- Starting {}", methodName);

        Settings settings = Settings.createFromPath(nonExistentSettingsPath);

        assertNull("Settings should be null", settings);

        log.info("Finished {} -->", methodName);
    }

}
