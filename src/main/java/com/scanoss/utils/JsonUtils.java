// SPDX-License-Identifier: MIT
/*
 * Copyright (c) 2023, SCANOSS
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
package com.scanoss.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.scanoss.dto.ScanFileDetails;
import com.scanoss.dto.ScanFileResult;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JSON Processing Utility Class
 * <p>
 * Provide a set of utility methods for manipulating JSON details from the SCANOSS service
 * </p>
 */
@Slf4j
public class JsonUtils {

    // Custom list type for decoding Scan Results
    private static final Type scanDetailslistType = new TypeToken<List<ScanFileDetails>>() {
    }.getType();

    private static final Gson gson = new Gson();

    /**
     * Pretty Print the given JSON Object to the specified Writer
     *
     * @param jsonObject JSON Object
     * @param writer     Print Writer (optional - default: STDOUT)
     */
    public static void writeJsonPretty(@NonNull JsonObject jsonObject, PrintWriter writer) {
        if (writer == null) {
            log.debug("No writer specified. Using STDOUT.");
            writer = new PrintWriter(System.out);
        }
        writer.println(toJsonPretty(jsonObject));
    }

    /**
     * Convert the given JSON Object to a pretty string
     *
     * @param jsonObject JSON object
     * @return prettified string
     */
    public static String toJsonPretty(@NonNull JsonObject jsonObject) {
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        return gson.toJson(sortJsonObject(jsonObject));
    }

    /**
     * Recursively sort the JSON object based on key name
     *
     * @param jsonObject JSON object to sort
     * @return sorted object
     */
    public static JsonObject sortJsonObject(JsonObject jsonObject) {
        List<String> keySet = jsonObject.keySet().stream().sorted().collect(Collectors.toList());
        JsonObject temp = new JsonObject();
        for (String key : keySet) {
            JsonElement ele = jsonObject.get(key);
            if (ele.isJsonObject()) {
                ele = sortJsonObject(ele.getAsJsonObject());
                temp.add(key, ele);
            } else if (ele.isJsonArray()) {
                temp.add(key, sortJsonArray(ele.getAsJsonArray()));
            } else if (ele.isJsonPrimitive()) {
                temp.add(key, ele.getAsJsonPrimitive());
            } else if ( ele.isJsonNull()) {
                temp.add(key, ele.getAsJsonNull());
            } else {
                log.debug("Unknown Element Type found: {} - {}", key, ele);
                temp.add(key, ele);
            }
        }
        return temp;
    }

    /**
     * Sort the given JSON Array (based on key)
     *
     * @param jsonArray JSON Array to sort
     * @return Sorted array
     */
    private static JsonArray sortJsonArray(JsonArray jsonArray) {
        JsonArray tempArray = new JsonArray();
        jsonArray.forEach(e -> {
            if (e.isJsonObject()) {
                tempArray.add(sortJsonObject(e.getAsJsonObject()));
            } else if (e.isJsonArray()) {
                tempArray.add(sortJsonArray(e.getAsJsonArray()));
            } else {
                tempArray.add(e);
            }
        });
        return tempArray;
    }

    /**
     * Convert a list of RAW JSON strings into a list of JSON Objects
     *
     * @param results list of JSON strings
     * @return List of JSON Objects
     * @throws JsonParseException    JSON Parsing failed
     * @throws IllegalStateException JSON field is not of JSON Object type
     */
    public static List<JsonObject> toJsonObjects(@NonNull List<String> results) throws JsonParseException, IllegalStateException {
        List<JsonObject> jsonObjects = new ArrayList<>(results.size());
        results.forEach(r -> jsonObjects.add(toJsonObject(r)));
        return jsonObjects;
    }

    /**
     * Convert the given JSON String to a JSON Object
     *
     * @param result JSON string
     * @return JSON Object
     * @throws JsonParseException    JSON Parsing failed
     * @throws IllegalStateException JSON field is not of JSON Object type
     */
    public static JsonObject toJsonObject(@NonNull String result) throws JsonParseException, IllegalStateException {
        return JsonParser.parseString(result).getAsJsonObject();
    }

    /**
     * Join a list of JSON Objects into a single root object
     *
     * @param jsonObjects List of JSON Objects
     * @return combined JSON Object
     */
    public static JsonObject joinJsonObjects(@NonNull List<JsonObject> jsonObjects) {
        JsonObject root = new JsonObject();
        jsonObjects.forEach(j -> j.keySet().forEach(f -> root.add(f, j.get(f))));
        return root;
    }

