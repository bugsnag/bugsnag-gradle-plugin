.PHONY: install test-fixture check bump

check:
	@./gradlew --continue detekt ktlintCheck test

install:
	@./gradlew -PVERSION_NAME=9.9.9 clean publishToMavenLocal

bump:
ifneq ($(shell git diff --staged),)
	@git diff --staged
	@$(error You have uncommitted changes. Push or discard them to continue)
endif
	@./scripts/bump-version.sh $(VERSION)
