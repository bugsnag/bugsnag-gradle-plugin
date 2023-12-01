# Mazerunner tests

E2E tests are implemented with our testing tool [Maze runner](https://github.com/bugsnag/maze-runner), which is a black-box test framework written in Ruby.

### Setting up local end-to-end testing

1. Run `bundle install`

### Running an end-to-end test

1. Check the contents of `Gemfile` to select the version of `maze-runner` to use
1. To run a single feature:
    ```shell script
    bundle exec maze-runner features/aab_upload.feature
    ```
1. To run all features, omit the final argument, but be wary of how many tests you run locally as we have a limited number of parallel tests and local running subverts the controls we have in place.  For a full test run it is generally best to push your branch to Github and let CI run them.
1. Maze Runner also supports all options that Cucumber does.  Run `bundle exec maze-runner --help` for full details.
