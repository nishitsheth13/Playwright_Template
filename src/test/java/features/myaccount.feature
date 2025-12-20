Feature: Validate after login user is redirected to my account page and check details and if they want update the personal details

  @Priority=0
  Scenario: Verify USer is Redirected to My Account
    Given Enter Valid Credentials
    When When User has valid credentais and try to login
    Then Redirect user to MyAccount Page

  @Priority=1
  Scenario: Verify User is able to View and Update the Account
    Given Enter Valid Credentails
    When When User Redirect to MYAccount View Basic Details
    And VErify USer is able to Update the basic details
    Then Verify Details are updated correctly

