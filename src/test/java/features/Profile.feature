@AUTO-GEN @Profile
Feature: Profile Test
  Auto-generated from Playwright recording

  Scenario Outline: Complete Profile workflow with existing login
    Given user navigates to Profile page
    # ═══ LOGIN STEPS - USING EXISTING METHODS ═══
    When User enters valid username from configuration
    And User enters valid password from configuration
    And User clicks on Sign In button
    # ═══════════════════════════════════════════════
    When user clicks on mobile phone number
    And user enters "<mobilephonenumber>" into mobilephonenumber
    When user clicks on save
    When user clicks on logout logout
    Then page should be updated

    Examples:
      | mobilephonenumber |
      | 789745145184 |
