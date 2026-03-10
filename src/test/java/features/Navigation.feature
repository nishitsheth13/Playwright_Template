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
    Then page should be visible
    When user clicks on mri navigation bar expand
    When user clicks on setup
    When user clicks on tree
    When user clicks on configure tree
