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
    private Bom bom;



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
    public static Settings fromPath(@NotNull Path path) throws IOException {
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            return fromJSON(json);
        } catch (IOException e) {
            throw new IOException("Failed to read settings file: " + path, e);
        }
    }
}

