// SPDX-License-Identifier: MIT
/*
 * Copyright (c) 2025, SCANOSS
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

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.Assert.*;

@Slf4j
public class TestHeaderFilter {

    private static final String TEST_FILES_DIR = "testing/data/header-files-test";
    private HeaderFilter headerFilter;

    @Before
    public void setUp() {
        log.info("Starting HeaderFilter test cases...");
        headerFilter = new HeaderFilter();
    }

    @Test
    public void TestJavaFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "TokenVerifier.java"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("TokenVerifier.java", contents);
        log.info("TokenVerifier.java => offset={}", offset);
        assertTrue("Java file with Apache license + imports should have offset > 0", offset > 0);
        assertEquals("TokenVerifier.java offset should be 46" , 46, offset);
    }

    @Test
    public void TestPythonFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "results.py"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("results.py", contents);
        log.info("results.py => offset={}", offset);
        assertTrue("Python file with MIT license docstring + imports should have offset > 0", offset > 0);
        assertEquals("result.py offset should be 31" , 31, offset);
    }

    @Test
    public void TestCFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "crc32c.c"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("crc32c.c", contents);
        log.info("crc32c.c => offset={}", offset);
        assertTrue("C file with SPDX + license block + includes should have offset > 0", offset > 0);
        assertEquals("crc32.c offset should be 50" , 50, offset);
    }

    @Test
    public void TestTypeScriptFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "FileModel.ts"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("FileModel.ts", contents);
        log.info("FileModel.ts => offset={}", offset);
        // This file starts directly with imports (no license header)
        assertTrue("TypeScript file with imports should have offset >= 0", offset >= 0);
        assertEquals("FileModel.ts offset should be 7" , 7, offset);
    }

    @Test
    public void TestGoFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "handler.go"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("handler.go", contents);
        log.info("handler.go => offset={}", offset);
        assertTrue("Go file with license + import block should have offset > 0", offset > 0);
        assertEquals("FileModel.ts offset should be 18" , 18, offset);
    }

    @Test
    public void TestRustFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "config.rs"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("config.rs", contents);
        log.info("config.rs => offset={}", offset);
        assertTrue("Rust file with license + use statements should have offset > 0", offset > 0);
        assertEquals("config.rs offset should be 21" , 21, offset);
    }

    @Test
    public void TestKotlinFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "HttpClient.kt"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("HttpClient.kt", contents);
        log.info("HttpClient.kt => offset={}", offset);
        assertTrue("Kotlin file with Apache license + imports should have offset > 0", offset > 0);
        assertEquals("config.rs offset should be 26" , 26, offset);
    }

    @Test
    public void TestScalaFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "DataFrame.scala"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("DataFrame.scala", contents);
        log.info("DataFrame.scala => offset={}", offset);
        assertTrue("Scala file with ASF license + imports should have offset > 0", offset > 0);
        assertEquals("DataFrame.scala offset should be 27" , 27, offset);
    }

    @Test
    public void TestCppFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "StringUtils.hpp"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("StringUtils.hpp", contents);
        log.info("StringUtils.hpp => offset={}", offset);
        assertTrue("C++ header with license + guards + includes should have offset > 0", offset > 0);
        assertEquals("StringUtils.hpp offset should be 16" , 16, offset);
    }

    @Test
    public void TestCSharpFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "ServiceProvider.cs"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("ServiceProvider.cs", contents);
        log.info("ServiceProvider.cs => offset={}", offset);
        assertTrue("C# file with MIT license + usings should have offset > 0", offset > 0);
        assertEquals("ServiceProvider.cs offset should be 12" , 12, offset);
    }

    @Test
    public void TestPhpFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "Router.php"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("Router.php", contents);
        log.info("Router.php => offset={}", offset);
        // PHP <?php tag is not recognized as header/import (matching Python behavior), so offset is 0
        assertEquals("Router.php offset should be 0", 0, offset);
    }

    @Test
    public void TestSwiftFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "Package.swift"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("Package.swift", contents);
        log.info("Package.swift => offset={}", offset);
        assertTrue("Swift file with Apple license + imports should have offset > 0", offset > 0);
        assertEquals("Package.swift offset should be 15" , 15, offset);
    }

    @Test
    public void TestRubyFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "logger.rb"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("logger.rb", contents);
        log.info("logger.rb => offset={}", offset);
        assertTrue("Ruby file with MIT license + requires should have offset > 0", offset > 0);
        assertEquals("logger.rb offset should be 18" , 18, offset);
    }

    @Test
    public void TestPerlFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "parser.pl"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("parser.pl", contents);
        log.info("parser.pl => offset={}", offset);
        assertTrue("Perl file with shebang + license + use statements should have offset > 0", offset > 0);
        assertEquals("parser.pl offset should be 15" , 15, offset);
    }

    @Test
    public void TestRFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "analysis.r"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("analysis.r", contents);
        log.info("analysis.r => offset={}", offset);
        assertTrue("R file with GPL license + library() calls should have offset > 0", offset > 0);
        assertEquals("analysis.r offset should be 23" , 23, offset);
    }

    @Test
    public void TestLuaFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "cache.lua"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("cache.lua", contents);
        log.info("cache.lua => offset={}", offset);
        assertTrue("Lua file with Apache license + require should have offset > 0", offset > 0);
        assertEquals("cache.lua offset should be 14", 14, offset);
    }

    @Test
    public void TestDartFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "widget.dart"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("widget.dart", contents);
        log.info("widget.dart => offset={}", offset);
        assertTrue("Dart file with Flutter license + imports should have offset > 0", offset > 0);
        assertEquals("widget.dart offset should be 12", 12, offset);
    }

    @Test
    public void TestHaskellFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "Parser.hs"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("Parser.hs", contents);
        log.info("Parser.hs => offset={}", offset);
        assertTrue("Haskell file with BSD license + module/import should have offset > 0", offset > 0);
        assertEquals("Parser.hs offset should be 12", 12, offset);
    }

    @Test
    public void TestElixirFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "server.ex"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("server.ex", contents);
        log.info("server.ex => offset={}", offset);
        // Elixir uses # comments but defaults to c_style detection (matching Python behavior), so offset is 0
        assertEquals("server.ex offset should be 0", 0, offset);
    }

    @Test
    public void TestClojureFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "core.clj"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("core.clj", contents);
        log.info("core.clj => offset={}", offset);
        // Clojure uses ;; comments but defaults to c_style detection (matching Python behavior), so offset is 0
        assertEquals("core.clj offset should be 0", 0, offset);
    }

    @Test
    public void TestObjectiveCFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "ViewController.m"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("ViewController.m", contents);
        log.info("ViewController.m => offset={}", offset);
        assertTrue("Objective-C file with Apple license + #import should have offset > 0", offset > 0);
        assertEquals("ViewController.m offset should be 6", 6, offset);
    }

    @Test
    public void TestShellFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "deploy.sh"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("deploy.sh", contents);
        log.info("deploy.sh => offset={}", offset);
        assertTrue("Shell script with shebang + license comments should have offset > 0", offset > 0);
        assertEquals("deploy.sh offset should be 7", 7, offset);
    }

    @Test
    public void TestJavaScriptFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "server.js"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("server.js", contents);
        log.info("server.js => offset={}", offset);
        assertTrue("JavaScript file with MIT license + require should have offset > 0", offset > 0);
        assertEquals("server.js offset should be 23", 23, offset);
    }

    @Test
    public void TestMultilineImportsPythonFile() throws IOException {
        String contents = Files.readString(Path.of(TEST_FILES_DIR, "multiline_imports.py"), StandardCharsets.UTF_8);
        int offset = headerFilter.filter("multiline_imports.py", contents);
        log.info("multiline_imports.py => offset={}", offset);
        assertTrue("Python file with multiline imports should have offset > 0", offset > 0);
        assertEquals("multiline_imports.py offset should be 91", 91, offset);
    }

    @Test
    public void TestUnsupportedExtension() {
        int offset = headerFilter.filter("file.unknown", "some content\nwith lines\n");
        assertEquals("Unsupported extension should return 0", 0, offset);
    }

    @Test
    public void TestEmptyFile() {
        int offset = headerFilter.filter("test.java", "");
        assertEquals("Empty file should return 0", 0, offset);
    }

    @Test
    public void TestNoHeader() {
        String content = "public class MyClass {\n    int x = 1;\n}\n";
        int offset = headerFilter.filter("test.java", content);
        assertEquals("File with no header should return 0", 0, offset);
    }

    @Test
    public void TestMaxLinesLimit() {
        String content = "// Copyright 2024\n" +
                "// Licensed under MIT\n" +
                "// All rights reserved\n" +
                "\n" +
                "import java.util.List;\n" +
                "import java.io.File;\n" +
                "import java.util.Map;\n" +
                "import java.util.Set;\n" +
                "\n" +
                "public class Foo {}\n";

        HeaderFilter limited = new HeaderFilter(5);
        int offset = limited.filter("test.java", content);
        assertEquals("Should cap at maxLines=5", 5, offset);
    }

    @Test
    public void TestDetectLanguage() {
        assertEquals("java", headerFilter.detectLanguage("test.java"));
        assertEquals("python", headerFilter.detectLanguage("test.py"));
        assertEquals("javascript", headerFilter.detectLanguage("server.js"));
        assertEquals("typescript", headerFilter.detectLanguage("component.tsx"));
        assertEquals("go", headerFilter.detectLanguage("main.go"));
        assertEquals("rust", headerFilter.detectLanguage("lib.rs"));
        assertEquals("cpp", headerFilter.detectLanguage("main.cpp"));
        assertEquals("cpp", headerFilter.detectLanguage("header.h"));
        assertEquals("csharp", headerFilter.detectLanguage("Program.cs"));
        assertEquals("kotlin", headerFilter.detectLanguage("Main.kt"));
        assertEquals("scala", headerFilter.detectLanguage("App.scala"));
        assertEquals("php", headerFilter.detectLanguage("index.php"));
        assertEquals("ruby", headerFilter.detectLanguage("app.rb"));
        assertEquals("swift", headerFilter.detectLanguage("App.swift"));
        assertEquals("perl", headerFilter.detectLanguage("script.pl"));
        assertEquals("r", headerFilter.detectLanguage("analysis.r"));
        assertEquals("lua", headerFilter.detectLanguage("init.lua"));
        assertEquals("dart", headerFilter.detectLanguage("main.dart"));
        assertEquals("haskell", headerFilter.detectLanguage("Main.hs"));
        assertEquals("elixir", headerFilter.detectLanguage("app.ex"));
        assertEquals("clojure", headerFilter.detectLanguage("core.clj"));
        assertEquals("cpp", headerFilter.detectLanguage("view.m"));
        assertEquals("python", headerFilter.detectLanguage("script.sh"));
        assertNull(headerFilter.detectLanguage("file.unknown"));
        assertNull(headerFilter.detectLanguage("noextension"));
    }
}