# SCANOSS Java Library

The SCANOSS java package provides a simple, easy to consume library for interacting with SCANOSS APIs.

[![Unit Test](https://github.com/scanoss/scanoss.java/actions/workflows/java-ci.yml/badge.svg)](https://github.com/scanoss/scanoss.java/actions/workflows/java-ci.yml)
[![Release](https://github.com/scanoss/scanoss.java/actions/workflows/release.yml/badge.svg)](https://github.com/scanoss/scanoss.java/actions/workflows/release.yml)

## Usage

The latest version of the package can be found
on [Maven Central](https://central.sonatype.com/artifact/com.scanoss/scanoss).

Include in a maven project using:

```xml
        <dependency>
            <groupId>com.scanoss</groupId>
            <artifactId>scanoss</artifactId>
            <version>0.7.0</version>
        </dependency>
```

And in gradle using:

```
implementation group: 'com.scanoss', name: 'scanoss', version: '0.1.3'
```

Examples of consuming this SDK can be found in the [cli](src/main/java/com/scanoss/cli) package folder.
Specifically, the [ScanCommandLine.java](src/main/java/com/scanoss/cli/ScanCommandLine.java) shows how to instantiate
the `Scanner` class to initiate a scan.

Here is a simple way to initiate a scan using all the SCANOSS defaults:

```java
import com.scanoss.Scanner;

public class Test {
    public static void main(String[] args) {
        Scanner scanner = Scanner.builder().build();
        String file = "test.java";
        String result = scanner.scanFile(file);
    }
}
```

### CLI

The package also ships with a sample CLI. It can be run using the example script [scanoss-cli.sh](scanoss-cli.sh):

```bash
scanos-cli.sh -h
```

### Custom Certificate
In order to connect to a SCANOSS server with a custom (self-signed) certificate,
the keychain will need to be imported onto the CA Certs into the instance of java before proceeding:

```bash
keytool -cacerts -importcert -file custom-key-chain.pem
```

## Development

Before starting with development of this project, please read our [CONTRIBUTING](CONTRIBUTING.md)
and [CODE OF CONDUCT](CODE_OF_CONDUCT.md).

### Requirements

Java 11 or higher.

The dependencies can be found in the [pom.xml](pom.xml).

### Testing

A full set of [unit tests](src/test/java/com/scanoss) are included with the package.

To run tests, using the following command:

```bash
make test
```

### Package Development

#### Versioning

The version of the package is defined in the [pom.xml](pom.xml) file. Please update this version before
packaging/releasing an update.

The following commands are provided for incrementing version:

```bash
make inc_path
make inc_minor
make inc_major
```

#### Packaging

To package the library, please run:

```bash
make package
```

#### Dependency Updates

Check for dependency updates:

```bash
mvn versions:display-dependency-updates
```

#### Deployment

Then deploy to prod:

```bash
make deploy
```

This will deploy the package to [Maven Central](https://central.sonatype.com/artifact/com.scanoss/scanoss).

Alternatively pushing a tagged version to GitHub will trigger
the [Release](https://github.com/scanoss/scanoss.java/actions/workflows/release.yml) Action to automate the deployment.

## Bugs/Features

To request features or alert about bugs, please do so [here](https://github.com/scanoss/scanoss.java/issues).

## Changelog

Details of major changes to the library can be found in [CHANGELOG.md](CHANGELOG.md).
