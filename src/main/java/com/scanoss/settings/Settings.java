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
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
@Builder
public class Settings {
    private final Bom bom;


    /**
     * Creates a Settings object from a JSON string
     *
     * @param json The JSON string to parse
     * @return A new Settings object
     */
    public static Settings fromJSON(@NotNull String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Settings.class);
    }

    /**
     * Creates a Settings object from a JSON file
     *
     * @param path The path to the JSON file
     * @return A new Settings object
     * @throws IOException If there's an error reading the file
     */
    public static Settings fromPath(@NotNull Path path) {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            return fromJSON(json);
    }
}

