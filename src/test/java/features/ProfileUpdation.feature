@AUTO-GEN @ProfileUpdation
Feature: ProfileUpdation Test
  Auto-generated from Playwright recording

  Scenario Outline: Complete ProfileUpdation workflow
    Given user navigates to ProfileUpdation page
    When user clicks on username
    And user enters "<username>" into username
    And user presses key on username
    And user enters "<password>" into password
    When user clicks on sign in
    When user clicks on first name
    And user enters "<firstname>" into firstname
    When user clicks on last name
    And user enters "<lastname>" into lastname
    When user clicks on mobile phone number
    And user enters "<mobilephonenumber>" into mobilephonenumber
    When user clicks on b9ddca9b7382468e a006273c27d735b5
    When user clicks on number25
    When user clicks on add
    When user clicks on save
    When user clicks on logout logout
    Then page should be updated

    Examples:
      | username | password | firstname | lastname | mobilephonenumber |
      | admin | 131805 | Admin | Admin | 07771 531454 |
