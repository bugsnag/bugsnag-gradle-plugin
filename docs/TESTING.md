# Testing bugsnag-gradle-plugin

This documents code quality checks that are used to improve the quality of bugsnag-gradle-plugin. Most of these can be run all together using `./gradlew check`.

## Unit tests

Unit tests are implemented using [Junit](https://developer.android.com/training/testing/unit-testing/local-unit-tests) and can be run with the following:

`./gradlew test`

Unit tests run on the local JVM and cannot access Android OS classes.

## Running remote end-to-end tests

Please see the [mazerunner docs](MAZERUNNER.md) for information on how to run E2E tests locally.
