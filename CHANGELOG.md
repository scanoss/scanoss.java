# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Upcoming changes...
### Added
- `calculateOppositeLineEndingHash()` method in `WinnowingUtils` to compute hash with opposite line endings (Unix ↔ Windows)
- FH2 hash included in WFP output format as `fh2=<hash>`
- Support for detecting CRLF (Windows), LF (Unix), and CR (legacy Mac) line endings

## [0.11.0] - 2025-05-26
### Added
- Path obfuscation and deobfuscation support for enhanced privacy
- CLI obfuscate option (`--obfuscate`)
- Additional Makefile targets for documentation and linting

## [0.10.1] - 2025-02-20
### Added
- Add support to custom filtering rules
- Change logic on remove rule. Mark file as non match instead of deleting the key.

## [0.10.0] - 2025-02-17
### Added
- Add support to skip rule
- Improve file filtering

## [0.9.0] - 2025-02-03
### Added
- Add support to rules: include & ignore

## [0.8.2] - 2025-01-23
### Changed
- Updates scope on dependencies pom.xml
- Publish workflow

## [0.8.0] - 2025-01-21
### Added
- Added [tagging workflow](.github/workflows/version-tag.yml) to aid release generation
- Added variables `DEFAULT_BASE_URL` and `DEFAULT_BASE_URL2` to separate the scan URL from the scan path
- Adds support to rules: replace & remove

## [0.7.1] - 2024-04-12
### Changed
- Update file and winnowing filters
- Remove filter for '.whl' file extensions
- Added dir extension filter

## [0.7.0] - 2024-04-04
### Added
- Add HPSM support

## [0.6.1] - 2024-04-01
### Changed
- Fixed issue with SBOM ingestion

## [0.6.0] - 2024-03-26
### Changed
- Updated free default URL to now point to `https://api.osskb.org`
- Updated premium default URL to now point to `https://api.scanoss.com`

## [0.5.5] - 2023-10-25
### Fixed
- Fixed issue with `processFileList` file path

## [0.5.4] - 2023-10-23
### Added
- Added extra debug information to scanning

## [0.5.3] - 2023-10-03
### Added
- Added symbolic file check (to skip)

## [0.5.2] - 2023-08-14
### Added
- Added support for manual proxy configuration (`--proxy`)

## [0.5.1] - 2023-08-11
### Added
- Added support for scanning a file list (`scanFileList`)
- Added maven support for GraalVM compilation

## [0.5.0] - 2023-07-26
### Added
- Switched to okhttp for REST communication
- Added custom HTTP certificate support (`customCert`)
  - CLI option: `--ca-cert`
### Fixed
- Fixed issue with null json object printing

## [0.4.0] - 2023-07-07

### Added
- Added long snippet generation check limit (`snippetLimit`)
- Added command line option: `--snippet-limit` to support it

## [0.2.0] - 2023-07-04
### Added
- First pass at the following Classes
  - Fingerprinting ([Winnowing](src/main/java/com/scanoss/Winnowing.java))
  - Scanning ([Scanner](src/main/java/com/scanoss/Scanner.java))
  - REST Interface ([ScanApi](src/main/java/com/scanoss/rest/ScanApi.java))
  - JSON Utils ([JsonUtils](src/main/java/com/scanoss/utils/JsonUtils.java))
- CLI ([CommandLine](src/main/java/com/scanoss/cli/CommandLine.java))

[0.2.0]: https://github.com/scanoss/scanoss.java/compare/v0.0.0...v0.2.0
[0.4.0]: https://github.com/scanoss/scanoss.java/compare/v0.2.0...v0.4.0
[0.5.0]: https://github.com/scanoss/scanoss.java/compare/v0.4.0...v0.5.0
[0.5.1]: https://github.com/scanoss/scanoss.java/compare/v0.5.0...v0.5.1
[0.5.2]: https://github.com/scanoss/scanoss.java/compare/v0.5.1...v0.5.2
[0.5.3]: https://github.com/scanoss/scanoss.java/compare/v0.5.2...v0.5.3
[0.5.4]: https://github.com/scanoss/scanoss.java/compare/v0.5.3...v0.5.4
[0.5.5]: https://github.com/scanoss/scanoss.java/compare/v0.5.4...v0.5.5
[0.6.0]: https://github.com/scanoss/scanoss.java/compare/v0.5.5...v0.6.0
[0.6.1]: https://github.com/scanoss/scanoss.java/compare/v0.6.0...v0.6.1
[0.7.0]: https://github.com/scanoss/scanoss.java/compare/v0.6.1...v0.7.0
[0.7.1]: https://github.com/scanoss/scanoss.java/compare/v0.7.0...v0.7.1
[0.8.0]: https://github.com/scanoss/scanoss.java/compare/v0.7.1...v0.8.0
[0.8.1]: https://github.com/scanoss/scanoss.java/compare/v0.8.0...v0.8.1
[0.9.0]: https://github.com/scanoss/scanoss.java/compare/v0.8.1...v0.9.0
[0.10.0]: https://github.com/scanoss/scanoss.java/compare/v0.9.0...v0.10.0
[0.10.1]: https://github.com/scanoss/scanoss.java/compare/v0.10.0...v0.10.1
[0.11.0]: https://github.com/scanoss/scanoss.java/compare/v0.10.1...v0.11.0