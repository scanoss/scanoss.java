# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Upcoming changes...

## [0.5.0] - 2023-07-25
### Added
- Switched to okhttp for REST communication
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
