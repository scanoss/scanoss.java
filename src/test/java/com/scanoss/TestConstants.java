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
package com.scanoss;

public class TestConstants {
    static final String SCAN_RESP_SUCCESS = "{\"src/test/java/com/scanoss/TestScanner.java\": [{\"id\": \"none\",\"server\": {\"version\": \"5.2.5\",\"kb_version\": {\"monthly\":\"23.06\", \"daily\":\"23.06.20\"}}}]}";

    // Single file results block - No Match
    static final String jsonResultNoMatchString = "{\"src/test/java/com/scanoss/TestWinnowing.java\": [{\"id\": \"none\",\"server\": {\"version\": \"5.2.5\",\"kb_version\": {\"monthly\":\"23.06\", \"daily\":\"23.06.20\"}}}]}";

    // Single file results block - Match
    static final String jsonResultWithMatchString = "{\"java/com/scanoss/TestFileProcessor.java\": [{\"id\": \"file\",\"lines\": \"all\",\"oss_lines\": \"all\",\"matched\": \"100%\",\"file_hash\": \"81aff7648524d40036020bb7124a8b23\",\"source_hash\": \"81aff7648524d40036020bb7124a8b23\",\"file_url\": \"https://osskb.org/api/file_contents/81aff7648524d40036020bb7124a8b23\",\"purl\": [\"pkg:github/scanoss/scanoss.java\"],\"vendor\": \"scanoss\",\"component\": \"scanoss.java\",\"version\": \"a2b2b54\",\"latest\": \"8b47dda\",\"url\": \"https://github.com/scanoss/scanoss.java\",\"status\": \"pending\",\"release_date\": \"2023-06-30\",\"file\": \"test/java/com/scanoss/TestFileProcessor.java\",\"url_hash\": \"8e1b0b3393b4921b4c22dc887998e712\",\"licenses\": [{\"name\": \"MIT\",\"patent_hints\": \"no\", \"copyleft\": \"no\", \"checklist_url\": \"https://www.osadl.org/fileadmin/checklists/unreflicenses/MIT.txt\",\"osadl_updated\": \"2023-07-02T02:15:00+00:00\",\"source\": \"component_declared\",\"url\": \"https://spdx.org/licenses/MIT.html\"},{\"name\": \"MIT\",\"patent_hints\": \"no\", \"copyleft\": \"no\", \"checklist_url\": \"https://www.osadl.org/fileadmin/checklists/unreflicenses/MIT.txt\",\"osadl_updated\": \"2023-07-02T02:15:00+00:00\",\"source\": \"file_spdx_tag\",\"url\": \"https://spdx.org/licenses/MIT.html\"},{\"name\": \"MIT\",\"patent_hints\": \"no\", \"copyleft\": \"no\", \"checklist_url\": \"https://www.osadl.org/fileadmin/checklists/unreflicenses/MIT.txt\",\"osadl_updated\": \"2023-07-02T02:15:00+00:00\",\"source\": \"license_file\",\"url\": \"https://spdx.org/licenses/MIT.html\"}],\"server\": {\"version\": \"5.2.5\",\"kb_version\": {\"monthly\":\"23.06\", \"daily\":\"23.07.03\"}}}]}";

