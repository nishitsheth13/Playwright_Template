package configs.jira;


import configs.loadProps;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.io.File;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class jiraClient {

    private static String getAuthHeader() {
        String auth = loadProps.getJIRAConfig("JIRA_EMAIL")+ ":" + loadProps.getJIRAConfig("JIRA_API_TOKEN");
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }

    public static void handleTestCaseResultSmart(String issueKey, String summary, String descriptionText, File attachmentFile, boolean isFailed) {
        String passCommentText = loadProps.getProperty("PassComment") + " " + loadProps.getProperty("Version");
        String failCommentText = loadProps.getProperty("FailComment") + " " + loadProps.getProperty("Version");
        // Step 1: Check if issue exists
        Response checkResponse = RestAssured
                .given()
                .header("Authorization", getAuthHeader())
                .header("Content-Type", "application/json")
                .get(loadProps.getJIRAConfig("JIRA_BASE_URL") + "/rest/api/3/issue/" + issueKey);
        if (!isFailed) {
            if (checkResponse.getStatusCode() == 200) {
                addComment(issueKey, passCommentText);
            }
        } else {
            if (isFailed) {
                addComment(issueKey, failCommentText);
            } else if (checkResponse.getStatusCode() == 404) {
                System.out.println("❌ Unexpected Jira API response: " + checkResponse.getBody().asString());
            }
        }
    }

    public static void createBug(String summary, String descriptionText, File attachmentFile) {
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
                "    \"project\": { \"key\": \"" + loadProps.getJIRAConfig("PROJECT_KEY") + "\" },\n" +
                "    \"summary\": \"" + escapeForJson(summary) + "\",\n" +
                "    \"description\": " + adfDescription + ",\n" +
                "    \"labels\": [\"BugByAutomationFailure\"],\n" +
                "    \"issuetype\": { \"name\": \"Bug\" }\n" +
                "  }\n" +
                "}";

        Response response = RestAssured
                .given()
                .header("Authorization", getAuthHeader())
                .header("Content-Type", "application/json")
                .body(payload)
                .post(loadProps.getJIRAConfig("JIRA_BASE_URL") + "/rest/api/3/issue");

        System.out.println(response.asPrettyString());

        if (response.getStatusCode() == 201) {
            String createdIssueKey = response.jsonPath().getString("key");
            System.out.println("✅ Bug created in JIRA: " + createdIssueKey);

            // Attach screenshot if provided
            if (attachmentFile != null && attachmentFile.exists()) {
                Response attachResponse = RestAssured
                        .given()
                        .header("Authorization", getAuthHeader())
                        .header("X-Atlassian-Token", "no-check")
                        .multiPart("file", attachmentFile)
                        .post(loadProps.getJIRAConfig("JIRA_BASE_URL") + "/rest/api/3/issue/" + createdIssueKey + "/attachments");

                if (attachResponse.statusCode() == 200 || attachResponse.statusCode() == 201) {
                    System.out.println("📎 Attachment uploaded successfully.");
                } else {
                    System.out.println("❌ Failed to upload attachment: " + attachResponse.getBody().asString());
                }
            } else {
                System.out.println("⚠️ No attachment provided.");
            }

        } else {
            System.out.println("❌ Bug creation failed: " + response.getBody().asString());
        }
    }

    public static void addComment(String issueKey, String commentText) {
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
                .baseUri(loadProps.getJIRAConfig("JIRA_BASE_URL"))
                .header("Authorization", getAuthHeader())
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post("/rest/api/3/issue/" + issueKey + "/comment");

        if (response.statusCode() == 201 || response.statusCode() == 200) {
            System.out.println("💬 ADF Comment added to " + issueKey);
        } else {
            System.out.println("❌ Failed to add ADF comment: " + response.getStatusCode());
            System.out.println("Response body: " + response.getBody().asString());
        }
    }


    public static void updateTestCase(String issueKey, String newDescription) {
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
                .baseUri(loadProps.getJIRAConfig("JIRA_BASE_URL"))
                .header("Authorization", getAuthHeader())
                .header("Content-Type", "application/json")
                .body(requestBody)
                .put("/rest/api/3/issue/" + issueKey);

        System.out.println("🔄 Update Status: " + response.getStatusCode());
        System.out.println("🔄 Response: " + response.asString());
    }

    private static String escapeForJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
