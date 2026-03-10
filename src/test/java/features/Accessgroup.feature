@AUTO-GEN @Accessgroup
Feature: Accessgroup Test
  Auto-generated from Playwright recording

  Scenario Outline: Complete Accessgroup workflow
    Given user navigates to Accessgroup page
    # ═══ LOGIN STEPS - USING EXISTING METHODS ═══
    When User enters valid username from configuration
    And User enters valid password from configuration
    And User clicks on Sign In button
    # ═══════════════════════════════════════════════
    When user clicks on setup
    When user clicks on security
    When user clicks on access groups
    When user clicks on add
    And user enters "<pleaseenteraname>" into pleaseenteraname
    When user clicks on txtsearchboxvalue
    And user enters "<txtsearchboxvalue>" into txtsearchboxvalue
    When user clicks on btnsearch
    Then mri energy automation root company should be visible
    When user clicks on mri energy automation root company
    When user clicks on save
    When user clicks on clear filter
    And user enters "<search>" into search
    Then automationtest should be visible

    Examples:
      | pleaseenteraname | txtsearchboxvalue | search |
      | AutomationTest | MRI | Automation |
