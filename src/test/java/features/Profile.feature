Feature: USer can update the profile details like firstname,lastname, and email address also you can choose lunguage and landing page from dropdown options
  As a user
  I want to test Profile functionality

  Background:
    Given the application is ready

  @Functional @PerformanceTest @Priority=0
  Scenario: Verify profile page
    Given: User login with valid credentails and redirect to profile page
    When: User is in profile page
    Then: Verify the profile after update