    /**
     * Convert a list of RAW JSON results to a list of Scan File Result objects
     *
     * @param results List of RAW results
     * @return List of Scan File Results
     * @throws JsonParseException    JSON Parsing failed
     * @throws IllegalStateException JSON field is not of JSON Object type
     */
    public static List<ScanFileResult> toScanFileResults(@NonNull List<String> results) throws JsonParseException, IllegalStateException {

        List<JsonObject> jsonObjects = toJsonObjects(results);
        List<ScanFileResult> scanFileResults = new ArrayList<>(jsonObjects.size());
        Gson gson = new Gson();
        jsonObjects.forEach(j -> j.keySet().forEach(f -> {
            List<ScanFileDetails> fileDetails = gson.fromJson(j.get(f).toString(), scanDetailslistType);
            scanFileResults.add(new ScanFileResult(f, fileDetails));
        }));
        return scanFileResults;
    }


    /**
     * Convert a list of ScanFileResult objects to a list of raw JSON strings
     *
     * @param results List of ScanFileResult objects to convert
     * @return List of raw JSON strings
     * @throws JsonParseException    JSON Parsing failed
     * @throws IllegalStateException JSON field is not of JSON Object type
     */
    public static List<String> toRawJsonString(@NonNull List<ScanFileResult> results) throws JsonParseException, IllegalStateException {
        List<String> rawJsonStrings = new ArrayList<>(results.size());
        Gson gson = new Gson();

        results.forEach(result -> {
            JsonObject jsonObject = new JsonObject();
            JsonElement detailsJson = gson.toJsonTree(result.getFileDetails());
            jsonObject.add(result.getFilePath(), detailsJson);
            rawJsonStrings.add(jsonObject.toString());
        });

        return rawJsonStrings;
    }

    /**
     * Converts a list of ScanFileResult objects into a JSON object where the file paths are keys
     * and the corresponding file details are the values
     *
     * @param scanFileResults List of ScanFileResult objects to convert
     * @return JsonObject containing file paths as keys and file details as JSON elements
     */
    public static JsonObject toScanFileResultJsonObject(List<ScanFileResult> scanFileResults) {
        JsonObject root = new JsonObject();
        Gson gson = new Gson();

        scanFileResults.forEach(result -> {
            JsonElement detailsJson = gson.toJsonTree(result.getFileDetails());
            root.add(result.getFilePath(), detailsJson);
        });

        return root;
    }


    /**
     * Convert the given JSON Object to a list of Scan File Results
     *
     * @param jsonObject JSON Object
     * @return List of Scan File Results
     */
    public static List<ScanFileResult> toScanFileResultsFromObject(@NonNull JsonObject jsonObject) {
        List<ScanFileResult> results = new ArrayList<>(jsonObject.keySet().size());
        Gson gson = new Gson();
        jsonObject.keySet().forEach(f -> {
            List<ScanFileDetails> fileDetails = gson.fromJson(jsonObject.get(f).toString(), scanDetailslistType);
            results.add(new ScanFileResult(f, fileDetails));
        });
        return results;
    }



    /**
     * Determine if the given string is a boolean true/false
     *
     * @param value String value to check
     * @return <code>true</code> if value is <code>yes</code> or <code>true</code>, <code>false</code> otherwise
     */
    public static boolean checkBooleanString(String value) {
        return value != null && (value.equals("yes") || value.equals("true"));
    }


    /**
     * Converts a Java object to its JSON string representation.
     *
     * @param obj The object to be converted to JSON
     * @return A JSON formatted string representation of the object
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * Converts a JSON string back to a Java object of the specified type.
     *
     * @param json The JSON string to be converted
     * @param classOfT The class type to convert the JSON into
     * @param <T> The type parameter representing the target class
     * @return An instance of type T populated with the JSON data
     * @throws JsonSyntaxException If the JSON string is malformed
     */
    public static <T> T fromJson(String json, Class<T> classOfT) throws  JsonSyntaxException {
        return gson.fromJson(json, classOfT);
    }

    /**
     * Reads JSON from a file path and converts it to the specified class type.
     *
     * @param path     The file path to read JSON from
     * @param classOfT The class type to convert the JSON to
     * @param <T>      The type parameter for the class
     * @return An instance of the specified class populated with the JSON data
     * @throws IOException If there's an error reading the file
     * @throws JsonSyntaxException If the JSON is malformed
     */
    public static <T> T fromJsonFile(Path path, Class<T> classOfT) throws IOException, JsonSyntaxException {
        String json = Files.readString(path);
        return JsonUtils.fromJson(json, classOfT);
    }
}
