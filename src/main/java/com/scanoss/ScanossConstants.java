package com.scanoss;

import java.util.Arrays;
import java.util.List;

public class ScanossConstants {

    static final int GRAM = 30; // Winnowing Gram size. Do NOT Modify
    static final int WINDOW = 64; // Winnowing Window size. Do NOT Modify
    static final long MAX_CRC32 = 4294967296L;
    static final int MAX_POST_SIZE = 64 * 1024;  // Default max post size
    static final int MIN_FILE_SIZE = 256;

    // File extensions to ignore snippets for
    static final List<String> SKIP_SNIPPET_EXT = Arrays.asList(
            ".exe", ".zip", ".tar", ".tgz", ".gz", ".7z", ".rar", ".jar", ".war", ".ear", ".class", ".pyc",
            ".o", ".a", ".so", ".obj", ".dll", ".lib", ".out", ".app", ".bin",
            ".lst", ".dat", ".json", ".htm", ".html", ".xml", ".md", ".txt",
            ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".odt", ".ods", ".odp", ".pages", ".key", ".numbers",
            ".pdf", ".min.js", ".mf", ".sum"
    );
}
