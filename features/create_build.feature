Feature: Create build

  Scenario: Create release build
    When I create the release build
    And I wait to receive 1 build
    Then 1 request is a valid build and matches the following:
      | appVersion | appVersionCode | apiKey       | builderName |
      | 1.0        | 1              | TEST_API_KEY | test_user   |
