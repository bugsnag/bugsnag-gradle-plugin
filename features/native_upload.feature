Feature: Android native symbols upload

  Scenario: Upload release native symbols
    When I upload the release native symbols
    And I wait to receive 4 builds
    And 4 requests are valid for the android NDK mapping API and match the following:
      | projectRoot                  | sharedObjectName  |
      | /^.*/features/fixtures/app$/ | libfixture.so.sym |
      | /^.*/features/fixtures/app$/ | libfixture.so.sym |
      | /^.*/features/fixtures/app$/ | libfixture.so.sym |
      | /^.*/features/fixtures/app$/ | libfixture.so.sym |

  Scenario: Upload release native symbols with custom projectRoot
    Given I set environment variable "PROJECT_ROOT" to "app/src/main/cpp"
    When I upload the release native symbols
    And I wait to receive 4 builds
    And 4 requests are valid for the android NDK mapping API and match the following:
      | projectRoot                                   | sharedObjectName  |
      | /^.*/features/fixtures/app/app/src/main/cpp$/ | libfixture.so.sym |
      | /^.*/features/fixtures/app/app/src/main/cpp$/ | libfixture.so.sym |
      | /^.*/features/fixtures/app/app/src/main/cpp$/ | libfixture.so.sym |
      | /^.*/features/fixtures/app/app/src/main/cpp$/ | libfixture.so.sym |

  Scenario: Upload native symbols with version overrides
    Given I set environment variable "VERSION_NAME_OVERRIDE" to "9.8.7"
    * I set environment variable "VERSION_CODE_OVERRIDE" to "987"
    When I upload the release native symbols
    And I wait to receive 4 builds
    And 4 requests are valid for the android NDK mapping API and match the following:
      | versionName | versionCode | projectRoot                       | sharedObjectName  |
      | 9.8.7       | 987         | /^.*/features/fixtures/app$/ | libfixture.so.sym |
      | 9.8.7       | 987         | /^.*/features/fixtures/app$/ | libfixture.so.sym |
      | 9.8.7       | 987         | /^.*/features/fixtures/app$/ | libfixture.so.sym |
      | 9.8.7       | 987         | /^.*/features/fixtures/app$/ | libfixture.so.sym |
