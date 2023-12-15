Feature: Android native symbols upload

  Scenario: Upload release native symbols
    When I upload the release native symbols
    And I wait to receive 4 builds
    Then 4 requests are valid for the android NDK mapping API and match the following:
      | sharedObjectName  |
      | libfixture.so.sym |
      | libfixture.so.sym |
      | libfixture.so.sym |
      | libfixture.so.sym |
