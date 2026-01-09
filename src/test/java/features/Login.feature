@Login @Regression
Feature: Login Test
  As a user
  I want to login to the application
  So that I can access the system

  Background:
    Given user navigates to Login page

  @ValidLogin @Smoke
  Scenario: Successful login with valid credentials
    When User enters valid username from configuration
    And User enters valid password from configuration
    And User clicks on Sign In button
    Then user should be logged in successfully

  @Logout
  Scenario: User logs out successfully
    When User enters valid username from configuration
    And User enters valid password from configuration
    And User clicks on Sign In button
    And user clicks on logout logout
    Then page should be updated
