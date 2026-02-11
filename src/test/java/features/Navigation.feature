@AUTO-GEN @Navigation
Feature: Navigation Test
  Auto-generated from Playwright recording

  Scenario: Complete Navigation workflow with existing login
    Given user navigates to Navigation page
    # ═══ LOGIN STEPS - USING EXISTING METHODS ═══
    When User enters valid username from configuration
    And User enters valid password from configuration
    And User clicks on Sign In button
    # ═══════════════════════════════════════════════
    When user clicks on div
    When user clicks on setup setup
    When user clicks on configure tree
    When user clicks on logout logout
    Then page should be updated
