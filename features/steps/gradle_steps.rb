When('I use version {string} of AGP') do |agp_version|
  steps %Q{
    When I set environment variable "AGP_VERSION" to "#{agp_version}"
  }
end

When('I set the app version name and code to {string} and {int}') do |version_name, version_code|
  steps %Q{
    When I set environment variable "APP_VERSION_NAME" to "#{version_name}"
    When I set environment variable "APP_VERSION_CODE" to "#{version_code}"
  }
end

When('I build the {word} bundle') do |variant|
  Maze::Runner.run_command("features/scripts/bundle.sh #{variant.capitalize}")
end

When('I upload the {word} native symbols') do |variant|
  Maze::Runner.run_command("features/scripts/ndk_app.sh #{variant.capitalize}")
end
