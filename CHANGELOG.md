# Changelog

## 1.2.1 (2026-11-11)

### Enhancements

- Add `releaseStage`, `sourceControl` (`provider`, `repository`, `revision`) and `exclude` options, configurable at the top level or per variant. Unset options fall back to the `bugsnag-cli`/server defaults [#90](https://github.com/bugsnag/bugsnag-gradle-plugin/pull/90)

### Bug Fixes

- Stop passing `--build-uuid` to `create-build`, which `bugsnag-cli` rejects [#90](https://github.com/bugsnag/bugsnag-gradle-plugin/pull/90)
- If `releaseStage` is not set, builds now default to the `production` release stage instead of the variant name [#90](https://github.com/bugsnag/bugsnag-gradle-plugin/pull/90)
- Fix "Task name already exists" build failure when APK splits (ABI or density) are enabled. Build ID tasks are now registered once per variant, and all split outputs share the same build ID [#89](https://github.com/bugsnag/bugsnag-gradle-plugin/pull/89)

## 1.2.0 (2026-07-22)

### Enhancements

- Add support for Android Gradle Plugin 9+[#79](https://github.com/bugsnag/bugsnag-gradle-plugin/pull/79)

## 1.1.1 (2026-03-18)

### Bug Fixes

- Fix: Build UUID mismatch by re-introducing automatic Build UUID generation and resource injection.
- Ensure all dex files are included when calculating fallback Build UUID in `bugsnag-cli`.

### Bug Fixes

- Fix Build UUID mismatch by re-introducing automatic Build UUID generation and resource injection.
- Ensure all dex files are included when calculating fallback Build UUID in `bugsnag-cli`.

## 1.1.0 (2026-03-17)

### Dependencies

Update bugsnag-cli to [v3.9.0](https://github.com/bugsnag/bugsnag-cli/releases/tag/v3.9.0) [#62](git@github.com:bugsnag/bugsnag-gradle-plugin/pull/62)

## 1.0.0 (2025-07-14)

### Enhancements

- Update bugsnag-cli dependency to [v3.2.0](https://github.com/bugsnag/bugsnag-cli/blob/next/CHANGELOG.md#320---2025-07-02) [#51](https://github.com/bugsnag/bugsnag-gradle-plugin/pull/51)

## 0.4.0 (2025-04-15)

### Enhancements

- Automatically upload configured (`bugsnag.variants.*.autoUploadBundle`) bundles when they are created [#45](https://github.com/bugsnag/bugsnag-gradle-plugin/pull/45)
- Automatically create builds for configured bundle variants (`bugsnag.variants.*.autoCreateBuild`) [#45](https://github.com/bugsnag/bugsnag-gradle-plugin/pull/45)
- Update bugsnag-cli dependency to [v3.0.0](https://github.com/bugsnag/bugsnag-cli/blob/next/CHANGELOG.md#300---2025-04-07) [47](https://github.com/bugsnag/bugsnag-gradle-plugin/pull/47)

## 0.3.0 (2024-10-23)

### Enhancements

- Update bugsnag-cli dependency to [v2.6.2](https://github.com/bugsnag/bugsnag-cli/blob/main/CHANGELOG.md#262-2024-10-17) [38](https://github.com/bugsnag/bugsnag-gradle-plugin/pull/38)

## 0.2.0 (2024-10-09)

### Enhancements

- Update bugsnag-cli dependency to [v2.6.1](https://github.com/bugsnag/bugsnag-cli/blob/next/CHANGELOG.md#261-2024-09-18) [35](https://github.com/bugsnag/bugsnag-gradle-plugin/pull/35)

## 0.1.0 (2024-03-27)

Initial Release of BugSnag Gradle Plugin

