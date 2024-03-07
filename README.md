<div align="center">
  <a href="https://www.bugsnag.com/platforms/android">
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset="https://assets.smartbear.com/m/3dab7e6cf880aa2b/original/BugSnag-Repository-Header-Dark.svg">
      <img alt="SmartBear BugSnag logo" src="https://assets.smartbear.com/m/3945e02cdc983893/original/BugSnag-Repository-Header-Light.svg">
    </picture>
  </a>
  <h1>Gradle plugin</h1>
</div>

[![Documentation](https://img.shields.io/badge/documentation-latest-blue.svg)](https://https://docs.bugsnag.com/build-integrations/gradle-plugin/)
[![Build status](https://badge.buildkite.com/4c62eab88eb99b1fcaef1941b5a0e975009d6387efb4ca85cb.svg?branch=main)](https://https://buildkite.com/bugsnag/bugsnag-gradle-plugin)

This Gradle plugin provides a wrapper of the [bugsnag-cli](https://github.com/bugsnag/bugsnag-cli) for developers using Gradle to build their projects.


## Features

- Automatically upload [ProGuard](https://developer.android.com/tools/help/proguard.html) and [DexGuard](https://www.guardsquare.com/en/dexguard) mapping files to deobfuscate your stacktraces
- Automatically upload [NDK Symbol Maps](https://docs.bugsnag.com/api/ndk-symbol-mapping-upload/) to deobfuscate NDK stacktraces
- Automatically report build information to track the [health of your releases](https://docs.bugsnag.com/product/releases/)

## Getting started

1. [Create a BugSnag account](https://bugsnag.com)
1. Complete the instructions in the [integration guide](https://docs.bugsnag.com/build-integrations/gradle-plugin/) to setup the Gradle plugin
1. Customize your integration using the [configuration options](http://docs.bugsnag.com/build-integrations/gradle-plugin/#configuration-options)

## Support

* [Read the integration guide](https://docs.bugsnag.com/build-integrations/gradle-plugin/) or [configuration options documentation](http://docs.bugsnag.com/build-integrations/gradle-plugin/#configuration-options)
* [Search open and closed issues](https://github.com/bugsnag/bugsnag-gradle-plugin/issues?utf8=✓&q=is%3Aissue) for similar problems
* [Report a bug or request a feature](https://github.com/bugsnag/bugsnag-gradle-plugin/issues/new)
- [BugSnag CLI documentation](https://docs.bugsnag.com/build-integrations/bugsnag-cli/)

## License

The BugSnag Gradle plugin is free software released under the MIT License. See the [LICENSE](./LICENSE) for details.