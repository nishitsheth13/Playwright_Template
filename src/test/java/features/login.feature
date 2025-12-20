Feature: Comprehensive Login Functionality Testing - MRI Energy Web Application
  As a user of the MRI Energy application
  I want to test the login functionality
  So that I can ensure secure and proper access control

  Background:
    Given User navigates to the MRI Energy login page

  # ========== FUNCTIONAL TEST SCENARIOS ==========

  @Functional @Smoke @Priority=0
  Scenario: Verify successful login with valid credentials from configuration
    When User enters valid username from configuration
    And User enters valid password from configuration
    And User clicks on Sign In button
    Then User should be successfully logged in
    And User should be redirected to the home page

  @Functional @Negative @Priority=1
  Scenario: Verify login fails with invalid username
    When User enters invalid username "wronguser@test.com"
    And User enters valid password from configuration
    And User clicks on Sign In button
    Then User should see an error message
    And User should remain on the login page

  @Functional @Negative @Priority=2
  Scenario: Verify login fails with invalid password
    When User enters valid username from configuration
    And User enters invalid password "WrongPassword123"
    And User clicks on Sign In button
    Then User should see an error message
    And User should remain on the login page

  @Functional @Negative @Priority=3
  Scenario: Verify login fails with both invalid credentials
    When User enters invalid username "wronguser@test.com"
    And User enters invalid password "WrongPassword123"
    And User clicks on Sign In button
    Then User should see an error message
    And User should remain on the login page

  @Functional @Negative @Priority=4
  Scenario: Verify login fails with empty username
    When User leaves username field empty
    And User enters valid password from configuration
    And User clicks on Sign In button
    Then User should see validation message for username

  @Functional @Negative @Priority=5
  Scenario: Verify login fails with empty password
    When User enters valid username from configuration
    And User leaves password field empty
    And User clicks on Sign In button
    Then User should see validation message for password

  @Functional @Negative @Priority=6
  Scenario: Verify login fails with empty credentials
    When User leaves username field empty
    And User leaves password field empty
    And User clicks on Sign In button
    Then User should see validation messages for both fields

  @Functional @Priority=7
  Scenario: Verify Remember Me functionality
    When User enters valid username from configuration
    And User enters valid password from configuration
    And User checks the Remember Me checkbox
    And User clicks on Sign In button
    Then User should be successfully logged in
    And Remember Me should be enabled

  @Functional @Priority=8
  Scenario: Verify Forgot Username link is functional
    When User clicks on Forgot Username link
    Then User should be redirected to Forgot Username page

  @Functional @Priority=9
  Scenario: Verify Forgot Password link is functional
    When User clicks on Forgot Password link
    Then User should be redirected to Forgot Password page

  # ========== NON-FUNCTIONAL TEST SCENARIOS ==========

  @NonFunctional @Security @Priority=10
  Scenario: Verify password field masks input
    When User enters valid password from configuration
    Then Password field should display masked characters

  @NonFunctional @Security @Priority=11
  Scenario: Verify account lockout after multiple failed attempts
    When User attempts login with invalid password 5 times
    Then User account should be locked
    And User should see account lockout message

  @NonFunctional @UI @Priority=12
  Scenario: Verify all login page elements are visible
    Then Username field should be visible
    And Password field should be visible
    And Sign In button should be visible
    And Remember Me checkbox should be visible
    And Forgot Username link should be visible
    And Forgot Password link should be visible
    And MRI Energy logo should be visible

  @NonFunctional @UI @Priority=13
  Scenario: Verify page title and branding
    Then Page title should be "Login - MRI Energy"
    And MRI Energy logo should be displayed correctly

  @NonFunctional @Performance @Priority=14
  Scenario: Verify login response time with valid credentials
    When User enters valid username from configuration
    And User enters valid password from configuration
    And User clicks on Sign In button
    Then Login should complete within 5 seconds

  @NonFunctional @Usability @Priority=15
  Scenario: Verify tab navigation on login page
    When User uses Tab key to navigate
    Then Focus should move from Username to Password
    And Focus should move to Remember Me checkbox
    And Focus should move to Sign In button

  @NonFunctional @Accessibility @Priority=16
  Scenario: Verify login page accessibility features
    Then Username field should have proper label
    And Password field should have proper label
    And Sign In button should be keyboard accessible

  @NonFunctional @Compatibility @Priority=17
  Scenario: Verify login page version information
    Then Version information should be displayed in footer
    And Version should show "Version: 2025.2 (Build: 019)"

