package configs.jira;

import configs.loadProps;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.io.File;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * JIRA REST API client for automated defect management.
 * Handles bug creation, comment addition, and attachment uploads.
 */
public class jiraClient {

    /**
     * Gets the properly formatted JIRA base URL with trailing slash.
     *
     * @return JIRA base URL ending with /
     */
    private static String getJiraBaseUrl() {
        String baseUrl = loadProps.getJIRAConfig("JIRA_BASE_URL");
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            System.err.println("❌ JIRA_BASE_URL not configured");
            return null;
        }
        // Ensure URL ends with /
        return baseUrl.trim().endsWith("/") ? baseUrl.trim() : baseUrl.trim() + "/";
    }

    /**
     * Generates Basic Auth header for JIRA API calls.
     * 
     * @return Base64 encoded authorization header
     */
    private static String getAuthHeader() {
        String email = loadProps.getJIRAConfig("JIRA_EMAIL");
        String token = loadProps.getJIRAConfig("JIRA_API_TOKEN");
        
        if (email == null || token == null) {
            System.err.println("❌ JIRA credentials not found in configuration");
            return null;
        }
        
        String auth = email + ":" + token;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }

    /**
     * Handles test case results by updating JIRA issues.
     * Adds pass/fail comments based on test outcome.
     * 
     * @param issueKey JIRA issue key to update
     * @param summary Test summary
     * @param descriptionText Test description or error message
     * @param attachmentFile Screenshot file (optional)
     * @param isFailed Whether the test failed
     */
    public static void handleTestCaseResultSmart(String issueKey, String summary, String descriptionText, File attachmentFile, boolean isFailed) {
        try {
            String authHeader = getAuthHeader();
            if (authHeader == null) {
                System.err.println("❌ Cannot update JIRA - authentication failed");
                return;
            }

            String baseUrl = getJiraBaseUrl();
            if (baseUrl == null) {
                return;
            }

            String passCommentText = loadProps.getProperty("PassComment") + " " + loadProps.getProperty("Version");
            String failCommentText = loadProps.getProperty("FailedComment") + " " + loadProps.getProperty("Version");
            
            // Check if issue exists
            Response checkResponse = RestAssured
                    .given()
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/json")
                    .get(baseUrl + "rest/api/3/issue/" + issueKey);

            if (checkResponse.getStatusCode() == 200) {
                String commentText = isFailed ?
                        failCommentText + "\n\n*Summary:* " + summary + "\n\n*Details:* " + descriptionText :
                        passCommentText;

                addComment(issueKey, commentText);

                // Attach screenshot if test failed and file exists
                if (isFailed) {
                    System.out.println("🔍 Screenshot attachment check:");
                    System.out.println("   - attachmentFile is null: " + (attachmentFile == null));
                    if (attachmentFile != null) {
                        System.out.println("   - attachmentFile path: " + attachmentFile.getAbsolutePath());
                        System.out.println("   - attachmentFile exists: " + attachmentFile.exists());
                        System.out.println("   - attachmentFile size: " + attachmentFile.length() + " bytes");
                    }

                    if (attachmentFile != null && attachmentFile.exists() && attachmentFile.length() > 0) {
                        System.out.println("📎 Attempting to attach screenshot to " + issueKey);
                        Response attachResponse = RestAssured
                                .given()
                                .header("Authorization", authHeader)
                                .header("X-Atlassian-Token", "no-check")
                                .multiPart("file", attachmentFile)
                                .post(baseUrl + "rest/api/3/issue/" + issueKey + "/attachments");

                        if (attachResponse.statusCode() == 200 || attachResponse.statusCode() == 201) {
                            System.out.println("✅ Screenshot attached successfully to " + issueKey);
                        } else {
                            System.err.println("❌ Failed to upload screenshot (" + attachResponse.statusCode() + "): " + attachResponse.getBody().asString());
                        }
                    } else {
                        if (attachmentFile == null) {
                            System.err.println("⚠️ No screenshot file provided (attachmentFile is null)");
                        } else if (!attachmentFile.exists()) {
                            System.err.println("⚠️ Screenshot file does not exist: " + attachmentFile.getAbsolutePath());
                        } else if (attachmentFile.length() == 0) {
                            System.err.println("⚠️ Screenshot file is empty (0 bytes): " + attachmentFile.getAbsolutePath());
                        }
                    }
                }

                System.out.println("✅ JIRA issue " + issueKey + " updated with " + (isFailed ? "failure" : "success") + " comment");
            } else if (checkResponse.getStatusCode() == 404) {
                System.err.println("❌ JIRA issue " + issueKey + " not found (404)");
            } else if (checkResponse.getStatusCode() == 401) {
                System.err.println("❌ JIRA authentication failed (401) - check credentials");
            } else if (checkResponse.getStatusCode() == 403) {
                System.err.println("❌ JIRA access forbidden (403) - check permissions");
            } else {
                System.err.println("❌ Unexpected JIRA API response (" + checkResponse.getStatusCode() + "): " + checkResponse.getBody().asString());
            }
        } catch (Exception e) {
            System.err.println("❌ Error updating JIRA issue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void createBug(String summary, String descriptionText, File attachmentFile) {
        String baseUrl = getJiraBaseUrl();
        if (baseUrl == null) {
            return;
        }

        String authHeader = getAuthHeader();
        if (authHeader == null) {
            System.err.println("❌ Cannot create bug - authentication failed");
            return;
        }

        String projectKey = loadProps.getJIRAConfig("PROJECT_KEY");
        if (projectKey == null || projectKey.trim().isEmpty()) {
            System.err.println("❌ PROJECT_KEY not configured");
            return;
        }

        String adfDescription = "{\n" +
                "  \"type\": \"doc\",\n" +
                "  \"version\": 1,\n" +
                "  \"content\": [\n" +
                "    {\n" +
                "      \"type\": \"paragraph\",\n" +
                "      \"content\": [\n" +
                "        {\n" +
                "          \"type\": \"text\",\n" +
                "          \"text\": \"" + escapeForJson(descriptionText) + "\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        String payload = "{\n" +
                "  \"fields\": {\n" +
                "    \"project\": { \"key\": \"" + projectKey.trim() + "\" },\n" +
                "    \"summary\": \"" + escapeForJson(summary) + "\",\n" +
                "    \"description\": " + adfDescription + ",\n" +
                "    \"labels\": [\"BugByAutomationFailure\"],\n" +
                "    \"issuetype\": { \"name\": \"Bug\" }\n" +
                "  }\n" +
                "}";

        Response response = RestAssured
                .given()
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .body(payload)
                .post(baseUrl + "rest/api/3/issue");

        System.out.println("📤 JIRA Bug Creation Response (" + response.getStatusCode() + "):");
        System.out.println(response.asPrettyString());

        if (response.getStatusCode() == 201) {
            String createdIssueKey = response.jsonPath().getString("key");
            System.out.println("✅ Bug created in JIRA: " + createdIssueKey);

            // Attach screenshot if provided
            if (attachmentFile != null && attachmentFile.exists()) {
                Response attachResponse = RestAssured
                        .given()
                        .header("Authorization", authHeader)
                        .header("X-Atlassian-Token", "no-check")
                        .multiPart("file", attachmentFile)
                        .post(baseUrl + "rest/api/3/issue/" + createdIssueKey + "/attachments");

                if (attachResponse.statusCode() == 200 || attachResponse.statusCode() == 201) {
                    System.out.println("📎 Attachment uploaded successfully to " + createdIssueKey);
                } else {
                    System.err.println("❌ Failed to upload attachment: " + attachResponse.getBody().asString());
                }
            } else {
                System.out.println("ℹ️ No screenshot attachment provided or file doesn't exist");
            }

        } else if (response.getStatusCode() == 400) {
            System.err.println("❌ Bad request - check project key, issue type, or required fields");
        } else if (response.getStatusCode() == 401) {
            System.err.println("❌ Authentication failed - check JIRA credentials");
        } else if (response.getStatusCode() == 403) {
            System.err.println("❌ Permission denied - user doesn't have permission to create issues");
        } else {
            System.err.println("❌ Bug creation failed: " + response.getBody().asString());
        }
    }

    public static void addComment(String issueKey, String commentText) {
        String baseUrl = getJiraBaseUrl();
        if (baseUrl == null) {
            return;
        }

        String authHeader = getAuthHeader();
        if (authHeader == null) {
            System.err.println("❌ Cannot add comment - authentication failed");
            return;
        }

        Map<String, Object> adfBody = new HashMap<>();
        adfBody.put("type", "doc");
        adfBody.put("version", 1);
        adfBody.put("content", new Object[]{
                Map.of("type", "paragraph", "content", new Object[]{
                        Map.of("type", "text", "text", commentText)
                })
        });

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("body", adfBody);

        Response response = RestAssured
                .given()
                .baseUri(baseUrl)
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post("rest/api/3/issue/" + issueKey + "/comment");

        if (response.statusCode() == 201 || response.statusCode() == 200) {
            System.out.println("💬 Comment added to " + issueKey);
        } else {
            System.err.println("❌ Failed to add comment (" + response.statusCode() + "): " + response.getBody().asString());
        }
    }


    public static void updateTestCase(String issueKey, String newDescription) {
        String baseUrl = getJiraBaseUrl();
        if (baseUrl == null) {
            return;
        }

        String authHeader = getAuthHeader();
        if (authHeader == null) {
            System.err.println("❌ Cannot update test case - authentication failed");
            return;
        }

        Map<String, Object> fields = new HashMap<>();
        Map<String, Object> description = new HashMap<>();
        description.put("type", "doc");
        description.put("version", 1);
        description.put("content", new Object[]{
                Map.of("type", "paragraph", "content", new Object[]{
                        Map.of("type", "text", "text", newDescription)
                })
        });

        fields.put("description", description);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("fields", fields);

        Response response = RestAssured
                .given()
                .baseUri(baseUrl)
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .put("rest/api/3/issue/" + issueKey);

        if (response.getStatusCode() == 204 || response.getStatusCode() == 200) {
            System.out.println("🔄 Test case " + issueKey + " updated successfully");
        } else {
            System.err.println("❌ Update failed (" + response.getStatusCode() + "): " + response.asString());
        }
    }

    private static String escapeForJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
