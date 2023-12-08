Feature: Android Assemble upload

  Scenario: Upload release bundle
    When I upload the release mapping.txt
    And I wait to receive 1 build
    Then 1 requests have an R8 mapping file with the following symbols:
      | jvmSymbols                    |
      | com.example.fixture.Logger    |
      | Logger.info(java.lang.String) |


