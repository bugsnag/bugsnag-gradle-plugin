require 'zlib'
require 'stringio'

Then('{int} request(s) have/has an R8 mapping file with the following symbols:') do |request_count, data_table|
  requests = get_requests_with_field('build', 'proguard')
  Maze.check.equal(request_count, requests.length, "Wrong number of mapping API requests: expected #{request_count}, got #{requests.length}")

  # inflate gzipped proguard mapping file & verify contents
  requests.each do |request|
    valid_android_mapping_api?(request[:body])
    gzipped_part = request[:body]['proguard']
    archive = Zlib::GzipReader.new(StringIO.new(gzipped_part))
    mapping_file_lines = archive.read.split("\n")
    valid_r8_mapping_contents?(mapping_file_lines, data_table.rows)
  end
end

def valid_android_mapping_api?(request_body)
  valid_mapping_api?(request_body)
  Maze.check.not_nil(request_body['buildUUID'], 'buildUUID was nil')
  Maze.check.not_nil(request_body['proguard'], 'proguard was nil')
end

def valid_mapping_api?(request_body)
  Maze.check.equal($api_key, request_body['apiKey'], "Wrong apiKey: expected #{api_key}, got #{request_body['apiKey']}")
  Maze.check.not_nil(request_body['appId'], 'appId was nil')
  Maze.check.not_nil(request_body['versionCode'], 'versionCode was nil')
  Maze.check.not_nil(request_body['versionName'], 'versionName was nil')
end

def valid_r8_mapping_contents?(mapping_file_lines, expected_entries)
  # validates that the mapping file key is present for each symbol,
  # obfuscated values are not validated as they vary depending on AGP's implementation
  expected_entries.each do |row|
    expected_entry = row[0]
    has_mapping_entry = mapping_file_lines.find { |line|
      line.include?(expected_entry) && line.include?(' -> ')
    }
    Maze.check.false(has_mapping_entry.nil?, "No entry in mapping file for '#{row[0]}'.")
  end
end

def get_requests_with_field(request_type, name)
  all_requests = Maze::Server.list_for(request_type).clone
  all_requests.all.reject do |request|
    value = Maze::Helper.read_key_path(request[:body], name)
    value.nil?
  end
end
