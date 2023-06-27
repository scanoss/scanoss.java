# SCANOSS Java Library
The SCANOSS java package provides a simple, easy to consume library for interacting with SCANOSS APIs/Engine.


## Usage
TBD

Include in a maven project using:
```xml
        <dependency>
            <groupId>com.scanoss</groupId>
            <artifactId>scanoss</artifactId>
            <version>0.1.0</version>
        </dependency>
```

## Development
Before starting with development of this project, please read our [CONTRIBUTING](CONTRIBUTING.md) and [CODE OF CONDUCT](CODE_OF_CONDUCT.md).

### Requirements
Java 11 or higher.

The dependencies can be found in the [pom.xml](pom.xml).

### Package Development

#### Versioning
The version of the package is defined in the [pom.xml](pom.xml) file. Please update this version before packaging/releasing an update.

#### Packaging
To package the library, please run:
```bash
make dist
```

#### Deployment

Then deploy to prod:
```bash
make publish
```
This will deploy the package to [Maven Central](https://maven.org/?).

## Bugs/Features
To request features or alert about bugs, please do so [here](https://github.com/scanoss/scanoss.java/issues).

## Changelog
Details of major changes to the library can be found in [CHANGELOG.md](CHANGELOG.md).
