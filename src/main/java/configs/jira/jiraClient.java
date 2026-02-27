package configs.jira;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.restassured.RestAssured;
import io.restassured.response.Response;

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
        String baseUrl = configs.loadProps.getJIRAConfig("JIRA_BASE_URL");
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
        String email = configs.loadProps.getJIRAConfig("JIRA_EMAIL");
        String token = configs.loadProps.getJIRAConfig("JIRA_API_TOKEN");

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
     * @param issueKey        JIRA issue key to update
     * @param summary         Test summary
     * @param descriptionText Test description or error message
     * @param attachmentFile  Screenshot file (optional)
     * @param isFailed        Whether the test failed
     */
    public static void updateIssueWithTestResult(String issueKey, String summary, String descriptionText, File attachmentFile,
            boolean isFailed) {
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

            String passCommentText = configs.loadProps.getProperty("PassComment") + " "
                    + configs.loadProps.getProperty("Version");
            String failCommentText = configs.loadProps.getProperty("FailedComment") + " "
                    + configs.loadProps.getProperty("Version");

            // Check if issue exists
            Response checkResponse = RestAssured
                    .given()
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/json")
                    .get(baseUrl + "rest/api/3/issue/" + issueKey);

            if (checkResponse.getStatusCode() == 200) {
                // Fixed: Pass comment for passed tests, fail comment for failed tests
                String commentText = isFailed
                        ? failCommentText + "\n\n*Summary:* " + summary + "\n\n*Details:* " + descriptionText
                        : passCommentText + "\n\n*Test Case:* " + summary;

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
                            System.err.println("❌ Failed to upload screenshot (" + attachResponse.statusCode() + "): "
                                    + attachResponse.getBody().asString());
                        }
                    } else {
                        if (attachmentFile == null) {
                            System.err.println("⚠️ No screenshot file provided (attachmentFile is null)");
                        } else if (!attachmentFile.exists()) {
                            System.err
                                    .println("⚠️ Screenshot file does not exist: " + attachmentFile.getAbsolutePath());
                        } else if (attachmentFile.length() == 0) {
                            System.err.println(
                                    "⚠️ Screenshot file is empty (0 bytes): " + attachmentFile.getAbsolutePath());
                        }
                    }
                }

                System.out.println("✅ JIRA issue " + issueKey + " updated with " + (isFailed ? "failure" : "success")
                        + " comment");
            } else if (checkResponse.getStatusCode() == 404) {
                System.err.println("❌ JIRA issue " + issueKey + " not found (404)");
            } else if (checkResponse.getStatusCode() == 401) {
                System.err.println("❌ JIRA authentication failed (401) - check credentials");
            } else if (checkResponse.getStatusCode() == 403) {
                System.err.println("❌ JIRA access forbidden (403) - check permissions");
            } else {
                System.err.println("❌ Unexpected JIRA API response (" + checkResponse.getStatusCode() + "): "
                        + checkResponse.getBody().asString());
            }
        } catch (Exception e) {
            System.err.println("❌ Error updating JIRA issue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void createBugInJIRA(String summary, String descriptionText, File attachmentFile) {
        String baseUrl = getJiraBaseUrl();
        if (baseUrl == null) {
            return;
        }

        String authHeader = getAuthHeader();
        if (authHeader == null) {
            System.err.println("❌ Cannot create bug - authentication failed");
            return;
        }

        String projectKey = configs.loadProps.getJIRAConfig("PROJECT_KEY");
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
        adfBody.put("content", new Object[] {
                Map.of("type", "paragraph", "content", new Object[] {
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
            System.err.println(
                    "❌ Failed to add comment (" + response.statusCode() + "): " + response.getBody().asString());
        }
    }

    /**
     * Fetches a JIRA story/issue and returns its details.
     * Useful for generating tests from JIRA requirements.
     * 
     * @param issueKey JIRA issue key (e.g., "ECS-123")
     * @return JiraStory object with story details, or null if failed
     */
    public static JiraStory getJiraStory(String issueKey) {
        String baseUrl = getJiraBaseUrl();
        if (baseUrl == null) {
            return null;
        }

        String authHeader = getAuthHeader();
        if (authHeader == null) {
            System.err.println("❌ Cannot fetch story - authentication failed");
            return null;
        }

        try {
            Response response = RestAssured
                    .given()
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/json")
                    .get(baseUrl + "rest/api/3/issue/" + issueKey);

            if (response.getStatusCode() == 200) {
                // Parse response
                String key = response.jsonPath().getString("key");
                String summary = response.jsonPath().getString("fields.summary");
                String issueType = response.jsonPath().getString("fields.issuetype.name");
                String status = response.jsonPath().getString("fields.status.name");
                String priority = response.jsonPath().getString("fields.priority.name");

                // Parse description (ADF format)
                String description = extractPlainTextFromADF(response.jsonPath().get("fields.description"));

                // Extract acceptance criteria
                List<String> acceptanceCriteria = extractAcceptanceCriteria(description);

                JiraStory story = new JiraStory(key, summary, description, issueType, status, priority,
                        acceptanceCriteria);

                System.out.println("✅ Fetched JIRA story: " + key);
                return story;

            } else if (response.getStatusCode() == 404) {
                System.err.println("❌ JIRA issue " + issueKey + " not found (404)");
            } else if (response.getStatusCode() == 401) {
                System.err.println("❌ JIRA authentication failed (401) - check credentials");
            } else {
                System.err.println(
                        "❌ Failed to fetch story (" + response.getStatusCode() + "): " + response.getBody().asString());
            }
        } catch (Exception e) {
            System.err.println("❌ Error fetching JIRA story: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Extracts plain text from Atlassian Document Format (ADF).
     * 
     * @param adfObject ADF content object
     * @return Plain text string
     */
    private static String extractPlainTextFromADF(Object adfObject) {
        if (adfObject == null) {
            return "";
        }

        StringBuilder text = new StringBuilder();

        if (adfObject instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> adfMap = (Map<String, Object>) adfObject;

            String type = (String) adfMap.get("type");

            if ("text".equals(type)) {
                String textContent = (String) adfMap.get("text");
                if (textContent != null) {
                    text.append(textContent);
                }
            } else if ("hardBreak".equals(type)) {
                text.append("\n");
            }

            // Recursively process content array
            Object content = adfMap.get("content");
            if (content instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> contentList = (List<Object>) content;
                for (Object item : contentList) {
                    String childText = extractPlainTextFromADF(item);
                    text.append(childText);
                }

                // Add paragraph breaks
                if ("paragraph".equals(type) && text.length() > 0) {
                    text.append("\n");
                }
            }
        }

        return text.toString();
    }

    /**
     * Extracts acceptance criteria from story description.
     * Looks for patterns like "Acceptance Criteria:", Given/When/Then, bullet
     * points.
     * 
     * @param description Story description text
     * @return List of acceptance criteria strings
     */
    private static List<String> extractAcceptanceCriteria(String description) {
        List<String> criteria = new ArrayList<>();

        if (description == null || description.trim().isEmpty()) {
            return criteria;
        }

        // Split by lines
        String[] lines = description.split("\n");

        boolean inAcceptanceCriteria = false;
        for (String line : lines) {
            String trimmed = line.trim();

            // Detect acceptance criteria section
            if (trimmed.matches("(?i).*acceptance criteria.*")) {
                inAcceptanceCriteria = true;
                continue;
            }

            // Stop if we hit another section
            if (inAcceptanceCriteria && trimmed.matches("(?i)^(description|notes|background):.*")) {
                break;
            }

            // Extract criteria lines
            if (inAcceptanceCriteria && !trimmed.isEmpty()) {
                // Remove bullet points, numbers, dashes
                String cleaned = trimmed.replaceFirst("^[•\\-*\\d+\\.\\)\\]]+\\s*", "");

                // Check if it's a Given/When/Then pattern
                if (cleaned.matches("(?i)^(given|when|then|and)\\s+.*")) {
                    criteria.add(cleaned);
                }
                // Or a regular criteria statement
                else if (cleaned.length() > 10) {
                    criteria.add(cleaned);
                }
            }
        }

        return criteria;
    }

    /**
     * JIRA Story data class.
     */
    public static class JiraStory {
        public final String key;
        public final String summary;
        public final String description;
        public final String issueType;
        public final String status;
        public final String priority;
        public final List<String> acceptanceCriteria;

        public JiraStory(String key, String summary, String description, String issueType,
                String status, String priority, List<String> acceptanceCriteria) {
            this.key = key;
            this.summary = summary;
            this.description = description;
            this.issueType = issueType;
            this.status = status;
            this.priority = priority;
            this.acceptanceCriteria = acceptanceCriteria != null ? acceptanceCriteria : new ArrayList<>();
        }

        @Override
        public String toString() {
            return "JiraStory{" +
                    "key='" + key + '\'' +
                    ", summary='" + summary + '\'' +
                    ", issueType='" + issueType + '\'' +
                    ", status='" + status + '\'' +
                    ", priority='" + priority + '\'' +
                    ", acceptanceCriteria=" + acceptanceCriteria.size() + " items" +
                    '}';
        }

        /**
         * Prints formatted story details to console.
         */
        public void printDetails() {
            System.out.println("\n📋 JIRA Story Details:");
            System.out.println("  Key: " + key);
            System.out.println("  Type: " + issueType);
            System.out.println("  Status: " + status);
            System.out.println("  Priority: " + priority);
            System.out.println("  Summary: " + summary);

            if (description != null && !description.isEmpty()) {
                System.out.println("\n  Description:");
                System.out.println("  " + description.replace("\n", "\n  "));
            }

            if (!acceptanceCriteria.isEmpty()) {
                System.out.println("\n  Acceptance Criteria:");
                for (int i = 0; i < acceptanceCriteria.size(); i++) {
                    System.out.println("    " + (i + 1) + ". " + acceptanceCriteria.get(i));
                }
            }
            System.out.println();
        }
    }

    public static void updateDescription(String issueKey, String newDescription) {
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
        description.put("content", new Object[] {
                Map.of("type", "paragraph", "content", new Object[] {
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

    /**
     * Transitions a JIRA issue to a specific status (e.g., "Done", "In Progress").
     * 
     * @param issueKey JIRA issue key (e.g., "ECS-123")
     * @param transitionName Name of the transition (e.g., "Done", "To Do", "In Progress")
     * @return true if transition successful, false otherwise
     */
    public static boolean transitionIssue(String issueKey, String transitionName) {
        String baseUrl = getJiraBaseUrl();
        if (baseUrl == null) {
            return false;
        }

        String authHeader = getAuthHeader();
        if (authHeader == null) {
            System.err.println("❌ Cannot transition issue - authentication failed");
            return false;
        }

        try {
            // Get available transitions for this issue
            Response transitionsResponse = RestAssured
                    .given()
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/json")
                    .get(baseUrl + "rest/api/3/issue/" + issueKey + "/transitions");

            if (transitionsResponse.getStatusCode() != 200) {
                System.err.println("❌ Failed to get transitions for " + issueKey + " (" + transitionsResponse.getStatusCode() + ")");
                return false;
            }

            // Find transition ID by name
            List<Map<String, Object>> transitions = transitionsResponse.jsonPath().getList("transitions");
            String transitionId = null;

            for (Map<String, Object> transition : transitions) {
                String name = (String) transition.get("name");
                if (name != null && name.equalsIgnoreCase(transitionName)) {
                    transitionId = String.valueOf(transition.get("id"));
                    break;
                }
            }

            if (transitionId == null) {
                System.err.println("❌ Transition '" + transitionName + "' not available for " + issueKey);
                System.out.println("   Available transitions:");
                transitions.forEach(t -> System.out.println("   - " + t.get("name")));
                return false;
            }

            // Execute transition
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> transitionMap = new HashMap<>();
            transitionMap.put("id", transitionId);
            requestBody.put("transition", transitionMap);

            Response response = RestAssured
                    .given()
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .post(baseUrl + "rest/api/3/issue/" + issueKey + "/transitions");

            if (response.getStatusCode() == 204 || response.getStatusCode() == 200) {
                System.out.println("✅ " + issueKey + " transitioned to '" + transitionName + "'");
                return true;
            } else {
                System.err.println("❌ Transition failed (" + response.getStatusCode() + "): " + response.getBody().asString());
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Error transitioning issue: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Marks a JIRA story as complete/done after successful test execution.
     * Adds a success comment and attempts to transition to "Done" status.
     * 
     * @param issueKey JIRA issue key
     * @param testName Name of the test that passed
     * @param executionTime Test execution time in milliseconds
     * @return true if successfully completed, false otherwise
     */
    public static boolean completeStory(String issueKey, String testName, long executionTime) {
        String baseUrl = getJiraBaseUrl();
        if (baseUrl == null) {
            return false;
        }

        try {
            // Add completion comment
            String version = configs.loadProps.getProperty("Version");
            String commentText = String.format(
                "✅ *Automation Complete*\n\n" +
                "Test: %s\n" +
                "Status: PASSED\n" +
                "Execution Time: %.2f seconds\n" +
                "Version: %s\n" +
                "Date: %s",
                testName,
                executionTime / 1000.0,
                version != null ? version : "N/A",
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())
            );

            addComment(issueKey, commentText);

            // Attempt to transition to Done
            // Common transition names: "Done", "Complete", "Resolve", "Close"
            String[] possibleDoneTransitions = {"Done", "Complete", "Resolve", "Close", "Resolved", "Closed"};
            
            for (String transition : possibleDoneTransitions) {
                if (transitionIssue(issueKey, transition)) {
                    System.out.println("🎉 Story " + issueKey + " marked as complete!");
                    return true;
                }
            }

            System.out.println("⚠️ Story " + issueKey + " updated with comment, but could not auto-transition to Done");
            System.out.println("   (Transition may need to be done manually in JIRA)");
            return false;

        } catch (Exception e) {
            System.err.println("❌ Error completing story: " + e.getMessage());
            return false;
        }
    }

    /**
     * Command-line interface for JIRA operations.
     * Usage:
     *   mvn exec:java -Dexec.mainClass=configs.jira.jiraClient -Dexec.args="completeStory ECS-123 LoginTest 5000"
     *   mvn exec:java -Dexec.mainClass=configs.jira.jiraClient -Dexec.args="transitionIssue ECS-123 Done"
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("❌ No command specified");
            System.out.println("Usage:");
            System.out.println("  completeStory <issueKey> <testName> <executionTime>");
            System.out.println("  transitionIssue <issueKey> <transitionName>");
            System.exit(1);
        }

        String command = args[0];

        try {
            switch (command) {
                case "completeStory":
                    if (args.length < 4) {
                        System.err.println("❌ Usage: completeStory <issueKey> <testName> <executionTime>");
                        System.exit(1);
                    }
                    String issueKey = args[1];
                    String testName = args[2];
                    long executionTime = Long.parseLong(args[3]);
                    boolean success = completeStory(issueKey, testName, executionTime);
                    System.exit(success ? 0 : 1);
                    break;

                case "transitionIssue":
                    if (args.length < 3) {
                        System.err.println("❌ Usage: transitionIssue <issueKey> <transitionName>");
                        System.exit(1);
                    }
                    boolean transitioned = transitionIssue(args[1], args[2]);
                    System.exit(transitioned ? 0 : 1);
                    break;

                default:
                    System.err.println("❌ Unknown command: " + command);
                    System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String escapeForJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