    // Multi-file results block
    static final String jsonResultsString = "{\n" +
            "  \"scanoss/__init__.py\": [\n" +
            "    {\n" +
            "      \"id\": \"none\",\n" +
            "      \"server\": {\n" +
            "        \"kb_version\": {\n" +
            "          \"daily\": \"23.02.14\",\n" +
            "          \"monthly\": \"23.01\"\n" +
            "        },\n" +
            "        \"version\": \"5.2.3\"\n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"scanoss/api/__init__.py\": [\n" +
            "    {\n" +
            "      \"component\": \"scanoss.py\",\n" +
            "      \"file\": \"scanoss/api/__init__.py\",\n" +
            "      \"file_hash\": \"11d0f68db66ebe04f2e74a761e1ad067\",\n" +
            "      \"file_url\": \"https://osskb.org/api/file_contents/11d0f68db66ebe04f2e74a761e1ad067\",\n" +
            "      \"id\": \"file\",\n" +
            "      \"latest\": \"1.3.7\",\n" +
            "      \"licenses\": [\n" +
            "        {\n" +
            "          \"checklist_url\": \"https://www.osadl.org/fileadmin/checklists/unreflicenses/MIT.txt\",\n" +
            "          \"copyleft\": \"no\",\n" +
            "          \"name\": \"MIT\",\n" +
            "          \"osadl_updated\": \"2023-02-12T03:11:00+00:00\",\n" +
            "          \"patent_hints\": \"no\",\n" +
            "          \"source\": \"component_declared\",\n" +
            "          \"url\": \"https://spdx.org/licenses/MIT.html\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"checklist_url\": \"https://www.osadl.org/fileadmin/checklists/unreflicenses/MIT.txt\",\n" +
            "          \"copyleft\": \"no\",\n" +
            "          \"name\": \"MIT\",\n" +
            "          \"osadl_updated\": \"2023-02-12T03:11:00+00:00\",\n" +
            "          \"patent_hints\": \"no\",\n" +
            "          \"source\": \"file_spdx_tag\",\n" +
            "          \"url\": \"https://spdx.org/licenses/MIT.html\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"checklist_url\": \"https://www.osadl.org/fileadmin/checklists/unreflicenses/MIT.txt\",\n" +
            "          \"copyleft\": \"no\",\n" +
            "          \"name\": \"MIT\",\n" +
            "          \"osadl_updated\": \"2023-02-12T03:11:00+00:00\",\n" +
            "          \"patent_hints\": \"no\",\n" +
            "          \"source\": \"license_file\",\n" +
            "          \"url\": \"https://spdx.org/licenses/MIT.html\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"checklist_url\": \"https://www.osadl.org/fileadmin/checklists/unreflicenses/GPL-2.0-only.txt\",\n" +
            "          \"copyleft\": \"yes\",\n" +
            "          \"incompatible_with\": \"Apache-1.0, Apache-1.1, Apache-2.0, BSD-4-Clause, BSD-4-Clause-UC, FTL, IJG, OpenSSL, Python-2.0, zlib-acknowledgement, XFree86-1.1\",\n" +
            "          \"name\": \"GPL-2.0-only\",\n" +
            "          \"osadl_updated\": \"2023-02-12T03:11:00+00:00\",\n" +
            "          \"patent_hints\": \"yes\",\n" +
            "          \"source\": \"license_file\",\n" +
            "          \"url\": \"https://spdx.org/licenses/GPL-2.0-only.html\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"checklist_url\": \"https://www.osadl.org/fileadmin/checklists/unreflicenses/MIT.txt\",\n" +
            "          \"copyleft\": \"no\",\n" +
            "          \"name\": \"MIT\",\n" +
            "          \"osadl_updated\": \"2023-02-12T03:11:00+00:00\",\n" +
            "          \"patent_hints\": \"no\",\n" +
            "          \"source\": \"scancode\",\n" +
            "          \"url\": \"https://spdx.org/licenses/MIT.html\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"checklist_url\": \"https://www.osadl.org/fileadmin/checklists/unreflicenses/GPL-2.0-or-later.txt\",\n" +
            "          \"copyleft\": \"yes\",\n" +
            "          \"incompatible_with\": \"Apache-1.0, Apache-1.1, Apache-2.0, BSD-4-Clause, BSD-4-Clause-UC, FTL, IJG, OpenSSL, Python-2.0, zlib-acknowledgement, XFree86-1.1\",\n" +
            "          \"name\": \"GPL-2.0-or-later\",\n" +
            "          \"osadl_updated\": \"2023-02-12T03:11:00+00:00\",\n" +
            "          \"patent_hints\": \"yes\",\n" +
            "          \"source\": \"component_declared\",\n" +
            "          \"url\": \"https://spdx.org/licenses/GPL-2.0-or-later.html\"\n" +
            "        }\n" +
            "      ],\n" +
            "      \"lines\": \"all\",\n" +
            "      \"matched\": \"100%\",\n" +
            "      \"oss_lines\": \"all\",\n" +
            "      \"purl\": [\n" +
            "        \"pkg:github/scanoss/scanoss.py\",\n" +
            "        \"pkg:pypi/scanoss\"\n" +
            "      ],\n" +
            "      \"release_date\": \"2022-06-10\",\n" +
            "      \"server\": {\n" +
            "        \"kb_version\": {\n" +
            "          \"daily\": \"23.02.14\",\n" +
            "          \"monthly\": \"23.01\"\n" +
            "        },\n" +
            "        \"version\": \"5.2.3\"\n" +
            "      },\n" +
            "      \"source_hash\": \"11d0f68db66ebe04f2e74a761e1ad067\",\n" +
            "      \"status\": \"pending\",\n" +
            "      \"url\": \"https://github.com/scanoss/scanoss.py\",\n" +
            "      \"url_hash\": \"00e7b8aa29c812271c0fb848959dc119\",\n" +
            "      \"vendor\": \"scanoss\",\n" +
            "      \"version\": \"0.9.0\"\n" +
            "    }\n" +
            "  ]\n" +
            "}\n";
}
