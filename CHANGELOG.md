# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Upcoming changes...

## [0.7.2] - 2024-04-17
### Added
- Added [tagging workflow](.github/workflows/version-tag.yml) to aid release generation

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
[0.7.2]: https://github.com/scanoss/scanoss.java/compare/v0.7.1...v0.7.2