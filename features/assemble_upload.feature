Feature: Android Assemble upload

  Scenario: Upload release bundle
    When I upload the release mapping.txt
    And I wait to receive 1 build
    Then 1 requests have an R8 mapping file with the following symbols:
      | jvmSymbols                    |
      | com.example.fixture.Logger    |
      | Logger.info(java.lang.String) |

  Scenario: Upload release bundle with custom build UUID
    Given I set environment variable "BUILD_UUID" to "test123"
    When I upload the release mapping.txt
    And I wait to receive 1 build
    Then 1 request is valid for the android assemble file and match the following:
      | buildUUID  |
      | test123    |

  Scenario: Upload debug bundle
    When I build the debug bundle
    Then I should receive no builds


