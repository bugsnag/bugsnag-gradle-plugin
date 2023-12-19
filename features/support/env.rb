BeforeAll do
  # Configure app environment
  # Set this explicitly
  $api_key = 'TEST_API_KEY'

  Maze.config.enforce_bugsnag_integrity = false

  Maze::Runner.run_command('./features/scripts/clear_local_maven_repo.sh')
  Maze::Runner.run_command('./features/scripts/install_gradle_plugin.sh')
end

Before do
  # reset the environment-vars to their default values
  ENV.delete 'AGP_VERSION'
  ENV.delete 'APP_VERSION_CODE'
  ENV.delete 'APP_VERSION_NAME'
  ENV.delete 'BUILD_UUID'
end

Before('@skip') do |scenario|
  skip_this_scenario("Skipping scenario")
end
