
# HELP
# This will output the help for each task
# thanks to https://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
.PHONY: help

help: ## This help
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

.DEFAULT_GOAL := help

clean:  ## Run maven clean
	@echo "Running package clean..."
	./mvnw clean

test:  ## Run package tests
	@echo "Running package tests..."
	./mvnw test

dep_update_check:  ## Run maven versions command to check for dependency updates
	@echo "Running dependency update check..."
	./mvnw versions:display-dependency-updates

dep_use_next_version:  ## Run maven to update dependencies to next available version
	@echo "Running dependency update check..."
	./mvnw versions:use-next-versions

compile:  ## Run maven compile goal
	@echo "Running package compilation..."
	./mvnw compile

verify:  ## Run maven verify goal
	@echo "Running package verify..."
	./mvnw verify

package:  ## Run maven package goal
	@echo "Running package..."
	./mvnw package

deploy:  ## Deploy the package to central repos
	@echo "Deploying latest package..."
	./mvnw deploy

native:  ## Run maven native binary build
	@echo "Running native package..."
	./mvnw -Pnative -DskipTests package -X

inc_patch:  ## Increment the patch version on pom.xml
	@echo "Incrementing patch version..."
	./mvnw build-helper:parse-version versions:set -DnewVersion='$${parsedVersion.majorVersion}.$${parsedVersion.minorVersion}.$${parsedVersion.nextIncrementalVersion}' versions:commit

inc_minor:  ## Increment the minor version on pom.xml
	@echo "Incrementing minor version..."
	./mvnw build-helper:parse-version versions:set -DnewVersion='$${parsedVersion.majorVersion}.$${parsedVersion.nextMinorVersion}.$${parsedVersion.incrementalVersion}' versions:commit

inc_major:  ## Increment the major version on pom.xml
	@echo "Incrementing major version..."
	./mvnw build-helper:parse-version versions:set -DnewVersion='$${parsedVersion.nextMajorVersion}.$${parsedVersion.minorVersion}.$${parsedVersion.incrementalVersion}' versions:commit

version:  ## Show the current version of the package
	@echo "Getting package version..."
	VER=$(shell ./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
