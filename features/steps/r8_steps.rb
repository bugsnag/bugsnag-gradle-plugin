require 'zlib'
require 'stringio'

Then('{int} requests have an R8 mapping file with the following symbols:') do |request_count, data_table|
  requests = get_requests_with_field('build', 'proguard')
  assert_equal(request_count, requests.length, 'Wrong number of mapping API requests')

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
  assert_not_nil(request_body['buildUUID'])
  assert_not_nil(request_body['proguard'])
end

def valid_mapping_api?(request_body)
  assert_equal($api_key, request_body['apiKey'])
  assert_not_nil(request_body['appId'])
  assert_not_nil(request_body['versionCode'])
  assert_not_nil(request_body['versionName'])
end

def valid_r8_mapping_contents?(mapping_file_lines, expected_entries)
  # validates that the mapping file key is present for each symbol,
  # obfuscated values are not validated as they vary depending on AGP's implementation
  expected_entries.each do |row|
    expected_entry = row[0]
    has_mapping_entry = mapping_file_lines.find { |line|
      line.include?(expected_entry) && line.include?(' -> ')
    }
    assert_false(has_mapping_entry.nil?, "No entry in mapping file for '#{row[0]}'.")
  end
end

def get_requests_with_field(request_type, name)
  all_requests = Maze::Server.list_for(request_type).clone
  all_requests.all.reject do |request|
    value = Maze::Helper.read_key_path(request[:body], name)
    value.nil?
  end
end
