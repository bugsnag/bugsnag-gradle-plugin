Feature: Android Assemble upload

  Scenario: Upload release mapping.txt
    When I upload the release mapping.txt
    And I wait to receive 1 build
    Then 1 requests have an R8 mapping file with the following symbols:
      | jvmSymbols                    |
      | com.example.fixture.Logger    |
      | Logger.info(java.lang.String) |

  Scenario: Upload release mapping.txt with custom build UUID
    Given I set environment variable "BUILD_UUID" to "test123"
    When I upload the release mapping.txt
    And I wait to receive 1 build
    Then 1 request is valid for the android assemble file and match the following:
      | buildUUID  | appId                  | apiKey       |
      | test123    | com.example.fixture    | TEST_API_KEY |

  Scenario: Upload release mapping.txt with version overrides
    Given I set environment variable "VERSION_NAME_OVERRIDE" to "9.8.7"
    * I set environment variable "VERSION_CODE_OVERRIDE" to "987"
    When I upload the release mapping.txt
    And I wait to receive 1 build
    Then 1 request is valid for the android assemble file and match the following:
      | versionName | versionCode | appId                  | apiKey       |
      | 9.8.7       | 987         | com.example.fixture    | TEST_API_KEY |

  Scenario: Upload debug bundle
    When I build the debug bundle
    Then I should receive no builds


