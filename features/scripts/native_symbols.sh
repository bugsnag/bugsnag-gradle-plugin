#!/usr/bin/env bash
set -e

# Build test app
cd features/fixtures/app
./gradlew :app:clean :app:bugsnagUpload${1}NativeSymbols -x lint --stacktrace $CUSTOM_JVM_ARGS