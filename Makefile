
# HELP
# This will output the help for each task
# thanks to https://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
.PHONY: help

help: ## This help
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

.DEFAULT_GOAL := help

clean:  ## Run maven clean
	@echo "Running package clean..."
	mvn clean

test:  ## Run package tests
	@echo "Running package tests..."
	mvn test

compile:  ## Run maven compile goal
	@echo "Running package compilation..."
	mvn compile

verify:  ## Run maven verify goal
	@echo "Running package verify..."
	mvn verify

package:  ## Run maven package goal
	@echo "Running package..."
	mvn package

deploy:  ## Deploy the package to central repos
	@echo "Deploying latest package..."
	mvn deploy
