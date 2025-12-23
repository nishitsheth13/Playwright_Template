Feature: Impersonate Access Group Functionality
  As a system administrator
  I want to configure users to impersonate different access groups when Access Group Security is enabled
  So that users can temporarily operate in the context of another access group without losing their original access configuration
  
  Background:
    Given user is logged into the system
    And Access Group Security is enabled in the system
  
  @ECS-14 @UserConfiguration @Smoke
  Scenario: Verify Impersonate Access Group checkbox is visible when Access Type is Access Group Security
    Given user navigates to User Configuration page
    When user selects Access Type as "Access Group Security"
    Then Impersonate Access Group checkbox should be displayed
    And Impersonate Access Group checkbox should be enabled
  
  @ECS-14 @UserConfiguration @Regression
  Scenario: Verify Impersonate Access Group checkbox is hidden for non-Access Group Security types
    Given user navigates to User Configuration page
    When user selects Access Type as "Standard Security"
    Then Impersonate Access Group checkbox should not be displayed
  
  @ECS-14 @UserConfiguration @Critical
  Scenario: Create new user with Impersonate Access Group enabled
    Given user navigates to User Configuration page
    When user enters the following details:
      | Field              | Value                      |
      | User Name          | testuser_impersonate       |
      | Email              | testuser@example.com       |
      | Access Type        | Access Group Security      |
      | Access Group       | Sales Team                 |
      | Impersonate Access | Enabled                    |
    And user clicks Save button
    Then user should be created successfully
    And success message should be displayed
    And OriginalAccessGroupId should be set to "Sales Team"
    And AccessGroupId should be set to "Sales Team"
    And ImpersonateAccessGroup flag should be true
  
  @ECS-14 @UserConfiguration @Functional
  Scenario: Edit existing user to enable Impersonate Access Group
    Given user "existing_user" exists with Access Type "Access Group Security"
    And user navigates to User Configuration page
    When user searches for "existing_user"
    And user selects Access Group as "Marketing Team"
    And user enables Impersonate Access Group checkbox
    And user clicks Save button
    Then user should be updated successfully
    And OriginalAccessGroupId should be updated to "Marketing Team"
    And ImpersonateAccessGroup flag should be true
  
  @ECS-14 @UserConfiguration @Functional
  Scenario: Disable Impersonate Access Group for existing user
    Given user "impersonate_user" has Impersonate Access Group enabled
    And user navigates to User Configuration page
    When user searches for "impersonate_user"
    And user disables Impersonate Access Group checkbox
    And user clicks Save button
    Then user should be updated successfully
    And ImpersonateAccessGroup flag should be false
  
  @ECS-14 @MyAccount @Critical
  Scenario: Verify Impersonate Access Group label is displayed on My Account page when enabled
    Given user "admin_user" has Impersonate Access Group enabled
    And user logs in as "admin_user"
    When user navigates to My Account page
    Then "Impersonate Access Group" label should be displayed
    And Access Group section should be visible
  
  @ECS-14 @MyAccount @Smoke
  Scenario: Display access group dropdown when current AccessGroupId equals OriginalAccessGroupId
    Given user "admin_user" has following configuration:
      | OriginalAccessGroupId | Sales Team   |
      | AccessGroupId         | Sales Team   |
      | ImpersonateAccessGroup| true         |
    And user logs in as "admin_user"
    When user navigates to My Account page
    Then access group dropdown should be displayed
    And dropdown should show all accessible access groups
    And Clear button should not be displayed
  
  @ECS-14 @MyAccount @Critical @Performance
  Scenario: Switch to different access group and verify context change
    Given user "admin_user" is on My Account page with original access group "Sales Team"
    And access group dropdown is displayed
    When user selects "Support Team" from access group dropdown
    Then AccessGroupId should be updated to "Support Team"
    And OriginalAccessGroupId should remain "Sales Team"
    And user should operate in "Support Team" context
    And page performance should be under 3 seconds
  
  @ECS-14 @MyAccount @Functional
  Scenario: Display current access group and Clear button when impersonating
    Given user "admin_user" has following configuration:
      | OriginalAccessGroupId | Sales Team    |
      | AccessGroupId         | Support Team  |
      | ImpersonateAccessGroup| true          |
    And user logs in as "admin_user"
    When user navigates to My Account page
    Then current access group label should display "Support Team"
    And Clear button should be displayed
    And access group dropdown should not be displayed
  
  @ECS-14 @MyAccount @Critical
  Scenario: Reset to original access group using Clear button
    Given user "admin_user" is impersonating "Support Team"
    And original access group is "Sales Team"
    And user is on My Account page
    When user clicks Clear button
    Then AccessGroupId should be reset to "Sales Team"
    And OriginalAccessGroupId should remain "Sales Team"
    And access group dropdown should be displayed again
    And Clear button should not be displayed
    And confirmation message "Successfully reset to original access group" should be displayed
  
  @ECS-14 @MyAccount @Security
  Scenario: Verify user can only see access groups they have permission to access
    Given user "restricted_user" has access to following groups:
      | Sales Team    |
      | Support Team  |
    And user has Impersonate Access Group enabled
    When user logs in as "restricted_user"
    And user navigates to My Account page
    Then access group dropdown should only show:
      | Sales Team    |
      | Support Team  |
    And dropdown should not show unauthorized access groups
  
  @ECS-14 @MyAccount @Functional
  Scenario: Switch between multiple access groups
    Given user "admin_user" is on My Account page with original access group "Sales Team"
    When user switches to "Support Team"
    And user verifies context changed to "Support Team"
    And user clicks Clear button
    And user switches to "Marketing Team"
    Then AccessGroupId should be "Marketing Team"
    And user should operate in "Marketing Team" context
  
  @ECS-14 @DataIntegrity @Critical
  Scenario: Verify OriginalAccessGroupId is preserved during impersonation
    Given user "test_user" has following configuration:
      | OriginalAccessGroupId | Sales Team |
      | AccessGroupId         | Sales Team |
    When user switches to "Support Team"
    And user switches to "Marketing Team"
    And user performs multiple operations
    Then OriginalAccessGroupId should always remain "Sales Team"
    And AccessGroupId should reflect current selection "Marketing Team"
  
  @ECS-14 @UserConfiguration @NegativeTest
  Scenario: Verify Impersonate checkbox cannot be enabled without Access Group Security
    Given user navigates to User Configuration page
    When user selects Access Type as "Standard Security"
    Then Impersonate Access Group checkbox should not be displayed
    And attempting to enable impersonation should fail
  
  @ECS-14 @MyAccount @NegativeTest
  Scenario: User without Impersonate permission should not see impersonation options
    Given user "regular_user" has ImpersonateAccessGroup flag set to false
    When user logs in as "regular_user"
    And user navigates to My Account page
    Then "Impersonate Access Group" label should not be displayed
    And access group dropdown should not be displayed
    And Clear button should not be displayed
  
  @ECS-14 @Integration @E2E
  Scenario: Complete workflow - Create user, enable impersonation, switch groups, and reset
    Given user navigates to User Configuration page
    When user creates new user with following details:
      | User Name          | e2e_test_user             |
      | Email              | e2etest@example.com       |
      | Access Type        | Access Group Security     |
      | Access Group       | Sales Team                |
      | Impersonate Access | Enabled                   |
    Then user should be created successfully
    When user logs in as "e2e_test_user"
    And user navigates to My Account page
    Then "Impersonate Access Group" label should be displayed
    And access group dropdown should be displayed
    When user selects "Support Team" from dropdown
    Then current access group should be "Support Team"
    And Clear button should be displayed
    When user clicks Clear button
    Then access group should reset to "Sales Team"
    And access group dropdown should be displayed again
  
  @ECS-14 @Performance @LoadTest
  Scenario: Verify performance when switching access groups multiple times
    Given user "perf_user" is on My Account page
    When user switches access groups 10 times consecutively
    Then each switch operation should complete within 2 seconds
    And no performance degradation should be observed
    And all switches should be successful
  
  @ECS-14 @AuditLog @Compliance
  Scenario: Verify audit trail for access group impersonation
    Given user "audit_user" has Impersonate Access Group enabled
    And audit logging is enabled
    When user switches from "Sales Team" to "Support Team"
    Then audit log should record:
      | User          | audit_user        |
      | Action        | Impersonate       |
      | Original AG   | Sales Team        |
      | Target AG     | Support Team      |
      | Timestamp     | Current           |
    When user clicks Clear button
    Then audit log should record reset action
  
  @ECS-14 @UI @Accessibility
  Scenario: Verify UI elements and accessibility on My Account page
    Given user "ui_user" has Impersonate Access Group enabled
    When user navigates to My Account page
    Then all UI elements should be properly aligned
    And labels should have proper contrast ratios
    And dropdown should be keyboard accessible
    And Clear button should be keyboard accessible
    And all elements should have proper ARIA labels
