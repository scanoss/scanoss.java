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
            "  \"CMSsite/admin/js/npm.js\": [\n" +
            "    {\n" +
            "      \"component\": \"bootstrap\",\n" +
            "      \"copyrights\": [\n" +
            "        {\n" +
            "          \"name\": \"Copyright (c) 2011-2021 Twitter; Inc.\",\n" +
            "          \"source\": \"license_file\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \"Copyright (c) 2011-2022 The Bootstrap Authors\",\n" +
            "          \"source\": \"license_file\"\n" +
            "        }\n" +
            "      ],\n" +
            "      \"cryptography\": [],\n" +
            "      \"dependencies\": [],\n" +
            "      \"file\": \"dist/js/npm.js\",\n" +
            "      \"file_hash\": \"ccb7f3909e30b1eb8f65a24393c6e12b\",\n" +
            "      \"file_url\": \"https://osskb.org/api/file_contents/ccb7f3909e30b1eb8f65a24393c6e12b\",\n" +
            "      \"health\": {\n" +
            "        \"creation_date\": \"2011-07-29\",\n" +
            "        \"issues\": 402,\n" +
            "        \"last_push\": \"2023-05-13\",\n" +
            "        \"last_update\": \"2023-05-14\",\n" +
            "        \"stars\": null\n" +
            "      },\n" +
            "      \"id\": \"file\",\n" +
            "      \"latest\": \"3.3.5\",\n" +
            "      \"licenses\": [\n" +
            "        {\n" +
            "          \"checklist_url\": \"https://www.osadl.org/fileadmin/checklists/unreflicenses/MIT.txt\",\n" +
            "          \"copyleft\": \"no\",\n" +
            "          \"name\": \"MIT\",\n" +
            "          \"osadl_updated\": \"2023-05-14T02:12:00+00:00\",\n" +
            "          \"patent_hints\": \"no\",\n" +
            "          \"source\": \"component_declared\",\n" +
            "          \"url\": \"https://spdx.org/licenses/MIT.html\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"checklist_url\": \"https://www.osadl.org/fileadmin/checklists/unreflicenses/MIT.txt\",\n" +
            "          \"copyleft\": \"no\",\n" +
            "          \"name\": \"MIT\",\n" +
            "          \"osadl_updated\": \"2023-05-14T02:12:00+00:00\",\n" +
            "          \"patent_hints\": \"no\",\n" +
            "          \"source\": \"license_file\",\n" +
            "          \"url\": \"https://spdx.org/licenses/MIT.html\"\n" +
            "        }\n" +
            "      ],\n" +
            "      \"lines\": \"all\",\n" +
            "      \"matched\": \"100%\",\n" +
            "      \"oss_lines\": \"all\",\n" +
            "      \"provenance\": \"United States\",\n" +
            "      \"purl\": [\n" +
            "        \"pkg:github/twbs/bootstrap\",\n" +
            "        \"pkg:sourceforge/bootstrap.mirror\",\n" +
            "        \"pkg:googlesource/external/github.com/twbs/bootstrap\",\n" +
            "        \"pkg:npm/mip-bootstrap\",\n" +
            "        \"pkg:npm/monsta-bootstrap\",\n" +
            "        \"pkg:npm/myoneui\",\n" +
            "        \"pkg:npm/livn-bootstrap\",\n" +
            "        \"pkg:npm/abbstrap\",\n" +
            "        \"pkg:npm/co_bootstrap\",\n" +
            "        \"pkg:npm/cumulon-bootstrap\"\n" +
            "      ],\n" +
            "      \"quality\": [\n" +
            "        {\n" +
            "          \"score\": \"4/5\",\n" +
            "          \"source\": \"best_practices\"\n" +
            "        }\n" +
            "      ],\n" +
            "      \"release_date\": \"2014-10-29\",\n" +
            "      \"server\": {\n" +
            "        \"elapsed\": \"1.655429s\",\n" +
            "        \"flags\": \"0\",\n" +
            "        \"hostname\": \"p14\",\n" +
            "        \"kb_version\": {\n" +
            "          \"daily\": \"23.05.15\",\n" +
            "          \"monthly\": \"23.04\"\n" +
            "        },\n" +
            "        \"version\": \"5.2.5\"\n" +
            "      },\n" +
            "      \"source_hash\": \"ccb7f3909e30b1eb8f65a24393c6e12b\",\n" +
            "      \"status\": \"pending\",\n" +
            "      \"url\": \"https://github.com/twbs/bootstrap\",\n" +
            "      \"url_hash\": \"1363e882db14cc94d4d5011248a86d20\",\n" +
            "      \"vendor\": \"twbs\",\n" +
            "      \"version\": \"3.3.0\",\n" +
            "      \"vulnerabilities\": [\n" +
            "        {\n" +
            "          \"CVE\": \"CVE-2018-14042\",\n" +
            "          \"ID\": \"GHSA-7mvr-5x2g-wfc8\",\n" +
            "          \"introduced\": \">=0\",\n" +
            "          \"patched\": \"<4.1.2\",\n" +
            "          \"reported\": \"2023-04-11\",\n" +
            "          \"severity\": \"MODERATE\",\n" +
            "          \"source\": \"github_advisories\",\n" +
            "          \"summary\": \"Bootstrap Cross-site Scripting vulnerability\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"CVE\": \"CVE-2019-8331\",\n" +
            "          \"ID\": \"cpe:2.3:a:getbootstrap:bootstrap:3.3.0:*:*:*:*:*:*:*\",\n" +
            "          \"introduced\": \"\",\n" +
            "          \"patched\": \"3.3.0\",\n" +
            "          \"reported\": \"\",\n" +
            "          \"severity\": \"MEDIUM\",\n" +
            "          \"source\": \"nvd\",\n" +
            "          \"summary\": \"https://nvd.nist.gov/vuln/detail/CVE-2019-8331\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"CVE\": \"CVE-2019-8331\",\n" +
            "          \"ID\": \"cpe:2.3:a:getbootstrap:bootstrap:3.3.5:*:*:*:*:*:*:*\",\n" +
            "          \"introduced\": \"\",\n" +
            "          \"patched\": \"3.3.5\",\n" +
            "          \"reported\": \"\",\n" +
            "          \"severity\": \"MEDIUM\",\n" +
            "          \"source\": \"nvd\",\n" +
            "          \"summary\": \"https://nvd.nist.gov/vuln/detail/CVE-2019-8331\"\n" +
            "        }\n" +
            "      ]\n" +
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

    // Custom self-signed certificate
    static final String customSelfSignedCertificate =
            "-----BEGIN CERTIFICATE-----\n" +
            "MIICATCCAWoCCQCp8OJx30PXlDANBgkqhkiG9w0BAQUFADBFMQswCQYDVQQGEwJB\n" +
            "VDEQMA4GA1UEBwwHVmlsbGFjaDEQMA4GA1UECwwHU0NBTk9TUzESMBAGA1UEAwwJ\n" +
            "bG9jYWxob3N0MB4XDTIzMDUxNzA4NDQ1NloXDTI0MDUxNjA4NDQ1NlowRTELMAkG\n" +
            "A1UEBhMCQVQxEDAOBgNVBAcMB1ZpbGxhY2gxEDAOBgNVBAsMB1NDQU5PU1MxEjAQ\n" +
            "BgNVBAMMCWxvY2FsaG9zdDCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAr+sn\n" +
            "tnzteHLVlDJPUmiIcxCeR0+fWDTEAbZy4BVoIf3GcqF9Wd4dJv7ecAlX+3dtg8KW\n" +
            "rNjGtswmQmQjnYesHBRV7bA8IhwJzFdEI9+Egsqjrti598/ZigG/J26FoVTK5QC6\n" +
            "yKnrNWwpl+cm2jt8EoslxoCWdkynf6LA32XU7hUCAwEAATANBgkqhkiG9w0BAQUF\n" +
            "AAOBgQB5Gknh1FKoZNUxeeW/hpyLERMBjNT0zmdSGOAIrAm+MarywQ16vUlek1yB\n" +
            "T8khRt8nVmv3ELOr5/KaRKJzYCUzCtUQyMsLQj9DA3XMLFS+PLGB0WGW5C6BwsRT\n" +
            "fAl9Hb0H+ljShqzwQDeFBfVQRNe6z/UXa9uk4bCkPEX8h5Kurw==\n" +
            "-----END CERTIFICATE-----\n";
}
