Feature: Create a detailed test cases for login including positive and negative scenario with project structure of POM
  As a user
  I want to test Login functionality

  Background:
    Given the application is ready

  @Functional @Priority=0
  Scenario: Verify User Should be login with Valid Credentials
    Given User is redirected to login page
    And User try to login with Valid Credentails
    And User is able to login with valid credentails

  @Functional @Negative @Priority=1
  Scenario: Verify User should not be able to login with Invalid Credentails
    Given User is redirected to login page
    And User try to login with Invalid Credentials
    And User is not able to login with Invalid Credentials


  @UI @Negative @Priority=3
  Scenario: Verify Login with empty required fields
    Given User navigates to Login page
    When User leaves all required fields empty
    And User attempts to submit
    Then Appropriate validation messages should be displayed
    And Submit action should be prevented

  @Functional @Boundary @Priority=4
  Scenario: Verify Login with boundary values
    Given User navigates to Login page
    When User enters minimum valid data
    And User submits the form
    Then Action should complete successfully

  @Functional @Boundary @Priority=5
  Scenario: Verify Login with maximum length values
    Given User navigates to Login page
    When User enters maximum valid data
    And User submits the form
    Then Action should complete successfully

  @UI @Priority=6
  Scenario: Verify Login UI elements are displayed correctly
    Given User navigates to Login page
    Then Username Field should be visible
    Then Password Field should be visible
    Then SignIn Button should be visible
    And All elements should have correct labels
    And Page layout should be correct

  @Functional @Priority=7
  Scenario: Verify Login element states
    Given User navigates to Login page
    Then All input fields should be enabled
    When User enters data in fields
    Then Input fields should accept user input
    And Submit button should remain enabled

  @Functional @Priority=8
  Scenario: Verify Login accessibility features
    Given User navigates to Login page
    Then All form elements should have proper labels
    And Tab navigation should work correctly
    And Keyboard shortcuts should be functional
    And ARIA labels should be present

  @UX @Priority=9
  Scenario: Verify Login user flow experience
    Given User navigates to Login page
    When User focuses on first input field
    Then Field should be highlighted
    When User presses Tab key
    Then Focus should move to next field
    When User completes all fields and submits
    Then Smooth transition should occur

  @Functional @Negative @Priority=10
  Scenario: Verify Login error handling and feedback
    Given User navigates to Login page
    When User try to login with Invalid Credentials
    And User attempts to submit
    Then Clear error message should be displayed
    And Previously entered valid data should be preserved
    And User should be able to correct errors

  @Functional @Priority=11
  Scenario: Verify Login provides appropriate feedback
    Given User navigates to Login page
    When User submits valid data
    Then Loading indicator should appear
    When action completes
    Then Success feedback should be provided
    And User should see confirmation

  @Performance @Priority=12
  Scenario: Verify Login page load performance
    When User navigates to Login page
    Then Page should load completely within 3 seconds
    And All elements should be interactive
    And No performance bottlenecks should exist

  @Performance @Priority=13
  Scenario: Verify Login action response time
    Given Page is fully loaded
    When User submits valid data
    Then Action should complete within 2 seconds
    And Server response should be fast
    And No UI freezing should occur

  @Functional @Priority=14
  Scenario: Verify Login handles concurrent access
    When Multiple users access page simultaneously
    Then Each action should process correctly
    And Response times should remain acceptable
    And No system degradation should occur

  @Security @Priority=15
  Scenario: Verify Login password security
    Given User navigates to Login page
    When User types password
    Then Password characters should be masked
    And Password should not be visible in page source
    And No password leakage in network requests

  @Functional @Priority=16
  Scenario: Verify Login brute force protection
    Given User navigates to Login page
    When User makes multiple failed attempts
    Then Account should be temporarily locked after threshold
    And Appropriate warning should be displayed

  @Security @Priority=17
  Scenario: Verify Login prevents SQL injection
    Given User navigates to Login page
    When User enters SQL injection attempts in fields
    And User attempts to submit
    Then System should reject malicious input safely
    And No database errors should be exposed
    And Appropriate error message should be shown
