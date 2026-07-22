.PHONY: install test-fixture check check-agp-matrix bump

check:
	@./gradlew --continue detekt ktlintCheck test

test-fixture:
	@AGP_VERSIONS=9.2.1 ./features/scripts/agp_matrix.sh --features features/aab_upload.feature

check-agp-matrix:
	@./features/scripts/agp_matrix.sh

install:
	@./gradlew -PVERSION_NAME=9.9.9 clean publishToMavenLocal

bump:
ifneq ($(shell git diff --staged),)
	@git diff --staged
	@$(error You have uncommitted changes. Push or discard them to continue)
endif
	@./scripts/bump-version.sh $(VERSION)
