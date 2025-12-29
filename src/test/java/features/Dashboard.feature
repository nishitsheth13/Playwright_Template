@AUTO-GEN @Dashboard
Feature: Dashboard Test
  Auto-generated from Playwright recording

  Scenario: Complete Dashboard workflow with existing login
    Given user navigates to Dashboard page
    # ═══ LOGIN STEPS - USING EXISTING METHODS ═══
    When User enters valid username from configuration
    And User enters valid password from configuration
    And User clicks on Sign In button
    # ═══════════════════════════════════════════════
    When user clicks on dashboards dashboards
    When user clicks on select new path
    When user clicks on edit
    When user clicks on yes
    Then page should be updated
