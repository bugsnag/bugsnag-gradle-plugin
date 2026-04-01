# Releasing a new version

`bugsnag-gradle-plugin` is released via [Sonatype](https://oss.sonatype.org/) and the [Gradle Plugin Portal](https://plugins.gradle.org/). If you are a project maintainer you can release a new version by unblocking the publish step on CI and following the steps below.

## Pre-release checklist

This contains a prompt of checks which you may want to test, depending on the extent of the changeset:

- [ ] Is the `bugsnag-cli` submodule up-to-date with the latest CLI version?
- [ ] Has the full test suite been triggered on Buildkite and does it pass?
- [ ] Does the build pass on the CI server?
- [ ] Are all Docs PRs ready to go?

## Making the release

- Create a new release branch from `next` -> `release/vN.N.N`
- Pull the release branch and update it locally:
  - [ ] Update the version number with `make bump`
  - [ ] Inspect the updated CHANGELOG, README, and version files to ensure they are correct
- Open a Pull Request from the release branch to `main`
- Once merged:
  - Pull the latest changes (checking out `main` if necessary)
  - On CI:
    - Trigger the release step by allowing the `Trigger package publish` step to continue
    - Verify the `Publish` step runs correctly and the artefacts are upload to sonatype.
  - Checkout `main` and pull the latest changes
  - [ ] "Promote" the release build on Maven Central:
    - Go to the [maven central sonatype dashboard](https://central.sonatype.com/)
    - Click the “Deployments” button
    - Check that all the expected artefacts are uploaded
    - Click "Deploy"
  - Release to GitHub:
    - [ ] Create *and tag* the release from `main` on [GitHub Releases](https://github.com/bugsnag/bugsnag-gradle-plugin/releases)
  - Merge outstanding docs PRs related to this release
