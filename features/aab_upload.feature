Feature: Android AAB upload

  Scenario: Upload release bundle
    When I build the release bundle
    And I wait to receive 1 build
    Then 1 requests have an R8 mapping file with the following symbols:
      | jvmSymbols                    |
      | com.example.fixture.Logger    |
      | Logger.info(java.lang.String) |

  Scenario: Upload debug bundle
    When I build the debug bundle
    And I should receive no builds
