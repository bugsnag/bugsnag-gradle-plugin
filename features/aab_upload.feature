Feature: Android AAB upload

  Scenario: Upload release bundle
    When I build the release bundle
    And I wait to receive 5 builds
    Then 1 requests have an R8 mapping file with the following symbols:
      | jvmSymbols                    |
      | com.example.fixture.Logger    |
      | Logger.info(java.lang.String) |
    And 4 requests are valid for the android NDK mapping API and match the following:
      | projectRoot                  | sharedObjectName  |
      | /^.*/features/fixtures/app$/ | libfixture.so.sym |
      | /^.*/features/fixtures/app$/ | libfixture.so.sym |
      | /^.*/features/fixtures/app$/ | libfixture.so.sym |
      | /^.*/features/fixtures/app$/ | libfixture.so.sym |

  Scenario: Upload release bundle with custom projectRoot
    Given I set environment variable "PROJECT_ROOT" to "app/src/main/cpp"
    When I build the release bundle
    And I wait to receive 5 builds
    And 4 requests are valid for the android NDK mapping API and match the following:
      | projectRoot                                   | sharedObjectName  |
      | /^.*/features/fixtures/app/app/src/main/cpp$/ | libfixture.so.sym |
      | /^.*/features/fixtures/app/app/src/main/cpp$/ | libfixture.so.sym |
      | /^.*/features/fixtures/app/app/src/main/cpp$/ | libfixture.so.sym |
      | /^.*/features/fixtures/app/app/src/main/cpp$/ | libfixture.so.sym |
    And 1 requests have an R8 mapping file with the following symbols:
      | jvmSymbols                    |
      | com.example.fixture.Logger    |
      | Logger.info(java.lang.String) |

  Scenario: Upload debug bundle
    When I build the debug bundle
    Then I should receive no builds
