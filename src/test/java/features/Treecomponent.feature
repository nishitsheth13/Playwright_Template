@AUTO-GEN @Treecomponent
Feature: Treecomponent Test
  Auto-generated from Playwright recording

  Scenario Outline: Complete Treecomponent workflow
    Given user navigates to Treecomponent page
    # ═══ LOGIN STEPS - USING EXISTING METHODS ═══
    When User enters valid username from configuration
    And User enters valid password from configuration
    And User clicks on Sign In button
    # ═══════════════════════════════════════════════
    When user clicks on setup
    When user clicks on security
    When user clicks on access groups
    When user clicks on add
    When user clicks on txtsearchboxvalue
    And user enters "<txtsearchboxvalue>" into txtsearchboxvalue
    When user clicks on btnsearch
    Then mri energy automation root company should be visible
    When user clicks on mri energy automation root company
    When user clicks on open
    When user clicks on tree details
    When user clicks on clear
    And user enters "<selectfilter>" into selectfilter
    When user clicks on btnfilter
    Then list filters should be visible
    When user clicks on uxname
    And user enters "<uxname>" into uxname
    And user selects "<uxcolumn>" from uxcolumn
    When user clicks on uxfcvaluetextbox
    And user enters "<uxfcvaluetextbox>" into uxfcvaluetextbox
    When user clicks on add to filter
    When user clicks on search
    When user clicks on close
    When user clicks on save
    When user clicks on new treefilter

    Examples:
      | txtsearchboxvalue | selectfilter | uxname | uxcolumn | uxfcvaluetextbox |
      | MRI | Tree | New TreeFilter | Name:String | MRI |
