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
import dto.ScanFileDetails;
import dto.ScanFileResult;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.lang.reflect.Type;
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
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
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
            } else
                temp.add(key, ele.getAsJsonPrimitive());
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
}
