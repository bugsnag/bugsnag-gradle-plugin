#!/usr/bin/env bash
set -e

./gradlew publishToMavenLocal -x test -PVERSION_NAME=9000.0.0-test
