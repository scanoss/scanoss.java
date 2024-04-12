package com.scanoss;

import java.util.Arrays;
import java.util.List;

/**
 * SCANOSS Constants Class
 * <p>
 * Contains shared static values to be used by the package classes.
 * </p>
 */
@SuppressWarnings("SpellCheckingInspection")
public class ScanossConstants {
    /**
     * Default timeout for HTTP communication
     */
    public static final int DEFAULT_TIMEOUT = 120;
    /**
     * Default number of worker threads to use then processing files
     */
    public static final int DEFAULT_WORKER_THREADS = 5;
    /**
     * Default number of times to retry sending data to HTTP
     */
    public static final int DEFAULT_HTTP_RETRY_LIMIT = 5;

    static final int GRAM = 30; // Winnowing Gram size. Do NOT Modify
    static final int WINDOW = 64; // Winnowing Window size. Do NOT Modify
    static final long MAX_CRC32 = 4294967296L;
    static final int MIN_FILE_SIZE = 256; // Minimum size for a file to be considered for snippet generation
    static final int MAX_LONG_LINE_CHARS = 1000; // Maximum length of a single source line to be considered source code

    // File extensions to ignore snippets for
    static final List<String> SKIP_SNIPPET_EXT = Arrays.asList(
            ".exe", ".zip", ".tar", ".tgz", ".gz", ".7z", ".rar", ".jar", ".war", ".ear", ".class", ".pyc",
            ".o", ".a", ".so", ".obj", ".dll", ".lib", ".out", ".app", ".bin",
            ".lst", ".dat", ".json", ".htm", ".html", ".xml", ".md", ".txt",
            ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".odt", ".ods", ".odp", ".pages", ".key", ".numbers",
            ".pdf", ".min.js", ".mf", ".sum", ".woff", ".woff2", ".xsd", ".pom", ".whl"
    );

    // Folders to skip
    static final List<String> FILTERED_DIRS = Arrays.asList(
            "nbproject", "nbbuild", "nbdist", "__pycache__", "venv", "_yardoc", "eggs", "wheels", "htmlcov",
            "__pypackages__", "target"
    );

    // Folder endings to skip
    static final List<String> FILTERED_DIR_EXT = List.of(".egg-info");


    // File extensions to skip
    static final List<String> FILTERED_EXTENSIONS = Arrays.asList(
            ".1", ".2", ".3", ".4", ".5", ".6", ".7", ".8", ".9", ".ac", ".adoc", ".am",
            ".asciidoc", ".bmp", ".build", ".cfg", ".chm", ".class", ".cmake", ".cnf",
            ".conf", ".config", ".contributors", ".copying", ".crt", ".csproj", ".css",
            ".csv", ".dat", ".data", ".doc", ".docx", ".dtd", ".dts", ".iws", ".c9", ".c9revisions",
            ".dtsi", ".dump", ".eot", ".eps", ".geojson", ".gdoc", ".gif",
            ".glif", ".gmo", ".gradle", ".guess", ".hex", ".htm", ".html", ".ico", ".iml",
            ".in", ".inc", ".info", ".ini", ".ipynb", ".jpeg", ".jpg", ".json", ".jsonld", ".lock",
            ".log", ".m4", ".map", ".markdown", ".md", ".md5", ".meta", ".mk", ".mxml",
            ".o", ".otf", ".out", ".pbtxt", ".pdf", ".pem", ".phtml", ".plist", ".png",
            ".po", ".ppt", ".prefs", ".properties", ".pyc", ".qdoc", ".result", ".rgb",
            ".rst", ".scss", ".sha", ".sha1", ".sha2", ".sha256", ".sln", ".spec", ".sql",
            ".sub", ".svg", ".svn-base", ".tab", ".template", ".test", ".tex", ".tiff",
            ".toml", ".ttf", ".txt", ".utf-8", ".vim", ".wav", ".woff", ".woff2", ".xht",
            ".xhtml", ".xls", ".xlsx", ".xml", ".xpm", ".xsd", ".xul", ".yaml", ".yml", ".wfp",
            ".editorconfig", ".dotcover", ".pid", ".lcov", ".egg", ".manifest", ".cache", ".coverage", ".cover",
            ".gem", ".lst", ".pickle", ".pdb", ".gml", ".pot", ".plt",
            // File endings
            "-doc", "changelog", "config", "copying", "license", "authors", "news", "licenses", "notice",
            "readme", "swiftdoc", "texidoc", "todo", "version", "ignore", "manifest", "sqlite", "sqlite3"
    );

    // Files to skip
    static final List<String> FILTERED_FILES = Arrays.asList(
            "gradlew", "gradlew.bat", "mvnw", "mvnw.cmd", "gradle-wrapper.jar", "maven-wrapper.jar",
            "thumbs.db", "babel.config.js", "license.txt", "license.md", "copying.lib", "makefile"
    );
}
