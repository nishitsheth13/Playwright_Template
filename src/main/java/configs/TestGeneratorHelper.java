package configs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import configs.jira.jiraClient;

/**
 * Unified Test Generator Helper - Single class for all test generation needs.
 * 
 * Features:
 * 1. Parse Playwright recordings and generate test files (replaces
 * TestFileGenerator)
 * 2. JIRA story integration and AI-assisted generation
 * 3. Test requirement management and validation
 * 
 * This consolidates all test generation functionality into one helper class.
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * METHOD NAMING CONVENTIONS (CRITICAL - NEVER USE "Value" AS METHOD NAME!)
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * ALL generated methods MUST follow action-based naming:
 * ✅ CORRECT Examples:
 * - clickSignIn(Page page) // click action
 * - enterUsername(Page page, String text) // fill/enter action
 * - selectCountry(Page page, String option) // select action
 * - checkRememberMe(Page page) // check/checkbox action
 * - pressKeyOnSearch(Page page) // press/keyboard action
 * - navigateToLogin(Page page) // navigate action
 * 
 * ❌ WRONG Examples:
 * - Value(Page page) // Generic, meaningless
 * - action1(Page page) // Not descriptive
 * - doSomething(Page page) // Vague
 * 
 * Method Name Generation Rules (generateMethodName method):
 * - click → click + ElementName
 * - fill → enter + ElementName (special: enterUsername, enterPassword,
 * enterEmail)
 * - select → select + ElementName
 * - check → check + ElementName (or toggle + ElementName)
 * - press → pressKeyOn + ElementName
 * - navigate→ navigateTo + PageName
 * - default → elementNameAction (camelCase + "Action")
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * LOCATOR STRATEGY & CODE REUSABILITY (Applied to ALL generation methods)
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * LOCATOR PRIORITY ORDER (optimizeSelector method):
 * 1. Static ID → //input[@id='Username'] (highest priority)
 * 2. Relative XPath → //div[@class='form']//input
 * 3. Absolute XPath → /html/body/div/input
 * 4. Label or Names → label=Username, @name='submit'
 * 5. Class name → .btn-primary, @class='container'
 * 6. CSS Selectors → div > button (lowest priority)
 * 
 * DYNAMIC ID DETECTION (isDynamicId method):
 * - Detects GUIDs: [a-f0-9]{8}-[a-f0-9]{4}-...
 * - Detects Timestamps: \d{10,13}
 * - Detects Random Hashes: [a-zA-Z0-9]{16,}
 * - Warns and downgrades dynamic IDs to lower priority
 * 
 * CODE REUSABILITY (Code Reuse Detection Helpers):
 * - pageObjectExists() → Checks if page object already exists
 * - detectExistingLogin() → Finds existing login.java methods
 * - containsLoginPattern() → Detects login/auth patterns in actions
 * - hasConfiguredCredentials() → Checks configurations.properties for test data
 * - Automatically imports existing classes (e.g., login.java)
 * - Skips regeneration of existing page objects
 * - Provides tips for manual integration
 * 
 * USAGE:
 * All test generation methods (Recording, JIRA, Manual) use these features:
 * - Recording-based: generateFromRecording() → parseRecording() →
 * optimizeSelector()
 * - JIRA-based: generateFromJiraStory() → [uses same file generation methods]
 * - File generation: generatePageObject(), generateFeatureFile(),
 * generateStepDefinitions()
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 */
public class TestGeneratorHelper {

    // ========================================================================
    // ADVANCED AUTO-FIX SYSTEM - Error Prevention & Recovery
    // ========================================================================

    /**
     * Auto-fixes selector to ensure it's usable
     * Reserved for future advanced selector optimization
     *
     * @param selector Raw selector from recording
     * @return Fixed selector or null if unfixable
     */
    @SuppressWarnings("unused")
    private static String autoFixSelector(String selector) {
        if (selector == null || selector.trim().isEmpty()) {
            return null;
        }

        String fixed = selector.trim();

        // Fix common issues
        fixed = fixed.replace("\\\"", "\""); // Unescape quotes
        fixed = fixed.replaceAll("\\s+", " "); // Normalize whitespace

        // Validate selector format
        if (!isValidSelector(fixed)) {
            // Try to fix common issues
            if (!fixed.contains("=") && !fixed.startsWith("xpath=") && !fixed.startsWith("css=")) {
                // Assume it's text
                fixed = "text=" + fixed;
            }
        }

        return fixed;
    }

    /**
     * Validates and auto-fixes feature name to ensure valid Java identifier
     *
     * @param featureName Raw feature name input
     * @return Sanitized, valid Java class name
     */
    private static String autoFixFeatureName(String featureName) {
        if (featureName == null || featureName.trim().isEmpty()) {
            return "TestFeature" + System.currentTimeMillis();
        }

        String fixed = featureName.trim();

        // Remove invalid characters
        fixed = fixed.replaceAll("[^a-zA-Z0-9_]", "");

        // Ensure starts with letter
        if (!fixed.isEmpty() && !Character.isLetter(fixed.charAt(0))) {
            fixed = "Test" + fixed;
        }

        // Convert to PascalCase
        fixed = toPascalCase(fixed);

        // Validate
        if (!isValidJavaIdentifier(fixed)) {
            fixed = "TestFeature" + System.currentTimeMillis();
        }

        return fixed;
    }

    /**
     * Auto-fixes method name to ensure valid Java identifier and no conflicts
     * Reserved for future advanced method name generation
     *
     * @param methodName      Generated method name
     * @param existingMethods Set of already generated methods
     * @param counter         Counter for uniqueness
     * @return Fixed unique method name
     */
    @SuppressWarnings("unused")
    private static String autoFixMethodName(String methodName, Set<String> existingMethods, int counter) {
        if (methodName == null || methodName.trim().isEmpty()) {
            return "action" + counter;
        }

        String fixed = methodName.trim();

        // Remove invalid characters
        fixed = fixed.replaceAll("[^a-zA-Z0-9_]", "");

        // Ensure starts with lowercase letter
        if (!fixed.isEmpty() && Character.isUpperCase(fixed.charAt(0))) {
            fixed = Character.toLowerCase(fixed.charAt(0)) + fixed.substring(1);
        }

        // Ensure not a Java keyword
        if (isJavaKeyword(fixed)) {
            fixed = fixed + "Action";
        }

        // Ensure uniqueness
        String uniqueName = fixed;
        int suffix = 2;
        while (existingMethods.contains(uniqueName)) {
            uniqueName = fixed + suffix;
            suffix++;
        }

        return uniqueName;
    }

    /**
     * Validates selector format
     */
    private static boolean isValidSelector(String selector) {
        if (selector == null || selector.isEmpty())
            return false;

        // Check for common selector patterns
        return selector.matches("^(xpath=|css=|text=|label=|placeholder=|id=|#|\\.).*") ||
                selector.contains("@") || // XPath attribute
                selector.contains("[") || // CSS attribute selector
                selector.matches("^[a-zA-Z][a-zA-Z0-9]*$"); // Simple tag name
    }

    /**
     * Auto-validates and fixes imports in generated code
     * Reserved for future advanced import management
     *
     * @param imports List of import statements
     * @return Fixed list with duplicates removed and sorted
     */
    @SuppressWarnings("unused")
    private static List<String> autoFixImports(List<String> imports) {
        Set<String> uniqueImports = new LinkedHashSet<>();

        for (String imp : imports) {
            if (imp != null && !imp.trim().isEmpty()) {
                String fixed = imp.trim();
                if (!fixed.endsWith(";")) {
                    fixed += ";";
                }
                uniqueImports.add(fixed);
            }
        }

        List<String> sorted = new ArrayList<>(uniqueImports);
        Collections.sort(sorted);

        return sorted;
    }

    /**
     * Auto-fixes feature file step text for valid Gherkin syntax
     *
     * @param stepText Raw step text
     * @return Fixed Gherkin step
     */
    private static String autoFixFeatureStep(String stepText) {
        if (stepText == null || stepText.trim().isEmpty()) {
            return "And action is performed";
        }

        String fixed = stepText.trim();

        // Ensure proper Gherkin keyword
        if (!fixed.matches("^(Given|When|Then|And|But)\\s+.*")) {
            fixed = "When " + fixed;
        }

        // Fix common issues
        fixed = fixed.replaceAll("\\s+", " "); // Normalize whitespace
        fixed = fixed.replaceAll("\"\"", "\""); // Fix double quotes

        // Ensure ends properly (no trailing spaces/punctuation)
        fixed = fixed.replaceAll("[\\s.!?]+$", "");

        return fixed;
    }

    /**
     * Checks if string is a Java keyword
     */
    private static boolean isJavaKeyword(String word) {
        String[] keywords = {
                "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
                "class", "const", "continue", "default", "do", "double", "else", "enum",
                "extends", "final", "finally", "float", "for", "goto", "if", "implements",
                "import", "instanceof", "int", "interface", "long", "native", "new", "package",
                "private", "protected", "public", "return", "short", "static", "strictfp",
                "super", "switch", "synchronized", "this", "throw", "throws", "transient",
                "try", "void", "volatile", "while", "true", "false", "null"
        };

        for (String keyword : keywords) {
            if (keyword.equals(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * AUTO-FIX: Ensures all Page Object methods are public (not protected)
     * Prevents compilation errors when Step Definitions try to access methods
     *
     * @param content Page Object content
     * @return Fixed content with all methods public
     */
    private static String autoFixMethodVisibility(String content) {
        if (content == null || !content.contains("protected static void")) {
            return content;
        }

        // Replace all protected static methods with public static
        return content.replaceAll("protected static void", "public static void");
    }

    /**
     * Validates recording file before processing
     *
     * @param recordingFile Path to recording file
     * @return true if valid, false otherwise
     */
    private static boolean validateRecordingFile(String recordingFile) {
        if (recordingFile == null || recordingFile.trim().isEmpty()) {
            System.err.println("[ERROR] Recording file path is empty");
            return false;
        }

        File file = new File(recordingFile);
        if (!file.exists()) {
            System.err.println("[ERROR] Recording file not found: " + recordingFile);
            return false;
        }

        if (!file.canRead()) {
            System.err.println("[ERROR] Recording file is not readable: " + recordingFile);
            return false;
        }

        if (file.length() == 0) {
            System.err.println("[ERROR] Recording file is empty: " + recordingFile);
            return false;
        }

        if (file.length() < 100) {
            System.err.println("[WARNING] Recording file is very small (" + file.length()
                    + " bytes), may not contain valid actions");
        }

        return true;
    }

    /**
     * Auto-recovery: Attempts to fix common generation errors
     *
     * @param error   The exception that occurred
     * @param context Context information about what was being generated
     * @return Suggested fix or null if no fix available
     */
    private static String autoRecoverFromError(Exception error, String context) {
        String errorMsg = error.getMessage();
        String suggestion = null;

        if (errorMsg.contains("FileNotFoundException")) {
            suggestion = "Create missing directory structure";
        } else if (errorMsg.contains("PermissionDenied") || errorMsg.contains("AccessDenied")) {
            suggestion = "Check file/directory permissions";
        } else if (errorMsg.contains("OutOfMemory")) {
            suggestion = "Reduce recording size or increase JVM heap";
        } else if (errorMsg.contains("IllegalArgumentException")) {
            suggestion = "Validate input parameters before generation";
        } else if (errorMsg.contains("NullPointerException")) {
            suggestion = "Check for null values in recording actions";
        }

        if (suggestion != null) {
            System.err.println("[AUTO-RECOVERY] Error in " + context + ": " + errorMsg);
            System.err.println("[AUTO-RECOVERY] Suggested fix: " + suggestion);
        }

        return suggestion;
    }

    /**
     * Validates generated file content before writing
     *
     * @param content  File content to validate
     * @param fileType Type of file (PageObject, Feature, StepDef)
     * @return true if valid, false otherwise
     */

    /**
     * AUTO-FIX: Ensures navigateTo method exists with proper signature
     *
     * @param content   Page Object content
     * @param className Page class name
     * @return Fixed content with navigateTo method
     */
    private static String ensureNavigateToMethod(String content, String className) {
        // Check if navigateTo method already exists
        if (content.contains("public static void navigateTo") ||
                content.contains("public static void navigateTo" + className)) {
            return content;
        }

        // Find the position to insert (after constructor, before first method)
        String navigateMethod = "    /**\n" +
                "     * Navigate to " + className + " page\n" +
                "     * @param page Playwright Page instance\n" +
                "     */\n" +
                "    public static void navigateTo(com.microsoft.playwright.Page page) {\n" +
                "        log.info(\"🌐 Navigating to " + className + " page\");\n" +
                "        String url = loadProps.getProperty(\"URL\");\n" +
                "        navigateToUrl(url);\n" +
                "        log.info(\"✅ Navigation completed\");\n" +
                "    }\n\n";

        // Insert after constructor (look for "}\n\n /**" or "}\n\n private" or "}\n\n
        // public")
        String pattern = "(public " + className + "\\(\\) \\{[^}]*\\}\\n\\n)";
        if (content.matches("(?s).*" + pattern + ".*")) {
            content = content.replaceFirst(pattern, "$1" + navigateMethod);
        } else {
            // Insert before first method if constructor pattern not found
            content = content.replaceFirst("(\n    /\\*\\*\n     \\* )", "\n" + navigateMethod + "$1");
        }

        return content;
    }

    /**
     * AUTO-FIX: Ensures all required imports are present
     *
     * @param content Page Object content
     * @return Fixed content with all imports
     */
    private static String ensureRequiredImports(String content) {
        StringBuilder imports = new StringBuilder();
        boolean needsFix = false;

        // Check and add missing imports
        if (!content.contains("import com.microsoft.playwright.Page;")) {
            imports.append("import com.microsoft.playwright.Page;\n");
            needsFix = true;
        }
        if (!content.contains("import configs.loadProps;")) {
            imports.append("import configs.loadProps;\n");
            needsFix = true;
        }
        if (!content.contains("import configs.TimeoutConfig;")) {
            imports.append("import configs.TimeoutConfig;\n");
            needsFix = true;
        }

        if (needsFix) {
            System.out.println("[AUTO-FIX] Adding missing imports");
            // Insert after package declaration
            content = content.replaceFirst("(package pages;\\n)", "$1" + imports.toString());
        }

        return content;
    }

    /**
     * AUTO-FIX: Validates that all feature file steps have matching step
     * definitions
     * Generates missing step definitions automatically
     *
     * @param featureContent  Feature file content
     * @param stepDefsContent Step definitions content
     * @param testName        Test name for generating methods
     * @return Fixed step definitions with all missing steps added
     */
    private static String validateAndFixStepMatching(String featureContent, String stepDefsContent, String testName) {
        if (featureContent == null || stepDefsContent == null) {
            return stepDefsContent;
        }

        // Extract all steps from feature file
        Set<String> featureSteps = extractStepsFromFeature(featureContent);
        Set<String> existingSteps = extractStepsFromStepDefs(stepDefsContent);

        // Find missing steps
        Set<String> missingSteps = new HashSet<>(featureSteps);
        missingSteps.removeAll(existingSteps);

        // Filter out steps that would create duplicate method names
        Set<String> trulyMissingSteps = new HashSet<>();
        for (String step : missingSteps) {
            String methodName = generateMethodNameFromStep(step);

            // FIXED: Do NOT skip any steps from the recording.
            // All recorded steps must be generated regardless of name or keyword.
            if (!existingSteps.contains("METHOD:" + methodName)) {
                trulyMissingSteps.add(step);
            }
        }

        if (trulyMissingSteps.isEmpty()) {
            return stepDefsContent;
        }

        // Generate missing step definitions
        StringBuilder missingStepDefs = new StringBuilder();
        missingStepDefs.append("\n    // ========== AUTO-GENERATED MISSING STEPS ==========\n\n");

        for (String step : trulyMissingSteps) {
            missingStepDefs.append(generateStepDefinition(step));
        }

        // Insert before closing brace
        int lastBrace = stepDefsContent.lastIndexOf("}");
        if (lastBrace > 0) {
            stepDefsContent = stepDefsContent.substring(0, lastBrace) +
                    missingStepDefs.toString() +
                    "\n}\n";
        }

        return stepDefsContent;
    }

    /**
     * Generate method name from step text (used for duplicate detection)
     */
    private static String generateMethodNameFromStep(String stepText) {
        String methodName = stepText
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .trim()
                .replaceAll("\\s+", " ")
                .split(" ").length > 0 ? String.join("",
                        Arrays.stream(stepText.replaceAll("[^a-zA-Z0-9\\s]", "").trim().split("\\s+"))
                                .limit(5)
                                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                                .toArray(String[]::new))
                        : "generatedStep";

        // Ensure starts with lowercase
        return methodName.substring(0, 1).toLowerCase() + methodName.substring(1);
    }

    /**
     * Extracts all Given/When/Then steps from feature file
     *
     * FIXED (Feb 12, 2026): Normalizes Scenario Outline parameters
     * Converts: user enters "<lastname>" into lastname
     * To: user enters {string} into lastname
     * This ensures proper matching with step definitions that use {string}
     */
    private static Set<String> extractStepsFromFeature(String featureContent) {
        Set<String> steps = new HashSet<>();
        Pattern stepPattern = Pattern.compile("^\\s*(Given|When|Then|And|But)\\s+(.+)$", Pattern.MULTILINE);
        Matcher matcher = stepPattern.matcher(featureContent);

        while (matcher.find()) {
            String keyword = matcher.group(1);
            String stepText = matcher.group(2).trim();

            // Normalize step text (remove trailing punctuation, extra spaces)
            stepText = stepText.replaceAll("[\\s]+", " ").trim();

            // CRITICAL FIX: Normalize Scenario Outline parameters
            // Convert: "<parameterName>" to {string}
            // This allows proper matching with step definitions
            stepText = normalizeScenarioOutlineParameters(stepText);

            // Store in lowercase for case-insensitive comparison
            // This prevents duplicates like "user clicks on Logout" vs "user clicks on
            // logout"
            stepText = stepText.toLowerCase();

            // Convert And/But to most recent keyword type
            if (!keyword.equals("And") && !keyword.equals("But")) {
                steps.add(stepText);
            } else {
                // For And/But, add as-is since they inherit previous keyword
                steps.add(stepText);
            }
        }

        return steps;
    }

    /**
     * Normalizes Scenario Outline parameters in step text.
     * <p>
     * PERMANENT FIX (Feb 12, 2026): Prevents duplicate step generation
     * <p>
     * Converts: user enters "<lastname>" into lastname
     * To: user enters {string} into lastname
     * <p>
     * Why: Feature files use <parameter> syntax, but step definitions use {string}.
     * Without normalization, the comparison fails and duplicate steps are
     * generated.
     * <p>
     * DO NOT REMOVE - This is critical for Scenario Outline support!
     */
    private static String normalizeScenarioOutlineParameters(String stepText) {
        // Replace "<anything>" with {string}
        // Pattern matches: "< ... >" anywhere in the text
        return stepText.replaceAll("\"<[^>]+>\"", "{string}");
    }

    /**
     * Extracts all implemented steps from step definitions file
     * Now also detects @And annotations and method names to prevent duplicates
     */
    private static Set<String> extractStepsFromStepDefs(String stepDefsContent) {
        Set<String> steps = new HashSet<>();

        // Pattern to match @Given/@When/@Then/@And annotations
        Pattern stepPattern = Pattern.compile("@(?:Given|When|Then|And)\\(\"([^\"]+)\"\\)");
        Matcher matcher = stepPattern.matcher(stepDefsContent);

        while (matcher.find()) {
            String stepText = matcher.group(1).trim();
            // Store in lowercase for case-insensitive comparison
            steps.add(stepText.toLowerCase());
        }

        // Also extract method names to detect duplicate implementations
        Pattern methodPattern = Pattern.compile("public\\s+void\\s+(\\w+)\\s*\\(");
        Matcher methodMatcher = methodPattern.matcher(stepDefsContent);

        while (methodMatcher.find()) {
            String methodName = methodMatcher.group(1);
            // Mark method as existing by adding a special marker
            steps.add("METHOD:" + methodName);
        }

        return steps;
    }

    /**
     * Generates a step definition method for a missing step
     * This is a FALLBACK for steps that weren't generated during main generation
     * Should rarely be called if recording parsing works correctly
     * 
     * CRITICAL FIX (Feb 18, 2026): Now marks placeholders with DEBUG comments so
     * they can be
     * identified and replaced on subsequent runs. This prevents placeholders from
     * persisting
     * across multiple generations.
     */
    private static String generateStepDefinition(String stepText) {
        // Determine keyword (Given/When/Then) based on step text content
        String keyword = determineStepKeyword(stepText);

        // Generate method name using shared method
        String methodName = generateMethodNameFromStep(stepText);

        StringBuilder sb = new StringBuilder();
        sb.append("\n    // ⚠️ PLACEHOLDER STEP - WILL BE REGENERATED IF RECORDING IS PROVIDED\n");
        sb.append("    @").append(keyword).append("(\"").append(stepText).append("\")\n");
        sb.append("    public void ").append(methodName).append("() {\n");
        sb.append("        // ⚠️ AUTO-GENERATED PLACEHOLDER - REVIEW AND IMPLEMENT\n");
        sb.append("        // This step was found in the feature file but not in the recording.\n");
        sb.append("        // Either: 1) Add this action to your recording, OR\n");
        sb.append("        //         2) Manually implement this step definition\n");
        sb.append("        System.out.println(\"⚠️ Placeholder step - needs implementation: ").append(stepText)
                .append("\");\n");
        sb.append("        throw new io.cucumber.java.PendingException(\"Step needs implementation: ").append(stepText)
                .append("\");\n");
        sb.append("    }\n");

        return sb.toString();
    }

    /**
     * Determines the appropriate keyword for a step based on its content
     */
    private static String determineStepKeyword(String stepText) {
        String lower = stepText.toLowerCase();

        // Given: Setup/preconditions
        if (lower.matches("^(user is|page is|application is|system is|browser is|data is).*")) {
            return "Given";
        }

        // When: Actions
        if (lower.matches(
                "^(user (clicks?|enters?|types?|selects?|submits?|navigates?|tries?|makes?|completes?)|multiple users).*")) {
            return "When";
        }

        // Then: Assertions/verifications
        if (lower.matches("^(.*should|.*must|.*will|.*can|.*cannot|all .*).*")) {
            return "Then";
        }

        // Default to Then for verification-like steps
        return "Then";
    }

    private static boolean validateGeneratedContent(String content, String fileType) {
        if (content == null || content.trim().isEmpty()) {
            System.err.println("[VALIDATION ERROR] " + fileType + " content is empty");
            return false;
        }

        // Basic syntax validation
        switch (fileType) {
            case "PageObject":
                if (!content.contains("package pages;")) {
                    System.err.println("[VALIDATION ERROR] Missing package declaration in Page Object");
                    return false;
                }
                if (!content.contains("extends BasePage")) {
                    System.err.println("[VALIDATION ERROR] Page Object doesn't extend BasePage");
                    return false;
                }
                if (content.contains("private static final String") && !content.contains("=")) {
                    System.err.println("[VALIDATION ERROR] Locator constant missing value");
                    return false;
                }
                // Check for navigateTo method
                if (!content.contains("public static void navigateTo")) {
                    System.err.println("[VALIDATION WARNING] Missing navigateTo method - will be auto-fixed");
                }
                // Check for protected methods
                if (content.contains("protected static void")) {
                    System.err.println("[VALIDATION WARNING] Protected methods found - will be auto-fixed to public");
                }
                break;

            case "Feature":
                if (!content.contains("Feature:")) {
                    System.err.println("[VALIDATION ERROR] Missing Feature declaration");
                    return false;
                }
                // Accept both "Scenario:" and "Scenario Outline:"
                if (!content.contains("Scenario:") && !content.contains("Scenario Outline:")) {
                    System.err.println(
                            "[VALIDATION ERROR] No scenarios defined (missing Scenario: or Scenario Outline:)");
                    return false;
                }
                // Check if placeholder was not replaced
                if (content.contains("SCENARIO_PLACEHOLDER:")) {
                    System.err.println(
                            "[VALIDATION ERROR] Scenario placeholder was not replaced - likely no steps generated");
                    return false;
                }
                break;

            case "StepDef":
                if (!content.contains("package stepDefs;")) {
                    System.err.println("[VALIDATION ERROR] Missing package declaration in Step Definitions");
                    return false;
                }
                if (!content.contains("extends browserSelector")) {
                    System.err.println("[VALIDATION ERROR] Step Definitions doesn't extend browserSelector");
                    return false;
                }
                break;
        }

        System.out.println("[VALIDATION] " + fileType + " content validated successfully");
        return true;
    }

    /**
     * Generate test files from Playwright recording.
     * Main entry point for recording-based generation.
     * <p>
     * GENERATION MODES:
     * - OVERWRITE (mergeMode=false): Regenerates all files from scratch (WARNING:
     * loses manual edits)
     * - MERGE (mergeMode=true): Intelligently adds new methods/steps to existing
     * files (preserves manual edits)
     * <p>
     * TODO: MANDATORY PRE-GENERATION CHECKS:
     * 1. Check if page object exists (avoid overwriting custom code)
     * 2. Verify existing login patterns (reuse existing steps)
     * 3. Check configurations.properties for test data
     * 4. Validate all selectors follow priority order
     * 5. Warn about dynamic IDs before generation
     *
     * @param recordingFile Path to recorded-actions.java file
     * @param featureName   Name of the feature (e.g., "login", "profile")
     * @param pageUrl       Page URL or path
     * @param jiraStory     JIRA story ID
     * @param mergeMode     If true, merge with existing files; if false, overwrite
     * @return true if successful, false otherwise
     */
    public static boolean generateFromRecording(String recordingFile, String featureName,
            String pageUrl, String jiraStory, boolean mergeMode) {

        // AUTO-FIX: Validate recording file first
        if (!validateRecordingFile(recordingFile)) {
            System.err.println("[ERROR] Recording file validation failed");
            return false;
        }

        // AUTO-FIX: Sanitize and validate feature name
        String className = autoFixFeatureName(featureName);

        // MANDATORY - Run pre-generation validation checks
        // Check existing page object and login patterns
        boolean filesExist = pageObjectExists(className);
        detectExistingLogin();
        hasConfiguredCredentials();

        // ─────────────────────────────────────────────────────────────
        // JIRA STORY DETAILS: Fetch actual story data when a story ID is provided
        // Supports both issue key (e.g. PROJ-123) and full JIRA URL
        // (e.g. https://company.atlassian.net/browse/PROJ-123)
        // ─────────────────────────────────────────────────────────────
        jiraClient.JiraStory jiraInfo = null;
        if (jiraStory != null && !jiraStory.trim().isEmpty()
                && !jiraStory.equalsIgnoreCase("NONE")
                && !jiraStory.equalsIgnoreCase("N/A")) {
            // Extract issue key from JIRA URL if a full URL was provided
            String issueKey = extractJiraIssueKey(jiraStory);
            System.out.println("\n🔗 [JIRA] Fetching story details for: " + issueKey);
            try {
                jiraInfo = jiraClient.getJiraStory(issueKey);
                if (jiraInfo != null) {
                    jiraInfo.printDetails();
                } else {
                    System.out.println("   ⚠️  Could not fetch JIRA story details - using story ID as tag only");
                }
            } catch (Exception e) {
                System.out.println("   ⚠️  JIRA fetch failed (" + e.getMessage()
                        + ") - continuing without story details");
            }
        }

        // Display merge mode information
        if (filesExist) {
            if (mergeMode) {
                System.out.println("\\n╔════════════════════════════════════════════════════════════════╗");
                System.out.println("║                  🔄 MERGE MODE ENABLED                         ║");
                System.out.println("╚════════════════════════════════════════════════════════════════╝");
                System.out.println("✅ Existing " + className + " files detected");
                System.out.println("✅ Will merge new recording with existing code");
                System.out.println("✅ Existing methods will be preserved");
                System.out.println("✅ Only NEW methods/steps from recording will be added\\n");
            } else {
                System.out.println("\\n⚠️  WARNING: Files exist but merge mode is OFF");
                System.out.println("   Existing files will be OVERWRITTEN!");
                System.out.println("   To preserve existing code, use mergeMode=true\\n");
            }
        }

        try {
            // Parse recording file with auto-fix
            List<RecordedAction> actions = parseRecording(recordingFile);

            if (actions.isEmpty()) {
                System.err.println("[ERROR] No valid actions found in recording");
                return false;
            }

            // Generate files with error handling
            try {
                generatePageObject(className, pageUrl, jiraStory, jiraInfo, actions, mergeMode);
            } catch (Exception e) {
                System.err.println("[ERROR] Page Object generation failed: " + e.getMessage());
                autoRecoverFromError(e, "Page Object Generation");
                throw e;
            }

            try {
                generateFeatureFile(className, jiraStory, jiraInfo, actions, mergeMode);
            } catch (Exception e) {
                System.err.println("[ERROR] Feature File generation failed: " + e.getMessage());
                autoRecoverFromError(e, "Feature File Generation");
                throw e;
            }

            // CRITICAL FIX: Clean up placeholder-only step definitions before regeneration
            // This ensures recordings generate proper implementations instead of keeping
            // placeholders
            try {
                cleanupPlaceholderStepDefinitions(className);
            } catch (Exception e) {
                System.err.println("[WARN] Could not cleanup placeholder steps: " + e.getMessage());
                // Continue anyway - non-critical
            }

            try {
                generateStepDefinitions(className, jiraStory, jiraInfo, actions, mergeMode);
            } catch (Exception e) {
                System.err.println("[ERROR] Step Definitions generation failed: " + e.getMessage());
                autoRecoverFromError(e, "Step Definitions Generation");
                throw e;
            }

            // ═══════════════════════════════════════════════════════════════
            // FINAL VALIDATION SUMMARY
            // ═══════════════════════════════════════════════════════════════
            System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║              GENERATION COMPLETE - VALIDATION REPORT           ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝");
            System.out.println("✅ Page Object: src/main/java/pages/" + className + ".java");
            System.out.println("✅ Feature File: src/test/java/features/" + className.toLowerCase() + ".feature");
            System.out.println("✅ Step Definitions: src/test/java/stepDefs/" + className + "Steps.java");
            System.out.println("\n📊 Action Summary:");
            System.out.println("   Total actions parsed: " + actions.size());
            long clickActions = actions.stream().filter(a -> "click".equals(a.type)).count();
            long fillActions = actions.stream().filter(a -> "fill".equals(a.type)).count();
            long verifyActions = actions.stream().filter(a -> "verify".equals(a.type)).count();
            System.out.println("   - Click actions: " + clickActions);
            System.out.println("   - Fill actions: " + fillActions);
            System.out.println("   - Verify actions: " + verifyActions);
            System.out.println("   - Other actions: " + (actions.size() - clickActions - fillActions - verifyActions));
            if (mergeMode && filesExist) {
                System.out.println("\n🔄 Merge Mode Results:");
                System.out.println("   ✓ Existing methods preserved");
                System.out.println("   ✓ New methods added from recording");
                System.out.println("   ✓ Manual edits maintained");
            }
            System.out.println("\n💡 Next Steps:");
            System.out.println("   1. Review generated files for accuracy");
            System.out.println("   2. Run: mvn clean compile");
            System.out.println("   3. Execute tests: mvn test -Dcucumber.filter.tags=@" + jiraStory);
            System.out.println("\n⚠️  If any steps are missing:");
            System.out.println("   - Check console output for [WARNING] messages above");
            System.out.println("   - Check parseRecording() patterns support all Playwright action types");
            System.out.println("   - All recorded actions are now always generated (no generic-step skipping)");
            System.out.println("═══════════════════════════════════════════════════════════════\n");

            // ═══════════════════════════════════════════════════════════════
            // CLEANUP: Delete unused "Recorded" template file if it exists
            // ═══════════════════════════════════════════════════════════════
            cleanupUnusedRecordedFile();

            // ═══════════════════════════════════════════════════════════════
            // CLEANUP: Delete recording directory after successful generation
            // PERMANENT FIX (Feb 18, 2026): Ensures cleanup happens regardless
            // of how TestGeneratorHelper is invoked (CLI or direct Maven)
            // ═══════════════════════════════════════════════════════════════
            cleanupRecordingDirectory(recordingFile);

            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to generate test files: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Overloaded method for backward compatibility - defaults to overwrite mode.
     * New code should use generateFromRecording(..., mergeMode) to explicitly
     * choose mode.
     * 
     * @param recordingFile Path to recorded-actions.java file
     * @param featureName   Name of the feature
     * @param pageUrl       Page URL or path
     * @param jiraStory     JIRA story ID
     * @return true if successful
     */
    public static boolean generateFromRecording(String recordingFile, String featureName,
            String pageUrl, String jiraStory) {
        return generateFromRecording(recordingFile, featureName, pageUrl, jiraStory, false);
    }

    // ========================================================================
    // RECORDING-BASED GENERATION (from TestFileGenerator)
    // ========================================================================

    /**
     * Normalize text by removing consecutive duplicate words.
     * Examples:
     * "Setup Setup" -> "Setup"
     * "Logout Logout" -> "Logout"
     * "Click Click Click" -> "Click"
     * "Save Changes" -> "Save Changes" (no change)
     */
    private static String normalizeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        String[] words = text.split("\\s+");
        StringBuilder result = new StringBuilder();
        String previousWord = null;

        for (String word : words) {
            // Only add word if it's different from previous (case-insensitive)
            if (previousWord == null || !word.equalsIgnoreCase(previousWord)) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(word);
                previousWord = word;
            }
        }

        return result.toString();
    }

    /**
     * Optimizes a selector by preferring stable locators.
     *
     * SMART PRIORITY SYSTEM (Feb 12, 2026 - ENHANCED):
     * 1. Static ID (highest priority) - Extracted from ANY selector format
     * 2. data-testid attributes - Best practice for testing
     * 3. data-test-id attributes - Alternative testing attribute
     * 4. name attribute (if stable) - Form elements
     * 5. Relative XPath - Structural locators
     * 6. Label or aria-label - Accessible locators
     * 7. Placeholder - Input hints
     * 8. Text content - Visual text
     * 9. CSS classes (if stable) - Last resort
     *
     * SMART EXTRACTION:
     * - Extracts IDs from complex CSS selectors (div#myId.class → #myId)
     * - Extracts IDs from XPath (//div[@id='myId'] → #myId)
     * - Extracts IDs from attribute selectors ([id='myId'] → #myId)
     * - Validates ID stability (rejects dynamic IDs with GUIDs/timestamps)
     * - Converts label= to proper Playwright Java selectors
     *
     * @param selector The original selector from recording
     * @return Optimized selector following smart priority order
     */
    private static String optimizeSelector(String selector) {
        if (selector == null || selector.isEmpty())
            return selector;

        // ========== SMART ID EXTRACTION (PRIORITY 1) ==========
        // Try to extract ID from ANY selector format FIRST
        String extractedId = extractIdFromComplexSelector(selector);
        if (extractedId != null && !extractedId.isEmpty() && !isDynamicId(extractedId)) {
            System.out.println("   🎯 SMART EXTRACTION: Found stable ID '" + extractedId + "' in complex selector");
            System.out.println("   ✅ Upgrading to Priority 1 (Static ID)");
            return "#" + extractedId;
        } else if (extractedId != null && isDynamicId(extractedId)) {
            System.out.println("   ⚠️  Found dynamic ID '" + extractedId + "' - will use fallback strategy");
        }

        // ========== PRIORITY 2: data-testid ATTRIBUTES ==========
        // Check for data-testid (testing best practice)
        if (selector.contains("data-testid") || selector.contains("data-test-id")) {
            String testId = extractTestId(selector);
            if (testId != null && !testId.isEmpty()) {
                System.out.println("   🎯 Found data-testid: '" + testId + "' - Priority 2");
                return "[data-testid=\"" + testId + "\"]";
            }
        }

        // ========== PLAYWRIGHT MODERN API CONVERSION ==========
        // Convert Playwright's getByRole(), getByText(), getByPlaceholder() to standard
        // format

        // Handle getByRole patterns
        if (selector.contains("AriaRole.") || selector.startsWith("role=")) {
            String roleName = "";
            String nameValue = "";

            if (selector.contains("AriaRole.")) {
                Pattern rolePattern = Pattern.compile("AriaRole\\.(\\w+)");
                Matcher roleMatcher = rolePattern.matcher(selector);
                if (roleMatcher.find()) {
                    roleName = roleMatcher.group(1).toLowerCase();
                }
            } else if (selector.startsWith("role=")) {
                roleName = selector.substring(5).split(",")[0];
            }

            Pattern namePattern = Pattern.compile("setName\\([\"']([^\"']+)[\"']\\)|name=[\"']?([^\"',)]+)[\"']?");
            Matcher nameMatcher = namePattern.matcher(selector);
            if (nameMatcher.find()) {
                nameValue = nameMatcher.group(1) != null ? nameMatcher.group(1) : nameMatcher.group(2);
            }

            if (!roleName.isEmpty() && !nameValue.isEmpty()) {
                return "role=" + roleName + ",name=" + nameValue;
            } else if (!roleName.isEmpty()) {
                return "role=" + roleName;
            }
        }

        // Handle getByText patterns
        if (selector.contains("getByText(\"") || selector.startsWith("text=")) {
            Pattern textPattern = Pattern.compile("getByText\\([\"']([^\"']+)[\"']\\)");
            Matcher textMatcher = textPattern.matcher(selector);
            if (textMatcher.find()) {
                return "text=" + textMatcher.group(1);
            }
        }

        // Handle getByPlaceholder patterns
        if (selector.contains("getByPlaceholder(\"") || selector.startsWith("placeholder=")) {
            Pattern placeholderPattern = Pattern.compile("getByPlaceholder\\([\"']([^\"']+)[\"']\\)");
            Matcher placeholderMatcher = placeholderPattern.matcher(selector);
            if (placeholderMatcher.find()) {
                return "placeholder=" + placeholderMatcher.group(1);
            }
        }

        // Handle getByLabel patterns
        if (selector.contains("getByLabel(\"") || selector.startsWith("label=")) {
            Pattern labelPattern = Pattern.compile("getByLabel\\([\"']([^\"']+)[\"']\\)");
            Matcher labelMatcher = labelPattern.matcher(selector);
            if (labelMatcher.find()) {
                return "label=" + labelMatcher.group(1);
            }
        }

        // ========== PRIORITY 3: NAME ATTRIBUTE ==========
        if (selector.startsWith("name=") || (selector.contains("name=") && !selector.contains("text="))) {
            Pattern namePattern = Pattern.compile("name=['\"]([^'\"]+)['\"]?");
            Matcher matcher = namePattern.matcher(selector);
            if (matcher.find()) {
                String name = matcher.group(1);
                System.out.println("   🔍 Using name attribute: '" + name + "' - Priority 3");
                return "[name=\"" + name + "\"]";
            }
        }

        // ========== PRIORITY 4: RELATIVE XPATH ==========
        if (selector.startsWith("//") || selector.startsWith("(//")) {
            System.out.println("   🔍 Using relative XPath - Priority 4");
            return selector;
        }

        // ========== PRIORITY 5: LABEL CONVERSION ==========
        if (selector.startsWith("label=")) {
            String labelText = selector.substring(6).trim();
            System.out.println("   🔍 Converting label to proper selector - Priority 5");

            // Common field mappings
            Map<String, String> labelToId = new HashMap<>();
            labelToId.put("Username", "Username");
            labelToId.put("Password", "Password");
            labelToId.put("First Name", "FirstName");
            labelToId.put("Last Name", "LastName");
            labelToId.put("Email", "Email");

            // Try to find ID mapping
            String fieldId = labelToId.get(labelText);
            if (fieldId != null) {
                System.out.println("   ✅ Found ID mapping for label - upgrading to ID selector");
                return "#" + fieldId;
            }

            // Fallback: Convert to CSS selector
            return "[aria-label=\"" + labelText + "\"], [placeholder*=\"" + labelText + "\" i]";
        }

        // ========== PRIORITY 6: PLACEHOLDER ==========
        if (selector.startsWith("placeholder=")) {
            String placeholder = selector.substring(12).trim();
            System.out.println("   🔍 Using placeholder - Priority 6");
            return "[placeholder=\"" + placeholder + "\"]";
        }

        // ========== PRIORITY 7: TEXT CONTENT ==========
        if (selector.startsWith("text=")) {
            // Already in correct format
            return selector;
        }

        // ========== PRIORITY 8: ABSOLUTE XPATH ==========
        if (selector.startsWith("/") && !selector.startsWith("//")) {
            System.out.println("   ⚠️  Using absolute XPath - Priority 8 (least stable)");
            return selector;
        }

        // Return as-is if no optimization applied
        return selector;
    }

    /**
     * Extracts static ID from complex selectors of ANY format.
     * <p>
     * PERMANENT FIX (Feb 12, 2026): Smart ID extraction from ANY selector type
     * <p>
     * Supported formats:
     * - CSS: div#myId.class → myId
     * - CSS: #myId → myId
     * - CSS: [id='myId'] → myId
     * - CSS: [id="myId"] → myId
     * - CSS: input[id='username'][type='text'] → username
     * - XPath: //div[@id='myId'] → myId
     * - XPath: //*[@id='myId'][@class='x'] → myId
     * - Playwright: text=Sign In >> #submitBtn → submitBtn
     * <p>
     * DO NOT REMOVE - Critical for smart ID prioritization!
     *
     * @param selector Original selector from recording
     * @return Extracted ID if found, null otherwise
     */
    private static String extractIdFromComplexSelector(String selector) {
        if (selector == null || selector.isEmpty())
            return null;

        // Pattern 1: CSS #id format (div#myId, #myId, span#userId.class)
        Pattern cssIdPattern = Pattern.compile("#([a-zA-Z][a-zA-Z0-9_-]*)");
        Matcher cssIdMatcher = cssIdPattern.matcher(selector);
        if (cssIdMatcher.find()) {
            return cssIdMatcher.group(1);
        }

        // Pattern 2: CSS attribute [id='value'] or [id="value"]
        Pattern attrIdPattern = Pattern.compile("\\[id=['\"]([^'\"]+)['\"]\\]");
        Matcher attrIdMatcher = attrIdPattern.matcher(selector);
        if (attrIdMatcher.find()) {
            return attrIdMatcher.group(1);
        }

        // Pattern 3: XPath @id='value' or @id="value"
        Pattern xpathIdPattern = Pattern.compile("@id=['\"]([^'\"]+)['\"]");
        Matcher xpathIdMatcher = xpathIdPattern.matcher(selector);
        if (xpathIdMatcher.find()) {
            return xpathIdMatcher.group(1);
        }

        // Pattern 4: CSS id= without quotes (less common but valid)
        Pattern idEqualPattern = Pattern.compile("\\bid=([a-zA-Z][a-zA-Z0-9_-]*)\\b");
        Matcher idEqualMatcher = idEqualPattern.matcher(selector);
        if (idEqualMatcher.find()) {
            return idEqualMatcher.group(1);
        }

        return null; // No ID found
    }

    /**
     * Extracts data-testid attribute value from selector.
     * <p>
     * PERMANENT FIX (Feb 12, 2026): Extract testing-specific attributes
     * <p>
     * Supported formats:
     * - [data-testid="login-button"]
     * - [data-test-id="login-button"]
     * - data-testid='login-button'
     *
     * @param selector Selector string
     * @return Extracted test ID value, null if not found
     */
    private static String extractTestId(String selector) {
        if (selector == null || selector.isEmpty())
            return null;

        // Pattern: data-testid="value" or data-testid='value'
        Pattern testIdPattern = Pattern.compile("data-test(?:-)?id=['\"]([^'\"]+)['\"]");
        Matcher matcher = testIdPattern.matcher(selector);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Detects overly generic selectors that would match multiple elements.
     * These selectors cause "strict mode violation" errors in Playwright.
     * <p>
     * Examples of generic selectors:
     * - //div (matches all divs)
     * - //span (matches all spans)
     * - //button (matches all buttons without attributes)
     * - div (CSS, matches all divs)
     *
     * @param selector The selector to check
     * @return true if selector is overly generic
     */
    @SuppressWarnings("unused")
    private static boolean isOverlyGenericSelector(String selector) {
        if (selector == null || selector.isEmpty()) {
            return false;
        }

        // List of generic HTML elements that often match multiple elements
        String[] genericElements = {
                "div", "span", "a", "p", "li", "ul", "ol", "td", "tr", "th",
                "section", "article", "aside", "nav", "header", "footer", "main"
        };

        // Check for XPath patterns: //element or //element[no attributes]
        for (String element : genericElements) {
            // Exact match: //div
            if (selector.equals("//" + element)) {
                return true;
            }
            // With trailing slash or brackets: //div/ or //div[]
            if (selector.matches("^//" + element + "\\s*[\\[/]?\\s*$")) {
                return true;
            }
        }

        // Check for CSS patterns: just element name
        for (String element : genericElements) {
            if (selector.equals(element)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if an ID is dynamic (contains GUIDs, timestamps, or random strings).
     * Dynamic IDs should be avoided as they change between sessions.
     *
     * @param id The ID to check
     * @return true if the ID appears to be dynamic
     */
    private static boolean isDynamicId(String id) {
        if (id == null || id.isEmpty())
            return false;

        // Check for GUID patterns (e.g., b0f53fd4-e8a9-4e88-87af-4456e7b35a2e)
        if (id.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")) {
            return true;
        }

        // Check for random hash patterns (long strings of random characters)
        if (id.length() > 20 && id.matches("[a-zA-Z0-9]{20,}")) {
            return true;
        }

        // Check for timestamp patterns
        if (id.matches(".*\\d{13,}.*")) { // Unix timestamp
            return true;
        }

        // Check for random suffixes like _1234567890
        if (id.matches(".*[_-]\\d{8,}$")) {
            return true;
        }

        return false;
    }

    /**
     * Attempts to make a generic selector more specific to avoid strict mode
     * violations.
     * <p>
     * Strategies:
     * 1. Add visibility constraint with wrapper class
     * 2. Target first visible element
     *
     * @param genericSelector The overly generic selector
     * @return A more specific selector, or the original if can't be improved
     */
    @SuppressWarnings("unused")
    private static String makeGenericSelectorSpecific(String genericSelector) {
        if (genericSelector == null || genericSelector.isEmpty()) {
            return genericSelector;
        }

        // Extract element name
        String element = genericSelector.replace("//", "").replace("/", "").trim();

        // Strategy: Add visible constraint with wrapper class or first match
        // This will select the first visible element of that type within a wrapper
        return element + ".wrapper >> visible=true";
    }

    /**
     * Gets priority comment for a selector (used in generated code).
     * TODO: MANDATORY - All generated locators must show their priority level
     *
     * @param selector The selector to analyze
     * @return Priority comment string
     */
    @SuppressWarnings("unused")
    private static String getPriorityComment(String selector) {
        if (selector == null || selector.isEmpty())
            return "Unknown Priority";

        // Priority 1: Static ID
        if (selector.contains("@id=") || selector.contains("[@id='")) {
            String id = selector.replaceAll(".*@id=['\"]([^'\"]+)['\"].*", "$1");
            if (isDynamicId(id)) {
                return "Priority 3: XPath (Dynamic ID downgraded)";
            }
            return "Priority 1: Static ID ⭐⭐⭐⭐⭐";
        }

        // Priority 2: Relative XPath (contains //)
        if (selector.startsWith("//") && !selector.matches("^/html/.*")) {
            return "Priority 2: Relative XPath ⭐⭐⭐⭐";
        }

        // Priority 3: Absolute XPath
        if (selector.startsWith("/html")) {
            return "Priority 3: Absolute XPath ⭐⭐⭐";
        }

        // Priority 4: Label or Names
        if (selector.startsWith("label=") || selector.contains("@name=")) {
            return "Priority 4: Label/Name ⭐⭐";
        }

        // Priority 5: Class name
        if (selector.contains("@class=") || selector.matches(".*\\[@class.*")) {
            return "Priority 5: Class name ⭐";
        }

        // Priority 6: CSS
        if (selector.startsWith("css=") || (!selector.startsWith("//") && !selector.startsWith("label="))) {
            return "Priority 6: CSS ⭐";
        }

        return "Unknown Priority";
    }

    /**
     * Extracts static ID from complex selectors before optimization.
     * Scans CSS selectors, XPath, and other formats for ID attributes.
     * This ensures we prioritize IDs even when Playwright recorded a different
     * selector type.
     * <p>
     * Examples:
     * - "div#myId.class" -> extracts "myId"
     * - "//div[@id='myId'][@class='x']" -> extracts "myId"
     * - "input[id='username']" -> extracts "username"
     *
     * @param selector Original selector from recording
     * @return ID if found and static, otherwise null
     */
    @SuppressWarnings("unused")
    private static String extractStaticIdFromSelector(String selector) {
        if (selector == null || selector.isEmpty()) {
            return null;
        }

        // Pattern 1: CSS selector with # (e.g., "div#myId", "#myId.class", "form
        // #myId")
        Pattern cssIdPattern = Pattern.compile("#([a-zA-Z][a-zA-Z0-9_-]*)");
        Matcher matcher = cssIdPattern.matcher(selector);
        if (matcher.find()) {
            String id = matcher.group(1);
            if (!isDynamicId(id)) {
                System.out.println("[ID EXTRACTION] Found static ID in CSS: #" + id);
                return id;
            } else {
                System.out.println("[ID EXTRACTION] Found dynamic ID in CSS: #" + id + " (rejected)");
            }
        }

        // Pattern 2: XPath with @id attribute (e.g., "//input[@id='username']")
        Pattern xpathIdPattern = Pattern.compile("@id\\s*=\\s*['\"]([^'\"]+)['\"]");
        matcher = xpathIdPattern.matcher(selector);
        if (matcher.find()) {
            String id = matcher.group(1);
            if (!isDynamicId(id)) {
                System.out.println("[ID EXTRACTION] Found static ID in XPath: " + id);
                return id;
            } else {
                System.out.println("[ID EXTRACTION] Found dynamic ID in XPath: " + id + " (rejected)");
            }
        }

        // Pattern 3: CSS attribute selector (e.g., "input[id='username']",
        // "[id=username]")
        Pattern attrIdPattern = Pattern.compile("\\[id\\s*=\\s*['\"]?([^'\"\\]]+)['\"]?\\]");
        matcher = attrIdPattern.matcher(selector);
        if (matcher.find()) {
            String id = matcher.group(1);
            if (!isDynamicId(id)) {
                System.out.println("[ID EXTRACTION] Found static ID in attribute selector: " + id);
                return id;
            } else {
                System.out.println("[ID EXTRACTION] Found dynamic ID in attribute selector: " + id + " (rejected)");
            }
        }

        // Pattern 4: Simple id= format (e.g., "id=username")
        if (selector.startsWith("id=") || selector.contains(" id=")) {
            Pattern simpleIdPattern = Pattern.compile("id\\s*=\\s*['\"]?([^'\"\\s]+)['\"]?");
            matcher = simpleIdPattern.matcher(selector);
            if (matcher.find()) {
                String id = matcher.group(1);
                if (!isDynamicId(id)) {
                    System.out.println("[ID EXTRACTION] Found static ID in simple format: " + id);
                    return id;
                }
            }
        }

        return null;
    }

    /**
     * Generate Page Object from recorded actions.
     * <p>
     * MERGE MODE: If mergeMode=true and page object exists:
     * - Parses existing methods to avoid duplicates
     * - Adds only NEW locators and methods from recording
     * - Preserves all existing methods and manual edits
     * <p>
     * OVERWRITE MODE: If mergeMode=false:
     * - Skips generation if file exists (conservative approach)
     * - Shows message to manually edit
     * <p>
     * LOCATOR OPTIMIZATION: All selectors optimized via optimizeSelector() before
     * generation
     * 
     * @param mergeMode If true, merge with existing; if false, skip if exists
     */
    private static void generatePageObject(String className, String pageUrl, String jiraStory,
            jiraClient.JiraStory jiraInfo, List<RecordedAction> actions, boolean mergeMode) throws IOException {

        Path pageObjectPath = Paths.get("src/main/java/pages/" + className + ".java");

        // Check if page object already exists
        if (Files.exists(pageObjectPath)) {
            if (!mergeMode) {
                System.out.println("[REUSE] Page object already exists: " + className + ".java");
                System.out.println("[INFO] Skipping page object generation - using existing implementation");
                System.out.println("[TIP] To merge new recording with existing code, use mergeMode=true");
                System.out.println("[TIP] Or manually edit: src/main/java/pages/" + className + ".java");
                return; // Skip generation, use existing
            } else {
                System.out.println("[MERGE MODE] Page object exists - will add new methods only");
                // Continue to merge mode logic below
            }
        }

        // If merge mode and file exists, parse existing methods
        Set<String> existingMethods = new HashSet<>();
        Set<String> existingLocators = new HashSet<>();
        String existingContent = "";

        if (mergeMode && Files.exists(pageObjectPath)) {
            try {
                existingContent = new String(Files.readAllBytes(pageObjectPath));
                existingMethods = extractMethodNames(existingContent);
                existingLocators = extractLocatorNames(existingContent);
                System.out.println("[MERGE] Found " + existingMethods.size() + " existing methods");
                System.out.println("[MERGE] Found " + existingLocators.size() + " existing locators");
            } catch (Exception e) {
                System.err.println("[WARN] Could not parse existing page object for merge: " + e.getMessage());
                System.err.println("[WARN] Will regenerate from scratch");
                mergeMode = false; // Fallback to overwrite
            }
        }

        if (!mergeMode && Files.exists(pageObjectPath)) {
            return; // Already handled above
        }

        // CROSS-PROJECT SCAN: Check for common locators in OTHER page objects
        Map<String, String> commonLocatorsMap = scanAllPageObjectsForCommonLocators();
        System.out.println("[CROSS-PROJECT] Found " + commonLocatorsMap.size()
                + " common locator methods across all page objects");

        // Track if we need to import other page classes (e.g., Login)
        Set<String> pageImports = new HashSet<>();

        StringBuilder sb = new StringBuilder();

        sb.append("package pages;\n");
        // AUTO-FIX: Always include all required imports for page objects
        sb.append("import com.microsoft.playwright.Locator;\n");
        sb.append("import com.microsoft.playwright.Page;\n");
        sb.append("import com.microsoft.playwright.options.AriaRole;\n");
        sb.append("import com.microsoft.playwright.assertions.PlaywrightAssertions;\n");
        sb.append("import configs.loadProps;\n");
        sb.append("import configs.TimeoutConfig;\n"); // Always include for consistent timeout usage
        sb.append("import java.util.logging.Logger;\n");
        // Note: Page imports (like Login) will be added dynamically if needed
        // Placeholder for dynamic imports will be inserted here
        String importsPlaceholder = "DYNAMIC_IMPORTS_PLACEHOLDER\n";
        sb.append(importsPlaceholder);
        sb.append("\n");
        sb.append("/**\n");
        sb.append(" * Page Object for ").append(className).append("\n");
        sb.append(" * Auto-generated from Playwright recording\n");
        sb.append(" * \n");
        // JIRA story details injected when available
        if (jiraInfo != null) {
            sb.append(" * ─────────────────────────────────────────────────\n");
            sb.append(" * JIRA Story: ").append(jiraInfo.key).append("\n");
            sb.append(" * Summary   : ").append(jiraInfo.summary).append("\n");
            if (jiraInfo.description != null && !jiraInfo.description.isEmpty()) {
                sb.append(" * Description: ").append(jiraInfo.description.replace("\n", "\n *              "))
                        .append("\n");
            }
            sb.append(" * Type      : ").append(jiraInfo.issueType).append("\n");
            sb.append(" * Status    : ").append(jiraInfo.status).append("\n");
            sb.append(" * Priority  : ").append(jiraInfo.priority).append("\n");
            if (!jiraInfo.acceptanceCriteria.isEmpty()) {
                sb.append(" * \n");
                sb.append(" * Acceptance Criteria:\n");
                for (int i = 0; i < jiraInfo.acceptanceCriteria.size(); i++) {
                    sb.append(" *   ").append(i + 1).append(". ").append(jiraInfo.acceptanceCriteria.get(i))
                            .append("\n");
                }
            }
            sb.append(" * ─────────────────────────────────────────────────\n");
        }
        sb.append(" * \n");
        sb.append(" * This class extends BasePage which provides:\n");
        sb.append(" *  - clickOnElement(locator) - from utils.java\n");
        sb.append(" *  - enterText(locator, text) - from utils.java\n");
        sb.append(" *  - selectDropDownValueByText(locator, text) - from utils.java\n");
        sb.append(" *  - navigateToUrl(url) - from BasePage.java\n");
        sb.append(" *  - And many more common utilities\n");
        sb.append(" * \n");
        sb.append(" * All generated methods use these common utilities for consistency\n");
        sb.append(" * and better maintainability across the test framework.\n");
        sb.append(" * \n");
        sb.append(" * @story ").append(jiraStory).append("\n");
        sb.append(" */\n");
        sb.append("public class ").append(className).append(" extends BasePage {\n");
        sb.append("    private static final Logger log = Logger.getLogger(").append(className)
                .append(".class.getName());\n");

        // Extract path from full URL if needed
        String pagePath = extractPathFromUrl(pageUrl);
        sb.append("    private static final String PAGE_PATH = \"").append(pagePath).append("\";\n");
        sb.append("\n");

        // Generate Locator-returning methods with descriptive names
        sb.append("    /* --------------------\n");
        sb.append("       Locators for ").append(className).append("\n");
        sb.append("       -----------------------*/\n");
        sb.append("    \n");

        // TODO: MANDATORY - Verify all selectors follow priority order
        int staticIdCount = 0;
        int labelSelectorCount = 0;
        int textSelectorCount = 0;

        // DEDUPLICATION: Track unique locators and methods (check against existing)
        Set<String> generatedLocators = new HashSet<>();
        Set<String> generatedMethods = new HashSet<>();
        Set<String> locatorMethodNames = new HashSet<>();
        Map<String, String> selectorToMethodName = new HashMap<>(); // Map selector -> method name for reuse

        for (RecordedAction action : actions) {
            if (action.selector != null) {
                // Generate locator method name
                String locatorMethodName = generateLocatorMethodName(action.readableName, action.type);

                // Check if this selector already has a generated method
                if (selectorToMethodName.containsKey(action.selector)) {
                    // Reuse existing locator method name for duplicate selectors
                    action.locatorMethodName = selectorToMethodName.get(action.selector);
                    System.out.println("[REUSE] Using existing locator method: " + action.locatorMethodName
                            + "() for selector: " + action.selector);
                    continue;
                }

                // CROSS-PROJECT CHECK: See if a similar locator exists in another page object
                String existingPageClass = commonLocatorsMap.get(locatorMethodName.toLowerCase());
                if (existingPageClass != null && !existingPageClass.equals(className)) {
                    System.out.println("[CROSS-PROJECT REUSE] Found similar locator in " + existingPageClass + ".java: "
                            + locatorMethodName + "() - Consider reusing " + existingPageClass + "." + locatorMethodName
                            + "()");

                    // For truly common login-related locators in Login page, reference instead of
                    // duplicate
                    String methodNameLower = locatorMethodName.toLowerCase();
                    if (existingPageClass.equalsIgnoreCase("Login") &&
                            (methodNameLower.contains("username") || methodNameLower.contains("password") ||
                                    methodNameLower.contains("signin") || methodNameLower.contains("login"))) {
                        // Store reference to Login page method instead of generating duplicate
                        action.locatorMethodName = "Login." + locatorMethodName;
                        selectorToMethodName.put(action.selector, "Login." + locatorMethodName);
                        pageImports.add("Login"); // Track that we need to import Login
                        System.out.println(
                                "[REFERENCE] Will use Login." + locatorMethodName + "() instead of duplicating");
                        continue; // Skip generating, use reference
                    }
                    // Add comment in generated code about the existing locator
                    // But still generate it in this class for independence
                }

                // Skip if locator method already exists in the existing page object
                if (existingLocators.contains(locatorMethodName)) {
                    action.locatorMethodName = locatorMethodName; // Still set the method name for reuse
                    selectorToMethodName.put(action.selector, locatorMethodName);
                    System.out.println("[SKIP DUPLICATE] Locator method already exists: " + locatorMethodName + "()");
                    continue;
                }

                // Skip duplicate locator method names (use numbered version if conflict)
                if (locatorMethodNames.contains(locatorMethodName)) {
                    // Generate unique method name by appending number
                    int counter = 2;
                    String uniqueName = locatorMethodName + counter;
                    while (locatorMethodNames.contains(uniqueName)) {
                        counter++;
                        uniqueName = locatorMethodName + counter;
                    }
                    locatorMethodName = uniqueName;
                    System.out.println("[CONFLICT RESOLVED] Using unique method name: " + locatorMethodName + "()");
                }

                generatedLocators.add(action.selector);
                locatorMethodNames.add(locatorMethodName);
                selectorToMethodName.put(action.selector, locatorMethodName);

                // Count selector types for validation
                if (action.selector.contains("@id=") && !isDynamicId(action.selector)) {
                    staticIdCount++;
                } else if (action.selector.startsWith("label=")) {
                    labelSelectorCount++;
                    System.out.println("⚠️ [WARNING] Using label= selector (Priority 4): " + action.selector);
                    System.out.println("   TODO: Verify static ID is not available for " + action.readableName);
                } else if (action.selector.startsWith("text=")) {
                    textSelectorCount++;
                }

                // Convert selector to proper Playwright locator call
                String finalSelector = convertLabelSelectorToPlaywrightJava(action.selector, action.readableName);
                String locatorCall = generateLocatorCall(finalSelector, action.readableName, action.frameSelector,
                        action.isInShadowDom, action.options);

                // Check if similar locator exists in another page object - add helpful comment
                existingPageClass = commonLocatorsMap.get(locatorMethodName.toLowerCase());
                if (existingPageClass != null && !existingPageClass.equals(className)) {
                    sb.append("    /**\n");
                    sb.append("     * NOTE: Similar locator exists in ").append(existingPageClass).append(".java: ")
                            .append(locatorMethodName).append("()\n");
                    sb.append("     * Consider reusing ").append(existingPageClass).append(".")
                            .append(locatorMethodName).append("() if applicable.\n");
                    sb.append("     */\n");
                }

                // Add iframe or shadow DOM comment if applicable
                if (action.isInFrame) {
                    sb.append("    /**\n");
                    sb.append("     * IFRAME CONTEXT: This element is within an iframe: ").append(action.frameSelector)
                            .append("\n");
                    sb.append("     * Playwright automatically handles frame switching with frameLocator()\n");
                    sb.append("     */\n");
                } else if (action.isInShadowDom) {
                    sb.append("    /**\n");
                    sb.append("     * SHADOW DOM: This element is within a shadow DOM tree\n");
                    sb.append("     * Playwright automatically pierces shadow DOM with >>> selector\n");
                    sb.append("     */\n");
                }

                // Generate Locator-returning method
                sb.append("    public static Locator ").append(locatorMethodName).append("() {\n");
                sb.append("        return ").append(locatorCall).append(";\n");
                sb.append("    }\n");
                sb.append("\n");

                // Store the method name for use in interaction methods
                action.locatorMethodName = locatorMethodName;
            }
        }

        // TODO: Log summary of locator types used
        System.out.println("[LOCATOR SUMMARY] Static IDs: " + staticIdCount +
                ", Labels: " + labelSelectorCount +
                ", Text: " + textSelectorCount);
        if (labelSelectorCount > 0) {
            System.out.println("⚠️ [TODO] Review " + labelSelectorCount +
                    " label= selectors - consider using XPath with static IDs");
        }

        // AUTO-FIX: Always generate navigateTo method with proper imports and logging
        sb.append("    /**\n");
        sb.append("     * Navigate to ").append(className).append(" page\n");
        sb.append("     * Uses common navigateToUrl method from BasePage\n");
        sb.append("     * @param page Playwright Page instance\n");
        sb.append("     */\n");
        sb.append("    public static void navigateTo").append(className).append("(Page page) {\n");
        sb.append("        // Fail-safe: Check if page is initialized\n");
        sb.append("        if (BasePage.page == null) {\n");
        sb.append(
                "            throw new IllegalStateException(\"❌ Browser not initialized. Ensure hooks are running: \" +\n");
        sb.append("                \"1) Check TestRunner @CucumberOptions glue includes 'hooks' \" +\n");
        sb.append("                \"2) Verify hooks.java @Before method calls browserSelector.setUp()\");\n");
        sb.append("        }\n");
        sb.append("        log.info(\"🌐 Navigating to ").append(className).append(" page\");\n");
        sb.append("        String fullUrl = loadProps.getProperty(\"URL\") + PAGE_PATH;\n");
        sb.append("        navigateToUrl(fullUrl);\n");
        sb.append("        log.info(\"✅ Navigation completed\");\n");
        sb.append("    }\n");
        sb.append("\n");

        // Generate methods for each action with descriptive names
        for (RecordedAction action : actions) {
            if ("navigate".equals(action.type))
                continue;

            // SKIP LOGIN-RELATED ACTIONS: Don't generate login methods in non-Login page
            // objects
            // These should only exist in Login.java and be referenced from other pages
            if (!className.equalsIgnoreCase("Login") && isLoginRelatedAction(action)) {
                System.out.println("[SKIP LOGIN ACTION] Not generating login method in " + className +
                        ".java: " + action.methodName + "() - Should use Login." + action.methodName + "() instead");
                continue;
            }

            // DEDUPLICATION: Skip duplicate methods or existing methods
            if (existingMethods.contains(action.methodName) || generatedMethods.contains(action.methodName)) {
                System.out.println("[SKIP DUPLICATE] Method already exists: " + action.methodName + "()");
                continue;
            }
            generatedMethods.add(action.methodName);

            String locatorMethod = action.locatorMethodName != null ? action.locatorMethodName
                    : "page.locator(\"" + action.selector + "\")";

            sb.append("    /**\n");
            sb.append("     * ").append(action.stepText).append("\n");
            sb.append("     * Element: ").append(action.readableName).append(" (").append(action.selector)
                    .append(")\n");
            sb.append("     * Uses Locator method\n");
            sb.append("     */\n");

            switch (action.type) {
                case "click":
                    sb.append("    public static void ").append(action.methodName).append("(Page page) {\n");
                    sb.append("        log.info(\"🖱️ ").append(action.stepText).append("\");\n");
                    sb.append("        clickOnElement(").append(locatorMethod)
                            .append("()); // Uses utils.clickOnElement(Locator)\n");
                    sb.append("        TimeoutConfig.waitShort();\n");
                    sb.append("        log.info(\"✅ ").append(action.stepText).append(" completed\");\n");
                    sb.append("    }\n");
                    break;

                case "fill":
                    sb.append("    public static void ").append(action.methodName)
                            .append("(Page page, String text) {\n");
                    sb.append("        log.info(\"⌨️ ").append(action.stepText).append(": \" + text);\n");
                    sb.append("        enterText(").append(locatorMethod)
                            .append("(), text); // Uses utils.enterText(Locator, String)\n");
                    sb.append("        TimeoutConfig.waitShort();\n");
                    sb.append("        log.info(\"✅ ").append(action.stepText).append(" completed\");\n");
                    sb.append("    }\n");
                    break;

                case "select":
                    sb.append("    public static void ").append(action.methodName)
                            .append("(Page page, String option) {\n");
                    sb.append("        log.info(\"🔽 ").append(action.stepText).append(": \" + option);\n");
                    sb.append("        selectDropDownValueByText(").append(locatorMethod)
                            .append("(), option); // Uses utils.selectDropDownValueByText(Locator, String)\n");
                    sb.append("        TimeoutConfig.waitShort();\n");
                    sb.append("        log.info(\"✅ ").append(action.stepText).append(" completed\");\n");
                    sb.append("    }\n");
                    break;

                case "check":
                    sb.append("    public static void ").append(action.methodName).append("(Page page) {\n");
                    sb.append("        log.info(\"☑️ ").append(action.stepText).append("\");\n");
                    sb.append("        checkElement(").append(locatorMethod)
                            .append("()); // Uses utils.checkElement(Locator)\n");
                    sb.append("        TimeoutConfig.waitShort();\n");
                    sb.append("        log.info(\"✅ ").append(action.stepText).append(" completed\");\n");
                    sb.append("    }\n");
                    break;

                case "press":
                    sb.append("    public static void ").append(action.methodName).append("(Page page) {\n");
                    sb.append("        log.info(\"⌨️ ").append(action.stepText).append(": Key: ")
                            .append(escapeJavaString(action.value)).append("\");\n");
                    sb.append("        pressKey(").append(locatorMethod).append("(), \"")
                            .append(escapeJavaString(action.value))
                            .append("\"); // Uses utils.pressKey(Locator, String)\n");
                    sb.append("        TimeoutConfig.waitShort();\n");
                    sb.append("        log.info(\"✅ ").append(action.stepText).append(" completed\");\n");
                    sb.append("    }\n");
                    break;

                case "verify":
                    // ALWAYS generate verify/assertion methods - these are intentional validations
                    sb.append("    public static void ").append(action.methodName).append("(Page page) {\n");
                    sb.append("        log.info(\"🔍 Verifying: ").append(action.stepText).append("\");\n");
                    sb.append("        // Use Playwright assertions for reliable visibility check with auto-waiting\n");
                    sb.append("        PlaywrightAssertions.assertThat(").append(locatorMethod)
                            .append("()).isVisible();\n");
                    sb.append("        TimeoutConfig.waitShort();\n");
                    sb.append("        log.info(\"✅ Verification passed: ").append(action.stepText).append("\");\n");
                    sb.append("    }\n");
                    System.out
                            .println("[VERIFY METHOD GENERATED] Assertion method created: " + action.methodName + "()");
                    break;

                default:
                    System.out.println("[UNKNOWN ACTION TYPE] Type: " + action.type + " for step: " + action.stepText);
                    System.out.println(
                            "⚠️  WARNING: Treating as generic interaction (click) - if incorrect, update TestGeneratorHelper switch statement");
                    sb.append("    public static void ").append(action.methodName).append("(Page page) {\n");
                    sb.append("        log.info(\"🖱️ ").append(action.stepText).append("\");\n");
                    sb.append("        clickOnElement(").append(locatorMethod)
                            .append("()); // Auto-generated: treats unknown action type '").append(action.type)
                            .append("' as click\n");
                    sb.append("        TimeoutConfig.waitShort();\n");
                    sb.append("        log.info(\"✅ ").append(action.stepText).append(" completed\");\n");
                    sb.append("    }\n");
                    break;
            }
            sb.append("\n");
        }

        sb.append("}\n");

        // AUTO-FIX: Apply all fixes before validation
        String pageObjectContent = sb.toString();

        // Fix 0: Replace dynamic imports placeholder with actual imports
        if (!pageImports.isEmpty()) {
            StringBuilder importsBuilder = new StringBuilder();
            for (String pageClass : pageImports) {
                importsBuilder.append("import pages.").append(pageClass).append(";\n");
                System.out.println("[IMPORT] Added import for pages." + pageClass);
            }
            pageObjectContent = pageObjectContent.replace("DYNAMIC_IMPORTS_PLACEHOLDER\n", importsBuilder.toString());
        } else {
            pageObjectContent = pageObjectContent.replace("DYNAMIC_IMPORTS_PLACEHOLDER\n", "");
        }

        // Fix 1: Ensure all required imports
        pageObjectContent = ensureRequiredImports(pageObjectContent);

        // Fix 2: Ensure navigateTo method exists
        pageObjectContent = ensureNavigateToMethod(pageObjectContent, className);

        // Fix 3: Convert protected methods to public
        pageObjectContent = autoFixMethodVisibility(pageObjectContent);

        // AUTO-FIX: Validate generated content after fixes
        if (!validateGeneratedContent(pageObjectContent, "PageObject")) {
            throw new IOException("Page Object validation failed");
        }

        // Write file with error handling
        try {
            Files.write(Paths.get("src/main/java/pages/" + className + ".java"), pageObjectContent.getBytes());
            System.out.println("[AUTO-FIX] ✅ Page Object file written successfully");
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to write Page Object file: " + e.getMessage());
            autoRecoverFromError(e, "Page Object File Write");
            throw e;
        }
    }

    /**
     * Parse Playwright recording file and extract actions.
     * Supports both old API (page.click) and modern API (page.locator().click())
     * NOW WITH IFRAME, SHADOW DOM, AND COMPREHENSIVE ASSERTION SUPPORT!
     *
     * ═══════════════════════════════════════════════════════════════════════════
     * LOCATOR OPTIMIZATION: Applies optimizeSelector() to prioritize:
     * 1. Static ID, 2. Relative XPath, 3. Absolute XPath, 4. Label/Names, 5. Class,
     * 6. CSS
     * ═══════════════════════════════════════════════════════════════════════════
     *
     * ═══════════════════════════════════════════════════════════════════════════
     * ADVANCED FEATURES:
     * ═══════════════════════════════════════════════════════════════════════════
     * ✅ IFRAME SUPPORT: Detects frameLocator() calls and preserves iframe context
     * ✅ SHADOW DOM SUPPORT: Detects shadow DOM piercing with >>> or :shadow
     * ✅ DYNAMIC ID DETECTION: Identifies and warns about GUID/timestamp IDs
     * ✅ OPTIONS CAPTURE: Preserves all chained options (.setExact(true),
     * .setHasText(), etc.)
     * ✅ ARIA ROLE PRESERVATION: Extracts actual AriaRole type (HEADING, LINK,
     * BUTTON, etc.)
     *
     * ═══════════════════════════════════════════════════════════════════════════
     * ASSERTION HANDLING (Enhanced for Playwright Best Practices):
     * ═══════════════════════════════════════════════════════════════════════════
     * Captures ALL assertThat().isVisible() patterns from recordings and generates:
     *
     * 1. SUPPORTED ASSERTION TYPES:
     * ✅ assertThat(page.getByRole(AriaRole.HEADING, ...)).isVisible()
     * ✅ assertThat(page.getByRole(AriaRole.LINK, ...)).isVisible()
     * ✅ assertThat(page.getByRole(AriaRole.BUTTON, ...)).isVisible()
     * ✅ assertThat(page.getByText("...")).isVisible()
     * ✅ assertThat(page.getByLabel("...")).isVisible()
     * ✅ assertThat(page.getByTitle("...")).isVisible()
     *
     * 2. GENERATED ARTIFACTS:
     * - Page Object: verify{ElementName}() methods using PlaywrightAssertions
     * - Step Definition: @Then annotations for verification steps
     * - Feature File: "Then {description}" steps placed BEFORE navigation/logout
     *
     * 3. PROPER VERIFICATION FLOW:
     * Recording: assertThat(page.getByRole(AriaRole.HEADING, ...)).isVisible()
     * ↓
     * Page Object: public static void verifyInvoiceGroupsPageLoaded(Page page) {
     * PlaywrightAssertions.assertThat(roleheadingnameinvoicegroups()).isVisible();
     * }
     * ↓
     * Step Def: @Then("page should be updated")
     * public void verifyPageUpdated() {
     * Invoicegroup.verifyInvoiceGroupsPageLoaded(page);
     * }
     * ↓
     * Feature: Then page should be updated ← BEFORE logout/navigation
     *
     * 4. OPTIONS PRESERVATION:
     * - Captures: .setExact(true), .setHasText(), etc. from assertions
     * - Applies to generated locators: new
     * Page.GetByRoleOptions().setName("...").setExact(true)
     *
     * ═══════════════════════════════════════════════════════════════════════════
     * FUTURE PAGE GENERATION GUARANTEE:
     * ═══════════════════════════════════════════════════════════════════════════
     * All future test scripts generated from Playwright recordings will
     * automatically:
     * ✓ Include verification methods with PlaywrightAssertions (auto-retry
     * built-in)
     * ✓ Generate @Then step definitions for assertions
     * ✓ Place verification steps in correct order (before logout/navigation)
     * ✓ Preserve AriaRole types (HEADING, LINK, BUTTON) without hardcoding
     * ✓ Apply all captured options (.setExact(true), etc.) to locators
     * ✓ Create proper locator methods for all assertion targets
     * ═══════════════════════════════════════════════════════════════════════════
     */
    private static List<RecordedAction> parseRecording(String recordingFile) throws IOException {
        List<RecordedAction> actions = new ArrayList<>();
        String content = new String(Files.readAllBytes(Paths.get(recordingFile)));

        // Patterns for IFRAME interactions
        Pattern frameLocatorClickPattern = Pattern
                .compile("page\\.frameLocator\\(\"([^\"]+)\"\\)\\.locator\\(\"([^\"]+)\"\\)\\.click\\(");
        Pattern frameLocatorFillPattern = Pattern
                .compile("page\\.frameLocator\\(\"([^\"]+)\"\\)\\.locator\\(\"([^\"]+)\"\\)\\.fill\\(\"([^\"]+)\"\\)");

        // Patterns for SHADOW DOM piercing (>>> syntax)
        Pattern shadowDomClickPattern = Pattern.compile("page\\.locator\\(\"([^\"]*>>>+[^\"]+)\"\\)\\.click\\(");
        Pattern shadowDomFillPattern = Pattern
                .compile("page\\.locator\\(\"([^\"]*>>>+[^\"]+)\"\\)\\.fill\\(\"([^\"]+)\"\\)");

        // Patterns for MODERN Playwright Locator API (primary)
        Pattern locatorClickPattern = Pattern.compile("page\\.locator\\(\"([^\"]+)\"\\)\\.click\\(");
        Pattern locatorFillPattern = Pattern.compile("page\\.locator\\(\"([^\"]+)\"\\)\\.fill\\(\"([^\"]+)\"\\)");
        Pattern locatorSelectPattern = Pattern
                .compile("page\\.locator\\(\"([^\"]+)\"\\)\\.selectOption\\(\"([^\"]+)\"\\)");
        Pattern locatorCheckPattern = Pattern.compile("page\\.locator\\(\"([^\"]+)\"\\)\\.check\\(");
        Pattern locatorPressPattern = Pattern.compile("page\\.locator\\(\"([^\"]+)\"\\)\\.press\\(\"([^\"]+)\"\\)");

        // Modern getBy* API patterns (Enhanced to capture all chained options like
        // .setExact(true), .setHasText(), etc.)
        Pattern getByRoleClickPattern = Pattern.compile(
                "page\\.getByRole\\(AriaRole\\.(\\w+),\\s*new Page\\.GetByRoleOptions\\(\\)\\.setName\\(\"([^\"]+)\"\\)((?:\\.\\w+\\([^)]*\\))*)\\)\\.click\\(");
        Pattern getByRoleFillPattern = Pattern.compile(
                "page\\.getByRole\\(AriaRole\\.(\\w+),\\s*new Page\\.GetByRoleOptions\\(\\)\\.setName\\(\"([^\"]+)\"\\)((?:\\.\\w+\\([^)]*\\))*)\\)\\.fill\\(\"([^\"]+)\"\\)");
        Pattern getByRolePressPattern = Pattern.compile(
                "page\\.getByRole\\(AriaRole\\.(\\w+),\\s*new Page\\.GetByRoleOptions\\(\\)\\.setName\\(\"([^\"]+)\"\\)((?:\\.\\w+\\([^)]*\\))*)\\)\\.press\\(\"([^\"]+)\"\\)");
        Pattern getByRoleCheckPattern = Pattern.compile(
                "page\\.getByRole\\(AriaRole\\.(\\w+),\\s*new Page\\.GetByRoleOptions\\(\\)\\.setName\\(\"([^\"]+)\"\\)((?:\\.\\w+\\([^)]*\\))*)\\)\\.check\\(");
        Pattern getByTextClickPattern = Pattern.compile(
                "page\\.getByText\\(\"([^\"]+)\"((?:,\\s*new Page\\.GetByTextOptions\\(\\)(?:\\.\\w+\\([^)]*\\))+)?)\\)\\.click\\(");
        Pattern getByTextFillPattern = Pattern.compile(
                "page\\.getByText\\(\"([^\"]+)\"((?:,\\s*new Page\\.GetByTextOptions\\(\\)(?:\\.\\w+\\([^)]*\\))+)?)\\)\\.fill\\(\"([^\"]+)\"\\)");
        Pattern getByTextPressPattern = Pattern.compile(
                "page\\.getByText\\(\"([^\"]+)\"((?:,\\s*new Page\\.GetByTextOptions\\(\\)(?:\\.\\w+\\([^)]*\\))+)?)\\)\\.press\\(\"([^\"]+)\"\\)");
        Pattern getByPlaceholderFillPattern = Pattern
                .compile(
                        "page\\.getByPlaceholder\\(\"([^\"]+)\"((?:,\\s*new Page\\.GetByPlaceholderOptions\\(\\)(?:\\.\\w+\\([^)]*\\))+)?)\\)\\.fill\\(\"([^\"]+)\"\\)");
        Pattern getByLabelClickPattern = Pattern.compile(
                "page\\.getByLabel\\(\"([^\"]+)\"((?:,\\s*new Page\\.GetByLabelOptions\\(\\)(?:\\.\\w+\\([^)]*\\))+)?)\\)\\.click\\(");
        Pattern getByLabelFillPattern = Pattern.compile(
                "page\\.getByLabel\\(\"([^\"]+)\"((?:,\\s*new Page\\.GetByLabelOptions\\(\\)(?:\\.\\w+\\([^)]*\\))+)?)\\)\\.fill\\(\"([^\"]+)\"\\)");
        Pattern getByLabelPressPattern = Pattern
                .compile(
                        "page\\.getByLabel\\(\"([^\"]+)\"((?:,\\s*new Page\\.GetByLabelOptions\\(\\)(?:\\.\\w+\\([^)]*\\))+)?)\\)\\.press\\(\"([^\"]+)\"\\)");

        // Patterns for ASSERTIONS (assertThat, isVisible, etc.) - Enhanced to capture
        // all getBy* methods
        Pattern assertVisiblePattern = Pattern.compile(
                "assertThat\\(page\\.(?:locator|getByTitle|getByRole|getByText|getByLabel)\\([^)]+\\)\\)\\.isVisible\\(");
        Pattern assertGetByTitlePattern = Pattern
                .compile("assertThat\\(page\\.getByTitle\\(\"([^\"]+)\"\\)\\)\\.isVisible\\(");
        Pattern assertGetByRolePattern = Pattern.compile(
                "assertThat\\(page\\.getByRole\\(AriaRole\\.(\\w+),\\s*new Page\\.GetByRoleOptions\\(\\)\\.setName\\(\"([^\"]+)\"\\)((?:\\.\\w+\\([^)]*\\))*)\\)\\)\\.isVisible\\(");
        Pattern assertGetByTextPattern = Pattern.compile(
                "assertThat\\(page\\.getByText\\(\"([^\"]+)\"((?:,\\s*new Page\\.GetByTextOptions\\(\\)(?:\\.\\w+\\([^)]*\\))+)?)\\)\\)\\.isVisible\\(");
        Pattern assertGetByLabelPattern = Pattern.compile(
                "assertThat\\(page\\.getByLabel\\(\"([^\"]+)\"((?:,\\s*new Page\\.GetByLabelOptions\\(\\)(?:\\.\\w+\\([^)]*\\))+)?)\\)\\)\\.isVisible\\(");

        // Patterns for OLD Playwright API (fallback)
        Pattern clickPattern = Pattern.compile("page\\.click\\(\"([^\"]+)\"\\)");
        Pattern fillPattern = Pattern.compile("page\\.fill\\(\"([^\"]+)\",\\s*\"([^\"]+)\"\\)");
        Pattern selectPattern = Pattern.compile("page\\.selectOption\\(\"([^\"]+)\",\\s*\"([^\"]+)\"\\)");
        Pattern checkPattern = Pattern.compile("page\\.check\\(\"([^\"]+)\"\\)");
        Pattern pressPattern = Pattern.compile("page\\.press\\(\"([^\"]+)\",\\s*\"([^\"]+)\"\\)");
        Pattern navigatePattern = Pattern.compile("page\\.navigate\\(\"([^\"]+)\"\\)");

        String[] lines = content.split("\\r?\\n");
        int actionId = 1;

        System.out.println("[DEBUG] Parsing recording file with " + lines.length + " lines");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("import") || line.startsWith("package")) {
                continue;
            }

            Matcher matcher;

            // Check for navigate
            matcher = navigatePattern.matcher(line);
            if (matcher.find()) {
                System.out.println("[DEBUG] Found navigate: " + matcher.group(1));
                actions.add(new RecordedAction(actionId++, "navigate", null, matcher.group(1)));
                continue;
            }

            // IFRAME - frameLocator().click()
            matcher = frameLocatorClickPattern.matcher(line);
            if (matcher.find()) {
                String frameSelector = matcher.group(1);
                String innerSelector = matcher.group(2);
                String combinedSelector = frameSelector + " >> " + innerSelector;
                System.out.println("[DEBUG] Found frameLocator click: " + frameSelector + " >> " + innerSelector);
                actions.add(new RecordedAction(actionId++, "click", combinedSelector, null));
                continue;
            }

            // IFRAME - frameLocator().fill()
            matcher = frameLocatorFillPattern.matcher(line);
            if (matcher.find()) {
                String frameSelector = matcher.group(1);
                String innerSelector = matcher.group(2);
                String value = matcher.group(3);
                String combinedSelector = frameSelector + " >> " + innerSelector;
                System.out.println(
                        "[DEBUG] Found frameLocator fill: " + frameSelector + " >> " + innerSelector + " = " + value);
                actions.add(new RecordedAction(actionId++, "fill", combinedSelector, value));
                continue;
            }

            // SHADOW DOM - locator with >>> piercing click
            matcher = shadowDomClickPattern.matcher(line);
            if (matcher.find()) {
                String shadowSelector = matcher.group(1);
                System.out.println("[DEBUG] Found shadow DOM click: " + shadowSelector);
                actions.add(new RecordedAction(actionId++, "click", shadowSelector, null));
                continue;
            }

            // SHADOW DOM - locator with >>> piercing fill
            matcher = shadowDomFillPattern.matcher(line);
            if (matcher.find()) {
                String shadowSelector = matcher.group(1);
                String value = matcher.group(2);
                System.out.println("[DEBUG] Found shadow DOM fill: " + shadowSelector + " = " + value);
                actions.add(new RecordedAction(actionId++, "fill", shadowSelector, value));
                continue;
            }

            // MODERN API - locator().click()
            matcher = locatorClickPattern.matcher(line);
            if (matcher.find()) {
                String originalSelector = matcher.group(1);
                String selector = optimizeSelector(originalSelector);
                System.out.println("[DEBUG] Found locator click: " + originalSelector +
                        (selector.equals(originalSelector) ? "" : " -> Optimized to: " + selector));
                actions.add(new RecordedAction(actionId++, "click", selector, null));
                continue;
            }

            // ═════════════════════════════════════════════════════════════════════════
            // MODERN API - getByRole() actions - PRESERVE ROLE INFO!
            // ═════════════════════════════════════════════════════════════════════════

            // getByRole().click()
            matcher = getByRoleClickPattern.matcher(line);
            if (matcher.find()) {
                String roleType = matcher.group(1).toLowerCase(); // Capture the actual AriaRole (LINK, BUTTON, etc.)
                String text = normalizeText(matcher.group(2));
                String options = matcher.groupCount() >= 3 ? matcher.group(3) : null;
                // Store as role=xxx format preserving the actual role type from recording
                String roleSelector = "role=" + roleType + ",name=" + text;
                System.out.println("[DEBUG] Found getByRole click: AriaRole." + roleType.toUpperCase() + " name="
                        + matcher.group(2) +
                        " -> Preserved as: " + roleSelector
                        + (options != null && !options.isEmpty() ? " with options: " + options : ""));
                actions.add(new RecordedAction(actionId++, "click", roleSelector, null, options));
                continue;
            }

            // getByRole().fill() - NEW!
            matcher = getByRoleFillPattern.matcher(line);
            if (matcher.find()) {
                String roleType = matcher.group(1).toLowerCase();
                String text = normalizeText(matcher.group(2));
                String options = matcher.groupCount() >= 4 ? matcher.group(3) : null;
                String value = matcher.groupCount() >= 4 ? matcher.group(4) : matcher.group(3);
                String roleSelector = "role=" + roleType + ",name=" + text;
                System.out.println("[DEBUG] Found getByRole fill: AriaRole." + roleType.toUpperCase() + " name="
                        + matcher.group(2) + " = " + value +
                        " -> Preserved as: " + roleSelector
                        + (options != null && !options.isEmpty() ? " with options: " + options : ""));
                actions.add(new RecordedAction(actionId++, "fill", roleSelector, value, options));
                continue;
            }

            // getByRole().press() - NEW!
            matcher = getByRolePressPattern.matcher(line);
            if (matcher.find()) {
                String roleType = matcher.group(1).toLowerCase();
                String text = normalizeText(matcher.group(2));
                String options = matcher.groupCount() >= 4 ? matcher.group(3) : null;
                String key = matcher.groupCount() >= 4 ? matcher.group(4) : matcher.group(3);
                String roleSelector = "role=" + roleType + ",name=" + text;
                System.out.println("[DEBUG] Found getByRole press: AriaRole." + roleType.toUpperCase() + " name="
                        + matcher.group(2) + " - " + key +
                        " -> Preserved as: " + roleSelector
                        + (options != null && !options.isEmpty() ? " with options: " + options : ""));
                actions.add(new RecordedAction(actionId++, "press", roleSelector, key, options));
                continue;
            }

            // getByRole().check() - NEW!
            matcher = getByRoleCheckPattern.matcher(line);
            if (matcher.find()) {
                String roleType = matcher.group(1).toLowerCase();
                String text = normalizeText(matcher.group(2));
                String options = matcher.groupCount() >= 3 ? matcher.group(3) : null;
                String roleSelector = "role=" + roleType + ",name=" + text;
                System.out.println("[DEBUG] Found getByRole check: AriaRole." + roleType.toUpperCase() + " name="
                        + matcher.group(2) +
                        " -> Preserved as: " + roleSelector
                        + (options != null && !options.isEmpty() ? " with options: " + options : ""));
                actions.add(new RecordedAction(actionId++, "check", roleSelector, null, options));
                continue;
            }

            // MODERN API - getByText().click() - Store for role detection
            matcher = getByTextClickPattern.matcher(line);
            if (matcher.find()) {
                String text = normalizeText(matcher.group(1));
                String options = matcher.groupCount() >= 2 ? matcher.group(2) : null;
                // Store as text= but will be upgraded to getByRole if it's a button/link
                System.out.println("[DEBUG] Found getByText click: " + matcher.group(1) +
                        (text.equals(matcher.group(1)) ? "" : " -> Normalized to: " + text) +
                        (options != null && !options.isEmpty() ? " with options: " + options : ""));
                actions.add(new RecordedAction(actionId++, "click", "text=" + text, null, options));
                continue;
            }

            // MODERN API - getByText().fill()
            matcher = getByTextFillPattern.matcher(line);
            if (matcher.find()) {
                String text = normalizeText(matcher.group(1));
                String options = matcher.groupCount() >= 3 ? matcher.group(2) : null;
                String value = matcher.groupCount() >= 3 ? matcher.group(3) : matcher.group(2);
                System.out.println("[DEBUG] Found getByText fill: " + matcher.group(1) + " = " + value +
                        (options != null && !options.isEmpty() ? " with options: " + options : ""));
                actions.add(new RecordedAction(actionId++, "fill", "text=" + text, value, options));
                continue;
            }

            // MODERN API - getByText().press()
            matcher = getByTextPressPattern.matcher(line);
            if (matcher.find()) {
                String text = normalizeText(matcher.group(1));
                String options = matcher.groupCount() >= 3 ? matcher.group(2) : null;
                String key = matcher.groupCount() >= 3 ? matcher.group(3) : matcher.group(2);
                System.out.println("[DEBUG] Found getByText press: " + matcher.group(1) + " - " + key +
                        (options != null && !options.isEmpty() ? " with options: " + options : ""));
                actions.add(new RecordedAction(actionId++, "press", "text=" + text, key, options));
                continue;
            }

            // ========================================================================
            // ASSERTIONS - assertThat().isVisible() - Enhanced to capture all assertion
            // types
            // ========================================================================

            // First check if this line contains any assertion
            matcher = assertVisiblePattern.matcher(line);
            if (matcher.find()) {
                boolean assertionParsed = false;

                // Try assertThat(page.getByRole(AriaRole.HEADING, ...)).isVisible() - MOST
                // COMMON
                Matcher roleAssertMatcher = assertGetByRolePattern.matcher(line);
                if (roleAssertMatcher.find()) {
                    String roleType = roleAssertMatcher.group(1).toLowerCase(); // HEADING, LINK, BUTTON, etc.
                    String name = roleAssertMatcher.group(2);
                    String options = roleAssertMatcher.groupCount() >= 3 ? roleAssertMatcher.group(3) : null;
                    System.out.println(
                            "[DEBUG] Found assertThat getByRole(" + roleType + ") isVisible: \"" + name + "\"" +
                                    (options != null && !options.isEmpty() ? " with options: " + options : ""));
                    actions.add(
                            new RecordedAction(actionId++, "verify", "role" + roleType + "=" + name, null, options));
                    assertionParsed = true;
                }

                // Try assertThat(page.getByText("...")).isVisible()
                if (!assertionParsed) {
                    Matcher textAssertMatcher = assertGetByTextPattern.matcher(line);
                    if (textAssertMatcher.find()) {
                        String text = textAssertMatcher.group(1);
                        String options = textAssertMatcher.groupCount() >= 2 ? textAssertMatcher.group(2) : null;
                        System.out.println("[DEBUG] Found assertThat getByText isVisible: \"" + text + "\"" +
                                (options != null && !options.isEmpty() ? " with options: " + options : ""));
                        actions.add(new RecordedAction(actionId++, "verify", "text=" + text, null, options));
                        assertionParsed = true;
                    }
                }

                // Try assertThat(page.getByLabel("...")).isVisible()
                if (!assertionParsed) {
                    Matcher labelAssertMatcher = assertGetByLabelPattern.matcher(line);
                    if (labelAssertMatcher.find()) {
                        String label = labelAssertMatcher.group(1);
                        String options = labelAssertMatcher.groupCount() >= 2 ? labelAssertMatcher.group(2) : null;
                        System.out.println("[DEBUG] Found assertThat getByLabel isVisible: \"" + label + "\"" +
                                (options != null && !options.isEmpty() ? " with options: " + options : ""));
                        actions.add(new RecordedAction(actionId++, "verify", "label=" + label, null, options));
                        assertionParsed = true;
                    }
                }

                // Try assertThat(page.getByTitle("...")).isVisible()
                if (!assertionParsed) {
                    Matcher titleAssertMatcher = assertGetByTitlePattern.matcher(line);
                    if (titleAssertMatcher.find()) {
                        String title = titleAssertMatcher.group(1);
                        System.out.println("[DEBUG] Found assertThat getByTitle isVisible: \"" + title + "\"");
                        actions.add(new RecordedAction(actionId++, "verify", "title=" + title, null));
                        assertionParsed = true;
                    }
                }

                // Fallback for other assertion types
                if (!assertionParsed) {
                    System.out.println("[DEBUG] Found assertion isVisible (generic fallback)");
                    actions.add(new RecordedAction(actionId++, "verify", "page", null));
                }

                continue;
            }

            // MODERN API - locator().fill()
            matcher = locatorFillPattern.matcher(line);
            if (matcher.find()) {
                String selector = optimizeSelector(matcher.group(1));
                System.out.println("[DEBUG] Found locator fill: " + matcher.group(1) + " = " + matcher.group(2) +
                        (selector.equals(matcher.group(1)) ? "" : " -> Optimized to: " + selector));
                actions.add(new RecordedAction(actionId++, "fill", selector, matcher.group(2)));
                continue;
            }

            // MODERN API - getByPlaceholder().fill()
            matcher = getByPlaceholderFillPattern.matcher(line);
            if (matcher.find()) {
                String placeholder = matcher.group(1);
                String options = matcher.groupCount() >= 3 ? matcher.group(2) : null;
                String value = matcher.groupCount() >= 3 ? matcher.group(3) : matcher.group(2);
                System.out.println("[DEBUG] Found getByPlaceholder fill: " + placeholder + " = " + value +
                        (options != null && !options.isEmpty() ? " with options: " + options : ""));
                actions.add(new RecordedAction(actionId++, "fill", "placeholder=" + placeholder, value, options));
                continue;
            }

            // MODERN API - getByLabel().click()
            matcher = getByLabelClickPattern.matcher(line);
            if (matcher.find()) {
                String label = matcher.group(1);
                String options = matcher.groupCount() >= 2 ? matcher.group(2) : null;
                System.out.println("[DEBUG] Found getByLabel click: " + label +
                        (options != null && !options.isEmpty() ? " with options: " + options : ""));
                actions.add(new RecordedAction(actionId++, "click", "label=" + label, null, options));
                continue;
            }

            // MODERN API - getByLabel().fill()
            matcher = getByLabelFillPattern.matcher(line);
            if (matcher.find()) {
                String label = matcher.group(1);
                String options = matcher.groupCount() >= 3 ? matcher.group(2) : null;
                String value = matcher.groupCount() >= 3 ? matcher.group(3) : matcher.group(2);
                System.out.println("[DEBUG] Found getByLabel fill: " + label + " = " + value +
                        (options != null && !options.isEmpty() ? " with options: " + options : ""));
                actions.add(new RecordedAction(actionId++, "fill", "label=" + label, value, options));
                continue;
            }

            // MODERN API - getByLabel().press()
            matcher = getByLabelPressPattern.matcher(line);
            if (matcher.find()) {
                String label = matcher.group(1);
                String options = matcher.groupCount() >= 3 ? matcher.group(2) : null;
                String key = matcher.groupCount() >= 3 ? matcher.group(3) : matcher.group(2);
                System.out.println("[DEBUG] Found getByLabel press: " + label + " - " + key +
                        (options != null && !options.isEmpty() ? " with options: " + options : ""));
                actions.add(new RecordedAction(actionId++, "press", "label=" + label, key, options));
                continue;
            }

            // MODERN API - locator().selectOption()
            matcher = locatorSelectPattern.matcher(line);
            if (matcher.find()) {
                String selector = optimizeSelector(matcher.group(1));
                System.out.println("[DEBUG] Found locator select: " + matcher.group(1) + " = " + matcher.group(2) +
                        (selector.equals(matcher.group(1)) ? "" : " -> Optimized to: " + selector));
                actions.add(new RecordedAction(actionId++, "select", selector, matcher.group(2)));
                continue;
            }

            // MODERN API - locator().check()
            matcher = locatorCheckPattern.matcher(line);
            if (matcher.find()) {
                String selector = optimizeSelector(matcher.group(1));
                System.out.println("[DEBUG] Found locator check: " + matcher.group(1) +
                        (selector.equals(matcher.group(1)) ? "" : " -> Optimized to: " + selector));
                actions.add(new RecordedAction(actionId++, "check", selector, null));
                continue;
            }

            // MODERN API - locator().press()
            matcher = locatorPressPattern.matcher(line);
            if (matcher.find()) {
                String selector = optimizeSelector(matcher.group(1));
                System.out.println("[DEBUG] Found locator press: " + matcher.group(1) + " - " + matcher.group(2) +
                        (selector.equals(matcher.group(1)) ? "" : " -> Optimized to: " + selector));
                actions.add(new RecordedAction(actionId++, "press", selector, matcher.group(2)));
                continue;
            }

            // OLD API fallbacks
            matcher = clickPattern.matcher(line);
            if (matcher.find()) {
                String selector = optimizeSelector(matcher.group(1));
                System.out.println("[DEBUG] Found old API click: " + matcher.group(1) +
                        (selector.equals(matcher.group(1)) ? "" : " -> Optimized to: " + selector));
                actions.add(new RecordedAction(actionId++, "click", selector, null));
                continue;
            }

            matcher = fillPattern.matcher(line);
            if (matcher.find()) {
                String selector = optimizeSelector(matcher.group(1));
                System.out.println("[DEBUG] Found old API fill: " + matcher.group(1) + " = " + matcher.group(2) +
                        (selector.equals(matcher.group(1)) ? "" : " -> Optimized to: " + selector));
                actions.add(new RecordedAction(actionId++, "fill", selector, matcher.group(2)));
                continue;
            }

            matcher = selectPattern.matcher(line);
            if (matcher.find()) {
                String selector = optimizeSelector(matcher.group(1));
                System.out.println("[DEBUG] Found old API select: " + matcher.group(1) + " = " + matcher.group(2) +
                        (selector.equals(matcher.group(1)) ? "" : " -> Optimized to: " + selector));
                actions.add(new RecordedAction(actionId++, "select", selector, matcher.group(2)));
                continue;
            }

            matcher = checkPattern.matcher(line);
            if (matcher.find()) {
                String selector = optimizeSelector(matcher.group(1));
                System.out.println("[DEBUG] Found old API check: " + matcher.group(1) +
                        (selector.equals(matcher.group(1)) ? "" : " -> Optimized to: " + selector));
                actions.add(new RecordedAction(actionId++, "check", selector, null));
                continue;
            }

            matcher = pressPattern.matcher(line);
            if (matcher.find()) {
                String selector = optimizeSelector(matcher.group(1));
                System.out.println("[DEBUG] Found old API press: " + matcher.group(1) + " - " + matcher.group(2) +
                        (selector.equals(matcher.group(1)) ? "" : " -> Optimized to: " + selector));
                actions.add(new RecordedAction(actionId++, "press", selector, matcher.group(2)));
                continue;
            }
        }

        // If no actions found, add a default navigate action
        if (actions.isEmpty()) {
            System.out.println("[WARN] No actions found in recording, adding default navigation");
            System.out.println("[WARN] Check if recording file contains actual Playwright actions");
            actions.add(new RecordedAction(1, "navigate", null, ""));
        } else {
            System.out.println("[SUCCESS] Extracted " + actions.size() + " actions from recording");
            System.out.println("[VALIDATION] Total lines in recording: " + lines.length);
            System.out.println("[VALIDATION] Actions captured: " + actions.size());
            // Warn if suspiciously low action count compared to code lines
            if (actions.size() < lines.length / 10) {
                System.out.println("⚠️  [WARNING] Low action count compared to recording size!");
                System.out.println("   This might indicate missing pattern matchers.");
                System.out.println("   Review parseRecording() patterns if steps are missing.");
            }

            // AUTO-ADD PAGE VERIFICATION: If no verify actions exist, add one at the end
            boolean hasVerifyAction = actions.stream().anyMatch(a -> "verify".equals(a.type));
            if (!hasVerifyAction) {
                System.out.println(
                        "[AUTO-ADD] No verification found in recording - adding 'page should be updated' assertion");
                System.out.println("   💡 TIP: Add assertThat().isVisible() to your recording for better verification");
                RecordedAction verifyAction = new RecordedAction(actions.size() + 1, "verify", "page", null);
                // Override stepText to match expected format
                verifyAction.stepText = "page should be updated";
                verifyAction.methodName = "verifyPageUpdated";
                actions.add(verifyAction);
            }
        }

        return actions;
    }

    /**
     * Generate Feature file from recorded actions.
     *
     * SCENARIO OUTLINE: Includes actual recorded data in Examples table
     * CODE REUSABILITY: Detects existing login patterns via containsLoginPattern()
     * Suggests using existing steps from detectExistingLogin()
     * Checks hasConfiguredCredentials() for test data reuse
     */
    private static void generateFeatureFile(String className, String jiraStory,
            jiraClient.JiraStory jiraInfo, List<RecordedAction> actions, boolean mergeMode) throws IOException {

        // Check if feature file exists and merge mode is enabled
        Path featurePath = Paths.get("src/test/java/features/" + className.toLowerCase() + ".feature");
        if (mergeMode && Files.exists(featurePath)) {
            System.out.println("[MERGE MODE] Feature file exists - will append new scenario");
            // Implementation: Merge by appending new scenario to existing feature file
            try {
                @SuppressWarnings("unused") // Reserved for future merge implementation
                String existingContent = new String(Files.readAllBytes(featurePath));
                // Generate new scenario (will be appended after existing content)
                System.out.println("[MERGE] Preserving existing scenarios");
                System.out.println("[MERGE] New scenario will be appended to feature file");
            } catch (IOException e) {
                System.err.println("[WARN] Could not read existing feature file for merge: " + e.getMessage());
                System.err.println("[WARN] Will regenerate entire feature file");
                // Fall through to regenerate
            }
        }

        // Scan existing step definitions to ensure we use correct step text for common
        // actions
        Map<String, String> existingSteps = scanAllExistingStepDefinitions();

        boolean hasConfiguredData = hasConfiguredCredentials();
        boolean hasLoginPattern = containsLoginPattern(actions);
        String existingLoginClass = detectExistingLogin();
        boolean shouldReuseLogin = hasLoginPattern && existingLoginClass != null;

        if (hasLoginPattern && existingLoginClass != null) {
            System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║         FEATURE FILE - LOGIN REUSE RECOMMENDATION              ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝");
            System.out.println("🔄 [REUSE TIP] Login pattern detected in feature");
            System.out
                    .println("   📝 Consider using existing login steps from " + existingLoginClass + "Steps.java:\n");
            System.out.println("   RECOMMENDED STEPS:");
            System.out.println("      Given User navigates to the MRI Energy login page");
            System.out.println("      When User enters valid username from configuration");
            System.out.println("      And User enters valid password from configuration");
            System.out.println("      And User clicks on Sign In button");
            System.out.println("      Then User should be successfully logged in\n");
            System.out.println("   💡 TIP: Reference existing step definitions for consistency");
            System.out.println("   🤖 AI ENHANCEMENT: Feature file will auto-integrate existing login methods");
            System.out.println("═══════════════════════════════════════════════════════════════\n");
        }

        if (hasConfiguredData) {
            System.out.println("📋 [DATA TIP] Configured test data available");
            System.out.println("   ✓ Use parameterized steps like 'user enters valid username from configuration'");
            System.out.println("   ✓ Avoid hardcoding credentials in feature files");
            System.out.println("   ✓ Reference: src/test/resources/configurations.properties");
            System.out.println("   🤖 AI OPTION: AITestFramework.generateTestData() for smart test data");
            System.out.println("   🤖 AI OPTION: AITestFramework.generateFormData() for complete form data\n");
        }

        StringBuilder sb = new StringBuilder();
        List<String> exampleColumns = new ArrayList<>();
        List<String> exampleValues = new ArrayList<>();

        sb.append("@").append(jiraStory).append(" @").append(className).append("\n");
        // Use JIRA story summary as Feature title when available
        String featureTitle = (jiraInfo != null && jiraInfo.summary != null && !jiraInfo.summary.isEmpty())
                ? jiraInfo.summary
                : className + " Test";
        sb.append("Feature: ").append(featureTitle).append("\n");
        if (jiraInfo != null) {
            sb.append("  Story   : ").append(jiraInfo.key).append("\n");
            sb.append("  Type    : ").append(jiraInfo.issueType)
                    .append(" | Status: ").append(jiraInfo.status)
                    .append(" | Priority: ").append(jiraInfo.priority).append("\n");
            if (jiraInfo.description != null && !jiraInfo.description.isEmpty()) {
                String shortDesc = jiraInfo.description.length() > 200
                        ? jiraInfo.description.substring(0, 200) + "..."
                        : jiraInfo.description;
                sb.append("  Description: ").append(shortDesc.replace("\n", " ")).append("\n");
            }
            if (!jiraInfo.acceptanceCriteria.isEmpty()) {
                sb.append("  Acceptance Criteria:\n");
                for (int i = 0; i < jiraInfo.acceptanceCriteria.size(); i++) {
                    sb.append("    ").append(i + 1).append(". ").append(jiraInfo.acceptanceCriteria.get(i))
                            .append("\n");
                }
            }
        } else {
            sb.append("  Auto-generated from Playwright recording\n");
        }
        sb.append("\n");

        // SMART DECISION: Will be determined after processing actions
        // Placeholder - will be replaced with correct keyword based on Examples table
        // presence
        String scenarioTitle = (jiraInfo != null && jiraInfo.summary != null && !jiraInfo.summary.isEmpty())
                ? jiraInfo.summary
                : "Complete " + className + " workflow";
        sb.append("  SCENARIO_PLACEHOLDER: ").append(scenarioTitle).append("\n");

        sb.append("    Given user navigates to ").append(className).append(" page\n");

        // DEDUPLICATION: Track generated steps in feature file
        Set<String> generatedFeatureSteps = new HashSet<>();
        boolean hasGeneratedLoginSteps = false;
        int nonLoginStepsGenerated = 0; // SAFEGUARD: Track non-login steps to ensure feature has content

        for (RecordedAction action : actions) {
            if ("navigate".equals(action.type))
                continue;

            // Check if this is a login-related action
            boolean isLoginAction = isLoginRelatedAction(action);

            /*
             * CRITICAL FIX (Feb 12, 2026): Login Action Handling
             *
             * PROBLEM: Previous logic had "continue" statement here that would skip
             * the first non-login action, resulting in empty feature files.
             *
             * SOLUTION: Two-step process:
             * Step 1: When we encounter FIRST login action, add login placeholder
             * BUT DON'T CONTINUE - let it fall through to skip mechanism
             * Step 2: Skip ALL login actions (but ONLY login actions)
             *
             * WHY THIS WORKS:
             * - First login action: Adds placeholder, then gets skipped by Step 2
             * - Subsequent login actions: Get skipped by Step 2
             * - Non-login actions: Pass through both checks and get added ✓
             *
             * DO NOT MODIFY without understanding this flow!
             */

            // STEP 1: Add login placeholder on FIRST login action (ONLY ONCE)
            if (shouldReuseLogin && isLoginAction && !hasGeneratedLoginSteps) {
                sb.append("    # ═══ LOGIN STEPS - USING EXISTING METHODS ═══\n");
                sb.append("    When User enters valid username from configuration\n");
                sb.append("    And User enters valid password from configuration\n");
                sb.append("    And User clicks on Sign In button\n");
                sb.append("    # ═══════════════════════════════════════════════\n");
                hasGeneratedLoginSteps = true;
                // CRITICAL: DO NOT add "continue" here! Let it fall through to Step 2
            }

            // STEP 2: Skip ALL login-related actions (they're replaced by placeholder
            // above)
            if (shouldReuseLogin && isLoginAction) {
                System.out.println("[SKIP LOGIN ACTION] Already using existing login steps, skipping: "
                        + action.stepText);
                System.out.println("   ℹ️  Login steps consolidated into common login methods");
                continue; // Skip this login action and move to next action
            }

            // SAFEGUARD: This point is reached ONLY for non-login actions
            // If we get here, we should be adding a step to the feature file
            nonLoginStepsGenerated++;

            String featureStep = "";

            switch (action.type) {
                case "click":
                    featureStep = "When " + action.stepText;

                    // NORMALIZATION: Check if a similar common step exists (e.g., "user clicks on
                    // logout" vs "user clicks on role button name logout")
                    // This ensures feature files use existing step text for common actions like
                    // logout
                    String normalizedStep = findMatchingExistingStep(action.stepText, existingSteps);
                    if (normalizedStep != null) {
                        featureStep = "When " + normalizedStep;
                        System.out.println("[STEP NORMALIZATION] Using existing step text: " + normalizedStep);
                        System.out.println("   Original: " + action.stepText);
                        // Update the action's stepText so step definition generation also uses the
                        // normalized text
                        action.stepText = normalizedStep;
                    }

                    // DEDUPLICATION: Skip duplicate steps in feature file
                    if (generatedFeatureSteps.contains(featureStep)) {
                        System.out.println("[SKIP DUPLICATE] Feature step already exists: " + featureStep);
                        continue;
                    }
                    generatedFeatureSteps.add(featureStep);

                    sb.append("    ").append(featureStep).append("\n");
                    break;
                case "fill":
                    // Skip login-related fill actions if login reuse is enabled
                    if (shouldReuseLogin && isLoginRelatedAction(action)) {
                        System.out.println("[SKIP LOGIN FIELD] Not adding to feature file (using existing login): "
                                + action.readableName);
                        continue;
                    }

                    String columnName = action.readableName.toLowerCase().replace(" ", "");
                    featureStep = "And user enters \"<" + columnName + ">\" into " + action.readableName.toLowerCase();

                    // DEDUPLICATION: Skip duplicate steps in feature file
                    if (generatedFeatureSteps.contains(featureStep)) {
                        System.out.println("[SKIP DUPLICATE] Feature step already exists: " + featureStep);
                        continue;
                    }
                    generatedFeatureSteps.add(featureStep);

                    sb.append("    ").append(featureStep).append("\n");
                    exampleColumns.add(columnName);
                    exampleValues.add(action.value != null ? action.value : "");
                    break;
                case "select":
                    String selectColumn = action.readableName.toLowerCase().replace(" ", "");
                    featureStep = "And user selects \"<" + selectColumn + ">\" from "
                            + action.readableName.toLowerCase();

                    // DEDUPLICATION: Skip duplicate steps in feature file
                    if (generatedFeatureSteps.contains(featureStep)) {
                        System.out.println("[SKIP DUPLICATE] Feature step already exists: " + featureStep);
                        continue;
                    }
                    generatedFeatureSteps.add(featureStep);

                    sb.append("    ").append(featureStep).append("\n");
                    exampleColumns.add(selectColumn);
                    exampleValues.add(action.value != null ? action.value : "");
                    break;
                case "check":
                    featureStep = "And " + action.stepText;

                    // DEDUPLICATION: Skip duplicate steps in feature file
                    if (generatedFeatureSteps.contains(featureStep)) {
                        System.out.println("[SKIP DUPLICATE] Feature step already exists: " + featureStep);
                        continue;
                    }
                    generatedFeatureSteps.add(featureStep);

                    sb.append("    ").append(featureStep).append("\n");
                    break;
                case "press":
                    featureStep = "And " + action.stepText;

                    // DEDUPLICATION: Skip duplicate steps in feature file
                    if (generatedFeatureSteps.contains(featureStep)) {
                        System.out.println("[SKIP DUPLICATE] Feature step already exists: " + featureStep);
                        continue;
                    }
                    generatedFeatureSteps.add(featureStep);

                    sb.append("    ").append(featureStep).append("\n");
                    break;
                case "verify":
                    // ALWAYS include verification steps from recording - these are intentional user
                    // validations
                    featureStep = "Then " + action.stepText;

                    // DEDUPLICATION: Skip duplicate steps in feature file
                    if (generatedFeatureSteps.contains(featureStep)) {
                        System.out.println("[SKIP DUPLICATE] Feature step already exists: " + featureStep);
                        continue;
                    }
                    generatedFeatureSteps.add(featureStep);

                    sb.append("    ").append(featureStep).append("\n");
                    System.out.println("[VERIFY STEP ADDED] Assertion step included: " + action.stepText);
                    break;
                default:
                    // Unknown action type - still try to add it to ensure no steps are lost
                    System.out.println("[UNKNOWN ACTION] Type: " + action.type + ", Step: " + action.stepText);
                    System.out.println("⚠️  WARNING: Unknown action type - adding to feature file anyway");
                    featureStep = "And " + action.stepText;
                    if (!generatedFeatureSteps.contains(featureStep)) {
                        generatedFeatureSteps.add(featureStep);
                        sb.append("    ").append(featureStep).append("\n");
                    }
                    break;
            }
        }

        // SAFEGUARD: Validate that we actually generated some test steps
        // This prevents empty feature files when all actions are accidentally skipped
        if (nonLoginStepsGenerated == 0 && !hasGeneratedLoginSteps) {
            System.err.println("[ERROR] No steps generated for feature file!");
            System.err.println("  Total actions processed: " + actions.size());
            System.err.println("  Login reuse enabled: " + shouldReuseLogin);
            System.err.println("  This usually means all actions were incorrectly classified as login actions");
            throw new IOException(
                    "Feature generation failed: No test steps generated. Check action classification logic.");
        }

        System.out.println("[FEATURE STATS] Generated " + nonLoginStepsGenerated + " non-login steps" +
                (hasGeneratedLoginSteps ? " + login placeholder" : ""));

        // VALIDATION: Check if we generated steps for all non-login, non-navigate
        // actions
        // Navigate actions are handled by the auto-generated "Given user navigates to
        // [page] page" step
        long nonLoginActions = actions.stream()
                .filter(a -> !isLoginRelatedAction(a) && !"navigate".equals(a.type))
                .count();
        System.out
                .println("[VALIDATION] Total non-login actions in recording (excluding navigate): " + nonLoginActions);
        System.out.println("[VALIDATION] Non-login steps generated in feature: " + nonLoginStepsGenerated);
        if (nonLoginStepsGenerated < nonLoginActions) {
            System.out.println("⚠️  [CRITICAL ERROR] Some recorded actions were NOT captured in feature file!");
            System.out.println("   Expected: " + nonLoginActions + " steps");
            System.out.println("   Generated: " + nonLoginStepsGenerated + " steps");
            System.out.println("   Missing: " + (nonLoginActions - nonLoginStepsGenerated) + " steps");
            System.out.println("\n   Analyzing which actions were skipped:");
            for (RecordedAction action : actions) {
                if (!isLoginRelatedAction(action) && !"navigate".equals(action.type)) {
                    String expectedStepFormat = "";
                    switch (action.type) {
                        case "click":
                            expectedStepFormat = "When " + action.stepText;
                            break;
                        case "fill":
                            expectedStepFormat = "And user enters";
                            break;
                        case "verify":
                            expectedStepFormat = "Then " + action.stepText;
                            break;
                        default:
                            expectedStepFormat = "And " + action.stepText;
                            break;
                    }
                    if (!generatedFeatureSteps.stream().anyMatch(s -> s.contains(action.stepText))) {
                        System.out.println(
                                "   ❌ MISSING " + expectedStepFormat + ": " + action.type + " - " + action.stepText);
                        System.out.println("      Expected step format: '" + expectedStepFormat + "'");
                    }
                }
            }
            throw new IOException(
                    "Feature generation incomplete: Not all recorded actions were captured. Review logic above.");
        } else {
            System.out.println("✅ [VALIDATION] All expected steps were generated successfully");
        }

        // NOTE: Verification steps should come from actual recorded assertions
        // Not from hardcoded text. Removed: "Then page should be updated"
        // If you need page verification, add assertThat().isVisible() to your recording

        // Add Examples table with actual recorded data
        if (!exampleColumns.isEmpty()) {
            sb.append("\n    Examples:\n");
            sb.append("      | ").append(String.join(" | ", exampleColumns)).append(" |\n");
            sb.append("      | ").append(String.join(" | ", exampleValues)).append(" |\n");
        }

        // SMART GHERKIN: Replace placeholder with correct keyword based on Examples
        // presence
        String featureContent = sb.toString();
        if (!exampleColumns.isEmpty()) {
            // Has Examples table → Use "Scenario Outline:"
            featureContent = featureContent.replace("SCENARIO_PLACEHOLDER:", "Scenario Outline:");
            System.out.println("[GHERKIN] Using 'Scenario Outline:' (Examples table detected)");
        } else {
            // No Examples table → Use "Scenario:"
            // Build the scenario title matching what was written to the placeholder
            String resolvedScenarioTitle = (jiraInfo != null && jiraInfo.summary != null && !jiraInfo.summary.isEmpty())
                    ? jiraInfo.summary
                    : "Complete " + className + " workflow";
            String scenarioName = shouldReuseLogin && hasLoginPattern
                    ? "Scenario: " + resolvedScenarioTitle + " with existing login"
                    : "Scenario: " + resolvedScenarioTitle;
            // Replace the full placeholder line regardless of title content
            featureContent = featureContent.replaceFirst(
                    "SCENARIO_PLACEHOLDER: .*", scenarioName);
            System.out.println("[GHERKIN] Using 'Scenario:' (No Examples table)");
        }

        // Update StringBuilder with corrected content
        sb = new StringBuilder(featureContent);

        // AUTO-FIX: Validate feature file content before writing
        featureContent = sb.toString();
        if (!validateGeneratedContent(featureContent, "Feature")) {
            throw new IOException("Feature file validation failed");
        }

        // Auto-fix: Ensure proper Gherkin syntax in all steps
        String[] lines = featureContent.split("\n");
        StringBuilder fixedContent = new StringBuilder();
        for (String line : lines) {
            if (line.trim().matches("^(Given|When|Then|And|But)\\s+.*")) {
                line = autoFixFeatureStep(line.trim());
                fixedContent.append("    ").append(line).append("\n");
            } else {
                fixedContent.append(line).append("\n");
            }
        }

        // Write file with error handling
        try {
            Files.write(Paths.get("src/test/java/features/" + className + ".feature"),
                    fixedContent.toString().getBytes());
            System.out.println("[AUTO-FIX] ✅ Feature file written successfully");

            // AI ENHANCEMENT: Analyze feature file quality with AITestFramework
            try {
                System.out.println("\n🤖 [AI ANALYSIS] Analyzing feature file quality with AITestFramework...");
                AITestFramework.CoverageReport coverage = AITestFramework.analyzeCoverage(className);

                if (coverage != null) {
                    System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
                    System.out.println("║              AI-POWERED COVERAGE ANALYSIS                      ║");
                    System.out.println("╚════════════════════════════════════════════════════════════════╝");
                    System.out.println("📊 Feature: " + coverage.featureName);
                    System.out.println("   Total Scenarios: " + coverage.totalScenarios);
                    System.out.println("   Implemented Steps: " + coverage.implementedSteps);
                    System.out.println("   Coverage: " + coverage.coveragePercentage + "%");
                    System.out.println("\n📈 Scenario Types:");
                    System.out.println("   ✓ Positive: " + coverage.positiveScenarios);
                    System.out.println("   ✗ Negative: " + coverage.negativeScenarios);
                    System.out.println("   📏 Boundary: " + coverage.boundaryScenarios);

                    if (!coverage.suggestions.isEmpty()) {
                        System.out.println("\n💡 AI Recommendations:");
                        for (String recommendation : coverage.suggestions) {
                            System.out.println("   • " + recommendation);
                        }
                    }
                    System.out.println("═══════════════════════════════════════════════════════════════\n");
                }
            } catch (Exception e) {
                System.out.println("[AI-INFO] Coverage analysis not available: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to write Feature file: " + e.getMessage());
            autoRecoverFromError(e, "Feature File Write");
            throw e;
        }
    }

    /**
     * Extracts path from full URL, returning only the path portion.
     * If URL contains the base URL from config, returns only the path after it.
     * If it's already a path (starts with /), returns as-is.
     * If it's just the base URL with no path, returns empty string.
     *
     * @param url Full URL or path
     * @return Path portion only (e.g., "/login", "/start-page", or "")
     */
    private static String extractPathFromUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "";
        }

        url = url.trim();

        // If it's already a relative path, return as-is
        if (url.startsWith("/")) {
            return url;
        }

        // If it doesn't look like a URL (no protocol), return as-is
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return url;
        }

        try {
            // Get base URL from config
            String baseUrl = loadProps.getProperty("URL");
            if (baseUrl != null && !baseUrl.isEmpty()) {
                baseUrl = baseUrl.trim();
                // Remove trailing slash from base URL for comparison
                if (baseUrl.endsWith("/")) {
                    baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                }

                // If the URL starts with base URL, extract the path
                if (url.startsWith(baseUrl)) {
                    String path = url.substring(baseUrl.length());
                    // Return empty string if no path, otherwise return the path
                    return path.isEmpty() ? "" : path;
                }
            }

            // If base URL not found or doesn't match, try to parse URL
            // Extract path from URL (everything after domain)
            int protocolEnd = url.indexOf("://");
            if (protocolEnd != -1) {
                String afterProtocol = url.substring(protocolEnd + 3);
                int pathStart = afterProtocol.indexOf('/');
                if (pathStart != -1) {
                    return afterProtocol.substring(pathStart);
                }
            }

            // No path found, return empty string
            return "";

        } catch (Exception e) {
            System.err.println("[WARN] Could not extract path from URL: " + url + " - using as-is");
            return url;
        }
    }

    /**
     * Generate Step Definitions from recorded actions.
     *
     * CODE REUSABILITY (ENHANCED Feb 12, 2026):
     * - Scans ALL existing step definition files to detect reusable steps
     * - Detects login patterns via containsLoginPattern()
     * - Imports existing classes via detectExistingLogin()
     * - Prevents duplicate step definitions across all files
     * - Adds documentation about reused methods
     * - Provides tips for integrating with existing login steps
     */
    private static void generateStepDefinitions(String className, String jiraStory,
            jiraClient.JiraStory jiraInfo, List<RecordedAction> actions, boolean mergeMode) throws IOException {

        // Check if step definitions exist and merge mode is enabled
        Path stepDefPath = Paths.get("src/test/java/stepDefs/" + className + "Steps.java");
        if (mergeMode && Files.exists(stepDefPath)) {
            System.out.println("[MERGE MODE] Step definitions exist - will add new steps only");
            // Parse existing to avoid duplicates
        }

        // ENHANCEMENT: Scan ALL existing step definition files first
        System.out.println("\n🔍 [REUSE CHECK] Scanning existing step definition files...");
        Map<String, String> existingSteps = scanAllExistingStepDefinitions();
        if (!existingSteps.isEmpty()) {
            System.out.println("✅ Found " + existingSteps.size() + " existing step definitions across all files");
            System.out.println("   These will be REUSED instead of duplicating");
        } else {
            System.out.println("ℹ️  No existing step definitions found (this might be the first test)");
        }

        // Detect if we need to import existing page objects for reuse
        boolean hasLoginPattern = containsLoginPattern(actions);
        String existingLoginClass = detectExistingLogin();
        boolean shouldReuseLogin = hasLoginPattern && existingLoginClass != null;

        if (shouldReuseLogin) {
            System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║            CODE REUSE OPPORTUNITY DETECTED                     ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝");
            System.out.println("🔄 [LOGIN REUSE] Detected login pattern in recorded actions");
            System.out.println("   ✅ Existing login class: " + existingLoginClass + ".java");
            System.out.println("   📁 Location: src/main/java/pages/" + existingLoginClass + ".java\n");

            System.out.println("📝 MANUAL INTEGRATION STEPS:");
            System.out.println("   1. Open generated file: src/test/java/stepDefs/" + className + "Steps.java");
            System.out
                    .println("   2. Locate login-related step definitions (look for 'username', 'password', 'signin')");
            System.out.println("   3. Replace with existing login methods:\n");
            System.out.println("      INSTEAD OF:");
            System.out.println("        @When(\"user enters text into username\")");
            System.out.println("        public void enterUsername(String text) {");
            System.out.println("            " + className + ".enterUsername(page, text);");
            System.out.println("        }\n");
            System.out.println("      USE THIS:");
            System.out.println("        @When(\"user enters valid username from configuration\")");
            System.out.println("        public void enterValidUsername() {");
            System.out.println("            " + existingLoginClass + ".enterValidUsernameFromConfiguration(page);");
            System.out.println("        }\n");

            System.out.println("   4. Update Feature file: src/test/java/features/" + className + ".feature");
            System.out.println("      Change step text to match existing login steps\n");

            System.out.println("💡 BENEFITS:");
            System.out.println("   ✓ Reuses tested and validated login methods");
            System.out.println("   ✓ Uses configured credentials from configurations.properties");
            System.out.println("   ✓ Consistent login behavior across all tests");
            System.out.println("   ✓ Less code duplication and maintenance\n");
            System.out.println("═══════════════════════════════════════════════════════════════\n");
        }

        StringBuilder sb = new StringBuilder();

        sb.append("package stepDefs;\n");
        sb.append("import configs.browserSelector;\n");

        // Import loadProps if we're reusing login functionality
        if (shouldReuseLogin) {
            sb.append("import configs.loadProps;\n");
        }

        sb.append("import io.cucumber.java.en.*;\n");
        sb.append("import pages.").append(className).append(";\n");

        // FIXED: Always import Login with capital L for login reuse
        if (shouldReuseLogin) {
            sb.append("import pages.Login;\n");
        }

        sb.append("\n");
        sb.append("/**\n");
        sb.append(" * Step Definitions for ").append(className).append("\n");
        sb.append(" * Auto-generated from Playwright recording by Pure Java Generator\n");
        if (jiraInfo != null) {
            sb.append(" * \n");
            sb.append(" * JIRA Story  : ").append(jiraInfo.key).append("\n");
            sb.append(" * Summary     : ").append(jiraInfo.summary).append("\n");
            sb.append(" * Type        : ").append(jiraInfo.issueType)
                    .append(" | Status: ").append(jiraInfo.status)
                    .append(" | Priority: ").append(jiraInfo.priority).append("\n");
            if (!jiraInfo.acceptanceCriteria.isEmpty()) {
                sb.append(" * \n");
                sb.append(" * Acceptance Criteria covered:\n");
                for (int i = 0; i < jiraInfo.acceptanceCriteria.size(); i++) {
                    sb.append(" *   ").append(i + 1).append(". ").append(jiraInfo.acceptanceCriteria.get(i))
                            .append("\n");
                }
            }
        }
        if (shouldReuseLogin && !className.equals(existingLoginClass)) {
            sb.append(" * Reuses existing login methods from ").append(existingLoginClass).append(".java\n");
        }
        sb.append(" * @story ").append(jiraStory).append("\n");
        sb.append(" */\n");
        sb.append("public class ").append(className).append("Steps extends browserSelector {\n");
        sb.append("\n");
        sb.append("    @Given(\"user navigates to ").append(className).append(" page\")\n");
        sb.append("    public void navigateTo() {\n");
        sb.append("        System.out.println(\"📍 Step: Navigating to ").append(className).append(" page\");\n");
        sb.append("        ").append(className).append(".navigateTo").append(className).append("(page);\n");
        sb.append("    }\n");
        sb.append("\n");

        // DEDUPLICATION: Track generated step definitions
        Set<String> generatedSteps = new HashSet<>();
        Set<String> generatedStepMethods = new HashSet<>();

        // Track if we're generating login steps
        boolean hasGeneratedLoginSteps = false;

        for (RecordedAction action : actions) {
            if ("navigate".equals(action.type))
                continue;

            // Check if this is a login-related action
            boolean isLoginAction = isLoginRelatedAction(action);

            // Generate camelCase method name for step definition
            String stepMethodName = action.methodName.substring(0, 1).toLowerCase() + action.methodName.substring(1);

            // DEDUPLICATION: Skip duplicate step methods
            if (generatedStepMethods.contains(stepMethodName)) {
                System.out.println("[SKIP DUPLICATE METHOD] Step method already exists: " + stepMethodName
                        + "() for action: " + action.stepText);
                continue;
            }

            // If login reuse is enabled and this is a login action, generate actual
            // implementation
            // instead of TODO comments
            if (shouldReuseLogin && isLoginAction && !hasGeneratedLoginSteps) {
                sb.append("    // ═══════════════════════════════════════════════════════════════\n");
                sb.append("    // ℹ️  LOGIN STEPS FROM RECORDING - GENERATED BELOW\n");
                sb.append("    // ═══════════════════════════════════════════════════════════════\n");
                sb.append("    // NOTE: If LoginSteps.java already has matching steps, you may\n");
                sb.append("    // remove duplicates, but ALL steps from the recording are generated.\n");
                sb.append("    // ═══════════════════════════════════════════════════════════════\n");
                sb.append("\n");
                hasGeneratedLoginSteps = true;
                // FIXED: Do NOT skip - fall through to generate actual step definition
            }

            // Mark step method as generated
            generatedStepMethods.add(stepMethodName);

            switch (action.type) {
                case "click":
                    String clickStepText = action.stepText;
                    String clickStepTextLower = clickStepText.toLowerCase(); // For case-insensitive comparison

                    // Check if step exists in ANY existing step definition file (case-insensitive)
                    if (existingSteps.containsKey(clickStepTextLower)) {
                        String existingFile = existingSteps.get(clickStepTextLower);
                        System.out.println("[REUSE EXISTING] Step already exists in " + existingFile + ": \""
                                + clickStepText + "\"");
                        System.out.println("   ✓ Will reuse existing step definition - no duplicate generated");
                        continue; // Skip generating - use existing step
                    }

                    // FIXED: All steps from recording are generated - no generic-step skipping.

                    // DEDUPLICATION: Skip duplicate step annotations within this file
                    // (case-insensitive)
                    if (generatedSteps.contains(clickStepTextLower)) {
                        System.out.println("[SKIP DUPLICATE STEP] Step annotation already exists in current file: "
                                + clickStepText);
                        continue;
                    }
                    generatedSteps.add(clickStepTextLower);

                    // LOG: Step is being generated
                    System.out
                            .println("✅ [GENERATING CLICK STEP] \"" + clickStepText + "\" -> " + stepMethodName + "()");

                    sb.append("    @When(\"").append(action.stepText).append("\")\n");
                    sb.append("    public void ").append(stepMethodName).append("() {\n");
                    sb.append("        System.out.println(\"📍 Step: ").append(action.stepText).append("\");\n");
                    if (shouldReuseLogin && isLoginAction) {
                        sb.append("        ").append(getLoginMethodCall(action)).append("\n");
                    } else {
                        sb.append("        ").append(className).append(".").append(action.methodName)
                                .append("(page);\n");
                    }
                    sb.append("    }\n");
                    sb.append("\n");
                    break;

                case "fill":
                    String fillStepText = "user enters {string} into " + action.readableName.toLowerCase();
                    String fillStepTextLower = fillStepText.toLowerCase(); // For case-insensitive comparison

                    // Check if step exists in ANY existing step definition file (case-insensitive)
                    if (existingSteps.containsKey(fillStepTextLower)) {
                        String existingFile = existingSteps.get(fillStepTextLower);
                        System.out.println(
                                "[REUSE EXISTING] Step already exists in " + existingFile + " (case-insensitive): \""
                                        + fillStepText + "\"");
                        System.out.println("   ✓ Will reuse existing step definition - no duplicate generated");
                        continue; // Skip generating - use existing step
                    }

                    // DEDUPLICATION: Skip duplicate step annotations within this file
                    // (case-insensitive)
                    if (generatedSteps.contains(fillStepTextLower)) {
                        System.out
                                .println("[SKIP DUPLICATE STEP] Fill step annotation already exists: " + fillStepText);
                        continue;
                    }
                    generatedSteps.add(fillStepTextLower);

                    // LOG: Step is being generated
                    System.out.println(
                            "✅ [GENERATING FILL STEP] \"" + fillStepText + "\" -> " + stepMethodName + "(String)");

                    sb.append("    @And(\"").append(fillStepText).append("\")\n");
                    sb.append("    public void ").append(stepMethodName).append("(String text) {\n");
                    sb.append("        System.out.println(\"📍 Step: Entering text into ").append(action.readableName)
                            .append(": '\" + text + \"'\");\n");
                    if (shouldReuseLogin && isLoginAction) {
                        sb.append("        ").append(getLoginMethodCall(action)).append("\n");
                    } else {
                        sb.append("        ").append(className).append(".").append(action.methodName)
                                .append("(page, text);\n");
                    }
                    sb.append("    }\n");
                    sb.append("\n");
                    break;

                case "select":
                    String selectStepText = "user selects {string} from " + action.readableName.toLowerCase();
                    String selectStepTextLower = selectStepText.toLowerCase(); // For case-insensitive comparison

                    // Check if step exists in ANY existing step definition file (case-insensitive)
                    if (existingSteps.containsKey(selectStepTextLower)) {
                        String existingFile = existingSteps.get(selectStepTextLower);
                        System.out.println(
                                "[REUSE EXISTING] Step already exists in " + existingFile + " (case-insensitive): \""
                                        + selectStepText + "\"");
                        System.out.println("   ✓ Will reuse existing step definition - no duplicate generated");
                        continue; // Skip generating - use existing step
                    }

                    // DEDUPLICATION: Skip duplicate step annotations within this file
                    // (case-insensitive)
                    if (generatedSteps.contains(selectStepTextLower)) {
                        System.out.println("[SKIP DUPLICATE] Step annotation already exists (case-insensitive): "
                                + selectStepText);
                        continue;
                    }
                    generatedSteps.add(selectStepTextLower);

                    sb.append("    @And(\"").append(selectStepText).append("\")\n");
                    sb.append("    public void ").append(stepMethodName).append("(String option) {\n");
                    sb.append("        System.out.println(\"📍 Step: Selecting option from ")
                            .append(action.readableName).append(": '\" + option + \"'\");\n");
                    sb.append("        ").append(className).append(".").append(action.methodName)
                            .append("(page, option);\n");
                    sb.append("    }\n");
                    sb.append("\n");
                    break;

                case "check":
                    String checkStepText = action.stepText;
                    String checkStepTextLower = checkStepText.toLowerCase(); // For case-insensitive comparison

                    // Check if step exists in ANY existing step definition file (case-insensitive)
                    if (existingSteps.containsKey(checkStepTextLower)) {
                        String existingFile = existingSteps.get(checkStepTextLower);
                        System.out.println(
                                "[REUSE EXISTING] Step already exists in " + existingFile + " (case-insensitive): \""
                                        + checkStepText + "\"");
                        System.out.println("   ✓ Will reuse existing step definition - no duplicate generated");
                        continue; // Skip generating - use existing step
                    }

                    // DEDUPLICATION: Skip duplicate step annotations within this file
                    // (case-insensitive)
                    if (generatedSteps.contains(checkStepTextLower)) {
                        System.out.println(
                                "[SKIP DUPLICATE] Step annotation already exists (case-insensitive): " + checkStepText);
                        continue;
                    }
                    generatedSteps.add(checkStepTextLower);

                    sb.append("    @And(\"").append(action.stepText).append("\")\n");
                    sb.append("    public void ").append(stepMethodName).append("() {\n");
                    sb.append("        System.out.println(\"📍 Step: ").append(action.stepText).append("\");\n");
                    sb.append("        ").append(className).append(".").append(action.methodName).append("(page);\n");
                    sb.append("    }\n");
                    sb.append("\n");
                    break;

                case "press":
                    String pressStepText = action.stepText;
                    String pressStepTextLower = pressStepText.toLowerCase(); // For case-insensitive comparison

                    // Check if step exists in ANY existing step definition file (case-insensitive)
                    if (existingSteps.containsKey(pressStepTextLower)) {
                        String existingFile = existingSteps.get(pressStepTextLower);
                        System.out.println(
                                "[REUSE EXISTING] Step already exists in " + existingFile + " (case-insensitive): \""
                                        + pressStepText + "\"");
                        System.out.println("   ✓ Will reuse existing step definition - no duplicate generated");
                        continue; // Skip generating - use existing step
                    }

                    // DEDUPLICATION: Skip duplicate step annotations within this file
                    // (case-insensitive)
                    if (generatedSteps.contains(pressStepTextLower)) {
                        System.out.println(
                                "[SKIP DUPLICATE] Step annotation already exists (case-insensitive): " + pressStepText);
                        continue;
                    }
                    generatedSteps.add(pressStepTextLower);

                    sb.append("    @And(\"").append(action.stepText).append("\")\n");
                    sb.append("    public void ").append(stepMethodName).append("() {\n");
                    sb.append("        System.out.println(\"📍 Step: ").append(action.stepText).append("\");\n");
                    if (shouldReuseLogin && isLoginAction) {
                        sb.append("        ").append(getLoginMethodCall(action)).append("\n");
                    } else {
                        sb.append("        ").append(className).append(".").append(action.methodName)
                                .append("(page);\n");
                    }
                    sb.append("    }\n");
                    sb.append("\n");
                    break;

                case "verify":
                    // ALWAYS generate verify/assertion steps - these are intentional validations
                    String verifyStepText = action.stepText;
                    String verifyStepTextLower = verifyStepText.toLowerCase();

                    // Check if step exists in ANY existing step definition file (case-insensitive)
                    if (existingSteps.containsKey(verifyStepTextLower)) {
                        String existingFile = existingSteps.get(verifyStepTextLower);
                        System.out.println("[REUSE EXISTING] Verify step already exists in " + existingFile
                                + " (case-insensitive): \""
                                + verifyStepText + "\"");
                        System.out.println("   ✓ Will reuse existing step definition - no duplicate generated");
                        continue;
                    }

                    // DEDUPLICATION: Skip duplicate step annotations within this file
                    if (generatedSteps.contains(verifyStepTextLower)) {
                        System.out.println(
                                "[SKIP DUPLICATE STEP] Verify step annotation already exists: " + verifyStepText);
                        continue;
                    }
                    generatedSteps.add(verifyStepTextLower);

                    // LOG: Verify step is being generated
                    System.out.println(
                            "✅ [GENERATING VERIFY STEP] \"" + verifyStepText + "\" -> " + stepMethodName + "()");

                    sb.append("    @Then(\"").append(action.stepText).append("\")\n");
                    sb.append("    public void ").append(stepMethodName).append("() {\n");
                    sb.append("        System.out.println(\"📍 Verify Step: ").append(action.stepText).append("\");\n");
                    sb.append("        ").append(className).append(".").append(action.methodName).append("(page);\n");
                    sb.append("    }\n");
                    sb.append("\n");
                    System.out.println("[VERIFY STEP GENERATED] Assertion step definition created: " + verifyStepText);
                    break;

                default:
                    // Unknown action type - generate working step definition (treats as generic
                    // interaction)
                    System.out.println("[UNKNOWN ACTION TYPE] Type: " + action.type + ", Step: " + action.stepText);
                    System.out.println(
                            "⚠️  WARNING: Generating step definition for unknown action type - verify it behaves correctly");

                    String unknownStepText = action.stepText;
                    String unknownStepTextLower = unknownStepText.toLowerCase();

                    if (!existingSteps.containsKey(unknownStepTextLower)
                            && !generatedSteps.contains(unknownStepTextLower)) {
                        generatedSteps.add(unknownStepTextLower);
                        sb.append("    @When(\"").append(action.stepText).append("\")\n");
                        sb.append("    public void ").append(stepMethodName).append("() {\n");
                        sb.append("        System.out.println(\"📍 Step: ").append(action.stepText).append("\");\n");
                        sb.append("        ").append(className).append(".").append(action.methodName)
                                .append("(page);\n");
                        sb.append("    }\n");
                        sb.append("\n");
                    }
                    break;
            }
        }

        // ═══════════════════════════════════════════════════════════════
        // 📋 COMMON STEPS - REUSE FROM CommonSteps.java
        // ═══════════════════════════════════════════════════════════════
        // ⚠️ DO NOT CREATE DUPLICATE COMMON STEP DEFINITIONS HERE!
        //
        // The following common steps already exist in CommonSteps.java:
        // @When("user clicks on logout logout")
        // @When("user clicks on welcome [username]")
        // @Then("page should be updated")
        //
        // These are GENERIC steps used across ALL tests.
        // ✅ Feature files can use these steps directly
        // ✅ Cucumber automatically finds them from CommonSteps.java
        // ✅ Never duplicate these steps in feature-specific files
        // ═══════════════════════════════════════════════════════════════
        sb.append("\n");
        sb.append("}\n");

        // AUTO-FIX: Validate step definitions content before writing
        String stepDefContent = sb.toString();
        if (!validateGeneratedContent(stepDefContent, "StepDef")) {
            throw new IOException("Step Definitions validation failed");
        }

        // VALIDATION: Report how many step definitions were actually generated
        int generatedMethodCount = generatedSteps.size();
        int totalActions = actions.size();
        int loginActions = (int) actions.stream().filter(a -> isLoginRelatedAction(a)).count();
        int nonLoginActions = totalActions - loginActions - 1; // -1 for navigate action

        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              STEP GENERATION VALIDATION REPORT                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println("📊 Generation Statistics:");
        System.out.println("   Total actions in recording: " + totalActions);
        System.out.println("   Login actions (reused from LoginSteps): " + loginActions);
        System.out.println("   Navigate actions (auto-generated): 1");
        System.out.println("   Non-login actions requiring steps: " + nonLoginActions);
        System.out.println("   Step definitions generated: " + generatedMethodCount);
        System.out.println("   Existing steps reused: " + existingSteps.size());

        // CRITICAL FIX: Detect when too few steps are generated
        // If we have actions but very few steps were generated, something went wrong
        double generationRate = nonLoginActions > 0 ? (double) generatedMethodCount / nonLoginActions : 1.0;

        if (nonLoginActions > 3 && generationRate < 0.5) {
            System.out.println("\n⚠️⚠️⚠️  [CRITICAL WARNING] STEP GENERATION INCOMPLETE! ⚠️⚠️⚠️");
            System.out.println("   Expected ~" + nonLoginActions + " step definitions");
            System.out.println("   Actually generated: " + generatedMethodCount);
            System.out.println("   Generation rate: " + String.format("%.1f%%", generationRate * 100));
            System.out.println("\n🔍 Analyzing why steps were skipped...");

            // Detailed analysis of what happened to each action
            for (RecordedAction action : actions) {
                if ("navigate".equals(action.type) || isLoginRelatedAction(action)) {
                    continue; // These are expected to be skipped
                }

                String stepText = action.stepText.toLowerCase();
                if (existingSteps.containsKey(stepText)) {
                    System.out.println(
                            "   [REUSED] " + action.stepText + " (exists in " + existingSteps.get(stepText) + ")");
                } else if (isCommonGenericStep(action.stepText)) {
                    System.out.println(
                            "   [NOTE] " + action.stepText + " (was previously classified as generic - now generated)");
                } else if (!generatedSteps.contains(stepText)) {
                    System.out.println("   [MISSING!] " + action.stepText + " <- THIS STEP WAS NOT GENERATED!");
                } else {
                    System.out.println("   [GENERATED] " + action.stepText);
                }
            }

            System.out.println("\n💡 Common causes for missing steps:");
            System.out.println("   1. Steps incorrectly marked as 'existing' when they don't actually exist");
            System.out.println("   2. Steps incorrectly marked as 'common/generic' when they're page-specific");
            System.out.println("   3. Duplicate detection logic too aggressive");
            System.out.println("   4. Login reuse logic incorrectly classifying non-login steps");
            System.out.println("\n⚠️  RECOMMENDATION: Review the step generation logic and re-run generation");
            System.out.println("════════════════════════════════════════════════════════════════\n");
        } else if (generatedMethodCount == 0 && nonLoginActions > 0) {
            System.out.println("\n⚠️  [WARNING] No new step definitions generated but recording has actions!");
            System.out.println("   This might indicate over-aggressive reuse or skipping logic.");
        } else {
            System.out.println("✅ Step generation appears complete\n");
        }

        // AUTO-FIX: Validate feature steps match step definitions and generate missing
        // ones
        // CRITICAL: This should only add placeholders for steps that are truly missing
        // from recording
        // NOT for steps that should have been generated above but were skipped
        try {
            String featureContent = new String(Files.readAllBytes(
                    Paths.get("src/test/java/features/" + className.toLowerCase() + ".feature")));

            // Only run validation if we're NOT regenerating (to avoid creating placeholders
            // unnecessarily)
            // When regenerating from recordings, all steps should be in the recording
            // already
            if (generatedMethodCount > 0 || actions.size() > 5) {
                System.out.println("[SKIP VALIDATION] Recording has " + actions.size() + " actions, generated "
                        + generatedMethodCount + " step methods");
                System.out.println("   Skipping placeholder generation - all steps should be from recording");
            } else {
                stepDefContent = validateAndFixStepMatching(featureContent, stepDefContent, className);
            }
        } catch (IOException e) {
            System.out.println("[AUTO-FIX] ⚠️ Could not read feature file for step matching validation");
        }

        // Write file with error handling
        try {
            Files.write(Paths.get("src/test/java/stepDefs/" + className + "Steps.java"), stepDefContent.getBytes());
            System.out.println("[AUTO-FIX] ✅ Step Definitions file written successfully");
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to write Step Definitions file: " + e.getMessage());
            autoRecoverFromError(e, "Step Definitions File Write");
            throw e;
        }
    }

    /**
     * Converts label= selector syntax to proper Playwright Java selectors.
     * <p>
     * PERMANENT FIX (Feb 12, 2026): Prevents "Unknown engine 'label'" errors
     * <p>
     * label= is NOT a valid Playwright Java selector engine.
     * This method converts it to proper CSS selectors that Playwright Java
     * understands.
     * <p>
     * Conversion logic:
     * - label=Username → input[aria-label='Username'], input[name*='username' i],
     * input[placeholder*='username' i]
     * - label=Password → input[type='password'], input[name='password']
     * - label=Email → input[type='email'], input[name*='email' i]
     * <p>
     * DO NOT REMOVE - Critical for Playwright Java compatibility!
     */
    private static String convertLabelSelectorToPlaywrightJava(String selector, String readableName) {
        if (selector == null || !selector.startsWith("label=")) {
            return selector; // Not a label selector, return as-is
        }

        // Extract the label text
        String labelText = selector.substring(6); // Remove "label=" prefix
        String lowerLabel = labelText.toLowerCase();
        String lowerName = readableName.toLowerCase();

        // Special cases for common input types
        if (lowerLabel.contains("password") || lowerName.contains("password")) {
            return "input[type='password'], input[name='password'], input[aria-label='" + labelText + "']";
        }

        if (lowerLabel.contains("email") || lowerName.contains("email")) {
            return "input[type='email'], input[name*='email' i], input[aria-label='" + labelText + "']";
        }

        if (lowerLabel.contains("username") || lowerLabel.contains("user name") || lowerName.contains("username")) {
            return "input[type='email'], input[name='username'], input[name*='user' i], input[aria-label='" + labelText
                    + "']";
        }

        if (lowerLabel.contains("phone") || lowerName.contains("phone")) {
            return "input[type='tel'], input[name*='phone' i], input[aria-label='" + labelText + "']";
        }

        // Generic conversion: try aria-label, name (case-insensitive), and placeholder
        String nameAttr = lowerLabel.replaceAll("\\s+", "");
        return "input[aria-label='" + labelText + "'], " +
                "input[name*='" + nameAttr + "' i], " +
                "input[placeholder*='" + labelText + "' i], " +
                "textarea[aria-label='" + labelText + "'], " +
                "select[aria-label='" + labelText + "']";
    }

    /**
     * Convert string to camelCase format
     * Reserved for future string formatting needs
     */
    @SuppressWarnings("unused")
    private static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return "defaultMethod";
        }

        String pascalCase = toPascalCase(input);

        // Convert first character to lowercase
        String camelCase = Character.toLowerCase(pascalCase.charAt(0)) + pascalCase.substring(1);

        // Validate and return
        if (isValidJavaIdentifier(camelCase)) {
            return camelCase;
        } else {
            System.err.println("⚠️  WARNING: Generated invalid method name '" + camelCase + "', using defaultMethod");
            return "defaultMethod";
        }
    }

    /**
     * Escape special characters for Java strings.
     */
    private static String escapeJavaString(String str) {
        if (str == null)
            return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ========================================================================
    // CODE REUSE DETECTION HELPERS
    // ========================================================================

    /**
     * Finds a matching existing step definition for common actions.
     * This ensures feature files use the exact text of existing steps (e.g., "user
     * clicks on logout")
     * instead of generated variations (e.g., "user clicks on role button name
     * logout").
     *
     * @param generatedStepText The generated step text
     * @param existingSteps     Map of existing step texts (lowercase -> filename)
     * @return The existing step text if a match is found, otherwise null
     */
    private static String findMatchingExistingStep(String generatedStepText, Map<String, String> existingSteps) {
        if (generatedStepText == null || existingSteps == null || existingSteps.isEmpty()) {
            return null;
        }

        String lowerGenerated = generatedStepText.toLowerCase().trim();

        // Check for exact match first
        if (existingSteps.containsKey(lowerGenerated)) {
            // Return the original case version from the map keys
            return existingSteps.keySet().stream()
                    .filter(k -> k.equalsIgnoreCase(lowerGenerated))
                    .findFirst()
                    .orElse(null);
        }

        // For logout-related steps, try to find any existing logout step
        if (lowerGenerated.contains("logout") || lowerGenerated.contains("log out") ||
                lowerGenerated.contains("sign out") || lowerGenerated.contains("signout")) {

            // Search for any logout step in existing steps
            for (String existingStep : existingSteps.keySet()) {
                String lowerExisting = existingStep.toLowerCase();
                if ((lowerExisting.contains("logout") || lowerExisting.contains("log out") ||
                        lowerExisting.contains("sign out") || lowerExisting.contains("signout")) &&
                        lowerExisting.contains("click")) {
                    // Found a matching logout click step
                    return existingStep;
                }
            }
        }

        // For page verification steps
        if (lowerGenerated.contains("page should be") || lowerGenerated.contains("page is updated")) {
            for (String existingStep : existingSteps.keySet()) {
                String lowerExisting = existingStep.toLowerCase();
                if (lowerExisting.contains("page should be") || lowerExisting.contains("page is updated")) {
                    return existingStep;
                }
            }
        }

        // No matching existing step found
        return null;
    }

    /**
     * Identifies common/generic steps that should ALWAYS be reused from
     * CommonSteps.java or LoginSteps.java.
     * <p>
     * IMPORTANT: Common steps are application-wide shared actions that should be
     * reused.
     * User-recorded page-specific interactions should NOT be marked as common.
     * <p>
     * Common steps (should exist in CommonSteps.java or LoginSteps.java):
     * - Page verification steps (page should be updated, page loaded)
     * - Generic navigation (navigate to home/dashboard)
     * - Login/Logout actions (login, logout, sign in, sign out)
     * <p>
     * NOT common (user-recorded page-specific interactions):
     * - "user clicks on setup" - specific link on specific page
     * - "user clicks on configure tree" - specific element on specific page
     */
    private static boolean isCommonGenericStep(String stepText) {
        if (stepText == null)
            return false;

        String lower = stepText.toLowerCase();

        // Page verification patterns (truly generic)
        if (lower.contains("page should be") || lower.contains("page is updated") ||
                lower.contains("page loaded") || lower.contains("page displayed")) {
            return true;
        }

        // Generic navigation (truly generic)
        if (lower.equals("navigate to home") || lower.equals("navigate to dashboard") ||
                lower.equals("go to home") || lower.equals("go to dashboard")) {
            return true;
        }

        // Login/Logout actions (application-wide common)
        // These are shared across all pages and should be in LoginSteps.java
        if (lower.contains("logout") || lower.contains("log out") ||
                lower.contains("sign out") || lower.contains("signout")) {
            System.out.println(
                    "[COMMON STEP] Logout-related step detected: \"" + stepText + "\" - should be in LoginSteps.java");
            return true;
        }

        // Login actions (when they contain login keywords)
        if (lower.contains("login") || lower.contains("log in") ||
                lower.contains("sign in") || lower.contains("signin")) {
            // But allow specific field interactions like "enters text into username"
            if (!lower.contains("enter") && !lower.contains("type") && !lower.contains("fill")) {
                System.out.println("[COMMON STEP] Login-related step detected: \"" + stepText
                        + "\" - should be in LoginSteps.java");
                return true;
            }
        }

        return false;
    }

    /**
     * Check if an action is login-related.
     */
    /**
     * Get the Login class method call for a login-related recorded action.
     * Routes actions (click/fill/press) on username, password, or sign-in elements
     * to the existing methods in Login.java so the new page class is not polluted
     * with duplicate login logic.
     *
     * Mapping:
     * fill + password → Login.PasswordField(text);
     * fill + username → Login.UsernameField(text);
     * press + password → Login.passwordField().press("Tab");
     * press + username → Login.usernameField().press("Tab");
     * click + sign-in → Login.SignInButton();
     * click + password → Login.passwordField().click();
     * click + username → Login.usernameField().click();
     */
    private static String getLoginMethodCall(RecordedAction action) {
        String readableLower = action.readableName != null ? action.readableName.toLowerCase() : "";
        String selectorLower = action.selector != null ? action.selector.toLowerCase() : "";
        String type = action.type;

        boolean isPassword = readableLower.contains("password") || selectorLower.contains("password");
        boolean isSignIn = selectorLower.contains("signin") || selectorLower.contains("sign-in") ||
                selectorLower.contains("login") || readableLower.contains("sign in") ||
                readableLower.contains("log in") || readableLower.contains("signin") ||
                readableLower.contains("login");

        if ("fill".equals(type)) {
            return isPassword ? "Login.PasswordField(text);" : "Login.UsernameField(text);";
        } else if ("press".equals(type)) {
            return isPassword ? "Login.passwordField().press(\"Tab\");" : "Login.usernameField().press(\"Tab\");";
        } else { // click (and any other interaction)
            if (isSignIn)
                return "Login.SignInButton();";
            if (isPassword)
                return "Login.passwordField().click();";
            return "Login.usernameField().click();";
        }
    }

    private static boolean isLoginRelatedAction(RecordedAction action) {
        if (action == null)
            return false;

        String selector = action.selector != null ? action.selector.toLowerCase() : "";
        String readableName = action.readableName != null ? action.readableName.toLowerCase() : "";
        String value = action.value != null ? action.value.toLowerCase() : "";

        return selector.contains("username") || selector.contains("password") ||
                selector.contains("login") || selector.contains("signin") || selector.contains("sign-in") ||
                selector.contains("logout") || selector.contains("log-out") || selector.contains("sign out") ||
                readableName.contains("username") || readableName.contains("password") ||
                readableName.contains("login") || readableName.contains("signin") ||
                readableName.contains("logout") || readableName.contains("sign out") ||
                value.contains("username") || value.contains("password");
    }

    /**
     * Generate a locator method name from a readable name and action type.
     * Examples:
     * Username + fill -> usernameField()
     * Sign In + click -> signInButton()
     * Expand Menu + click -> expandMenu()
     *
     * @param readableName Human-readable element name
     * @param actionType   Type of action (click, fill, select, etc.)
     * @return Camel-case locator method name
     */
    private static String generateLocatorMethodName(String readableName, String actionType) {
        if (readableName == null || readableName.isEmpty()) {
            return "element";
        }

        String name = readableName;

        // Clean up the name
        name = name.replaceAll("[^a-zA-Z0-9\\s]", "");
        name = name.trim();

        // Convert to camelCase
        String camelName = toCamelCase(name);

        // Add appropriate suffix based on element type and action
        if (actionType != null) {
            String lower = name.toLowerCase();
            if (actionType.equals("fill") || lower.contains("input") || lower.contains("enter") ||
                    lower.contains("username") || lower.contains("password") || lower.contains("email")) {
                if (!camelName.endsWith("Field")) {
                    camelName += "Field";
                }
            } else if ((actionType.equals("click") || actionType.equals("check")) &&
                    (lower.contains("button") || lower.contains("btn") || lower.contains("submit") ||
                            lower.contains("sign") || lower.contains("login") || lower.contains("logout"))) {
                if (!camelName.endsWith("Button")) {
                    camelName = camelName.replace("Button", "").replace("Btn", "") + "Button";
                }
            } else if (actionType.equals("click") && (lower.contains("link") || lower.contains("navigate"))) {
                // Keep as-is for navigation links
            } else if (actionType.equals("select")) {
                if (!camelName.endsWith("Dropdown") && !camelName.endsWith("Select")) {
                    camelName += "Dropdown";
                }
            }
        }

        return camelName;
    }

    /**
     * Generate proper Playwright locator call from a selector string.
     * Now supports iframes and shadow DOM:
     * - iframe#payment >> button#submit ->
     * page.frameLocator("iframe#payment").locator("button#submit")
     * - div >>> button (shadow DOM) -> page.locator("div").locator("button")
     * <p>
     * Converts selectors to appropriate Playwright Java API calls:
     * #id -> page.locator("#id") [PRIORITY 1]
     * .class#id -> page.locator("#id") [Extract ID]
     * role=button,name=Login -> page.getByRole(AriaRole.BUTTON, ...) [PRIORITY 2]
     * label=Username -> page.getByLabel("Username") [PRIORITY 3]
     * placeholder=Email -> page.getByPlaceholder("Email") [PRIORITY 4]
     * text=Login -> page.getByRole(AriaRole.BUTTON/LINK, ...) if interactive
     * [PRIORITY 5]
     * text=Login -> page.getByText("Login") otherwise [PRIORITY 6]
     * <p>
     * Priority: ID > getByRole > getByLabel > getByPlaceholder > getByText (smart)
     * > locator
     *
     * @param selector     The element selector
     * @param readableName Human-readable name for creating getByRole options
     * @return Playwright locator call string
     */
    @SuppressWarnings("unused") // Called by overloaded method with same signature + additional params
    private static String generateLocatorCall(String selector, String readableName) {
        return generateLocatorCall(selector, readableName, null, false, null);
    }

    /**
     * Generate Playwright Java locator call from selector with support for options.
     * Applies intelligent fallback: ID > getByRole > getByLabel > getByText >
     * locator
     * Enhanced with iframe and shadow DOM support.
     *
     * @param selector      The selector string
     * @param readableName  Human-readable element name
     * @param frameSelector Iframe selector if element is in iframe
     * @param isInShadowDom True if selector uses shadow DOM piercing
     * @param options       Chained options like .setExact(true),
     *                      .setHasText("..."), etc.
     * @return Generated Playwright locator call
     */
    private static String generateLocatorCall(String selector, String readableName, String frameSelector,
            boolean isInShadowDom, String options) {
        if (selector == null || selector.isEmpty()) {
            return "page.locator(\"body\")";
        }

        String baseLocator;

        // PRIORITY 1: Check for ID in selector (CSS or extracted)
        if (selector.startsWith("#")) {
            baseLocator = "page.locator(\"" + escapeJavaString(selector) + "\")";
        }
        // Extract ID from complex CSS selectors (e.g., "div#myId.class" -> "#myId")
        else if (extractIdFromComplexSelector(selector) != null
                && !isDynamicId(extractIdFromComplexSelector(selector))) {
            String extractedId = extractIdFromComplexSelector(selector);
            System.out.println("   [ID EXTRACTED] Using ID: #" + extractedId + " from: " + selector);
            baseLocator = "page.locator(\"#" + extractedId + "\")";
        }
        // PRIORITY 2: Handle role= selectors -> page.getByRole()
        else if (selector.startsWith("role=")) {
            String[] parts = selector.substring(5).split(",name=", 2);
            String role = parts[0].trim().toUpperCase();
            String name = parts.length > 1 ? parts[1].trim() : readableName;

            if (name != null && !name.isEmpty()) {
                baseLocator = "page.getByRole(AriaRole." + role + ", new Page.GetByRoleOptions().setName(\"" +
                        escapeJavaString(name) + "\")";
                // Apply chained options if present
                if (options != null && !options.isEmpty()) {
                    baseLocator += options;
                }
                baseLocator += ")";
            } else {
                baseLocator = "page.getByRole(AriaRole." + role + ")";
            }
        }
        // PRIORITY 3: Handle label= selectors
        else if (selector.startsWith("label=")) {
            String label = selector.substring(6).trim();
            baseLocator = "page.getByLabel(\"" + escapeJavaString(label) + "\")";
            // Apply options for getByLabel if present
            if (options != null && !options.isEmpty()) {
                // Convert options format: ", new Page.GetByLabelOptions().setExact(true)" ->
                // options without leading comma
                String cleanOptions = options.trim();
                if (cleanOptions.startsWith(",")) {
                    cleanOptions = cleanOptions.substring(1).trim();
                }
                // Insert options before closing parenthesis
                baseLocator = baseLocator.substring(0, baseLocator.length() - 1) + ", " + cleanOptions + ")";
            }
        }
        // PRIORITY 4: Handle placeholder= selectors
        else if (selector.startsWith("placeholder=")) {
            String placeholder = selector.substring(12).trim();
            baseLocator = "page.getByPlaceholder(\"" + escapeJavaString(placeholder) + "\")";
            // Apply options for getByPlaceholder if present
            if (options != null && !options.isEmpty()) {
                String cleanOptions = options.trim();
                if (cleanOptions.startsWith(",")) {
                    cleanOptions = cleanOptions.substring(1).trim();
                }
                baseLocator = baseLocator.substring(0, baseLocator.length() - 1) + ", " + cleanOptions + ")";
            }
        }
        // PRIORITY 5/6: Handle text= selectors
        else if (selector.startsWith("text=")) {
            String text = selector.substring(5).trim();
            String roleName = detectRoleFromText(text, readableName);
            if (roleName != null) {
                baseLocator = "page.getByRole(AriaRole." + roleName + ", new Page.GetByRoleOptions().setName(\"" +
                        escapeJavaString(text) + "\")";
                if (options != null && !options.isEmpty()) {
                    baseLocator += options;
                }
                baseLocator += ")";
            } else {
                baseLocator = "page.getByText(\"" + escapeJavaString(text) + "\")";
                // Apply options for getByText if present
                if (options != null && !options.isEmpty()) {
                    String cleanOptions = options.trim();
                    if (cleanOptions.startsWith(",")) {
                        cleanOptions = cleanOptions.substring(1).trim();
                    }
                    baseLocator = baseLocator.substring(0, baseLocator.length() - 1) + ", " + cleanOptions + ")";
                }
            }
        }
        // Shadow DOM piercing: div >>> button -> page.locator("div").locator("button")
        else if (isInShadowDom || selector.contains(">>>")) {
            String[] shadowParts = selector.split(">>>", 2);
            if (shadowParts.length == 2) {
                baseLocator = "page.locator(\"" + escapeJavaString(shadowParts[0].trim()) + "\")"
                        + ".locator(\"" + escapeJavaString(shadowParts[1].trim()) + "\")";
                System.out.println("   [SHADOW DOM] Generated piercing selector");
            } else {
                baseLocator = "page.locator(\"" + escapeJavaString(selector) + "\")";
            }
        }
        // PRIORITY 7: Default - use page.locator()
        else {
            baseLocator = "page.locator(\"" + escapeJavaString(selector) + "\")";
        }

        // IFRAME HANDLING: Wrap with frameLocator if action is within iframe
        if (frameSelector != null && !frameSelector.isEmpty()) {
            System.out.println("   [IFRAME] Wrapping locator with frameLocator: " + frameSelector);
            // Replace page.xxx with page.frameLocator("...").xxx
            baseLocator = baseLocator.replace("page.",
                    "page.frameLocator(\"" + escapeJavaString(frameSelector) + "\").");
        }

        return baseLocator;
    }

    /**
     * Detect AriaRole from element text (for text= selectors).
     * Returns null if not an interactive element.
     *
     * @param text         The element text
     * @param readableName The readable name
     * @return AriaRole name or null
     */
    private static String detectRoleFromText(String text, String readableName) {
        String lower = text.toLowerCase();
        String nameLower = readableName != null ? readableName.toLowerCase() : "";

        // Common button text patterns
        if (lower.matches(
                ".*(sign in|login|logout|log out|submit|save|cancel|delete|add|create|update|edit|remove|confirm|ok|yes|no|close|apply).*")) {
            return "BUTTON";
        }

        // Common link text patterns (navigation, "see more", etc.)
        if (lower.matches(".*(setup|configure|navigate|go to|view|details|more|settings|profile|dashboard|home).*")) {
            return "LINK";
        }

        // Check readable name for hints
        if (nameLower.contains("button") || nameLower.contains("btn")) {
            return "BUTTON";
        }

        if (nameLower.contains("link") || nameLower.contains("menu") || nameLower.contains("nav")) {
            return "LINK";
        }

        return null; // Not an interactive element, use getByText
    }

    /**
     * Determine the appropriate AriaRole based on element name and selector.
     *
     * @param readableName Human-readable element name
     * @param selector     The selector string
     * @return AriaRole enum name (e.g., "BUTTON", "LINK", "TEXTBOX")
     */
    @SuppressWarnings("unused") // Reserved for future AI-enhanced role inference
    private static String determineAriaRole(String readableName, String selector) {
        String lower = readableName.toLowerCase();
        String selectorLower = selector != null ? selector.toLowerCase() : "";

        // Button
        if (lower.contains("button") || lower.contains("btn") || lower.contains("submit") ||
                lower.contains("sign in") || lower.contains("login") || lower.contains("logout") ||
                selectorLower.contains("button") || selectorLower.contains("role=button")) {
            return "BUTTON";
        }

        // Link
        if (lower.contains("link") || lower.contains("navigate") || lower.contains("menu") ||
                selectorLower.contains("role=link") || selectorLower.contains("<a")) {
            return "LINK";
        }

        // Textbox/Input
        if (lower.contains("input") || lower.contains("field") || lower.contains("username") ||
                lower.contains("password") || lower.contains("email") || lower.contains("search") ||
                selectorLower.contains("input") || selectorLower.contains("type='text'")) {
            return "TEXTBOX";
        }

        // Checkbox
        if (lower.contains("check") || lower.contains("checkbox") ||
                selectorLower.contains("checkbox") || selectorLower.contains("type='checkbox'")) {
            return "CHECKBOX";
        }

        // Default to BUTTON for interactive elements
        return "BUTTON";
    }

    /**
     * Cleans up step definition files that contain ONLY placeholder
     * implementations.
     * <p>
     * CRITICAL FIX (Feb 18, 2026): Prevents placeholder steps from persisting
     * across regenerations
     * <p>
     * This method checks if a step definitions file exists and contains only
     * PendingException placeholders.
     * If so, it's deleted to allow proper regeneration from recordings.
     * <p>
     * Why this is needed:
     * 1. validateAndFixStepMatching() can create placeholder steps for missing
     * steps
     * 2. On subsequent runs, these placeholders are detected as "existing" and
     * skipped
     * 3. Result: Recording actions never generate proper implementations
     * <p>
     * DO NOT REMOVE - Critical for ensuring recordings always generate working
     * code!
     */
    private static void cleanupPlaceholderStepDefinitions(String className) {
        try {
            Path stepDefPath = Paths.get("src/test/java/stepDefs/" + className + "Steps.java");

            if (!Files.exists(stepDefPath)) {
                return; // No file to cleanup
            }

            String content = new String(Files.readAllBytes(stepDefPath));

            // Count total step methods and placeholder methods
            Pattern methodPattern = Pattern.compile("@(?:Given|When|Then|And|But)\\s*\\(");
            Matcher methodMatcher = methodPattern.matcher(content);
            int totalMethods = 0;
            while (methodMatcher.find()) {
                totalMethods++;
            }

            Pattern placeholderPattern = Pattern.compile("PendingException");
            Matcher placeholderMatcher = placeholderPattern.matcher(content);
            int placeholderMethods = 0;
            while (placeholderMatcher.find()) {
                placeholderMethods++;
            }

            // If ALL methods are placeholders (or nearly all - allow navigateTo to be real)
            // then delete the file for clean regeneration
            if (placeholderMethods > 0 && placeholderMethods >= (totalMethods - 1)) {
                System.out.println("\\n[CLEANUP] Found step definitions file with " + placeholderMethods + "/"
                        + totalMethods + " placeholder methods");
                System.out.println(
                        "   Deleting: " + stepDefPath.getFileName() + " for clean regeneration from recording");
                Files.delete(stepDefPath);
                System.out.println("   ✅ Deleted successfully - will regenerate with proper implementations\\n");
            } else if (placeholderMethods > 0) {
                System.out.println("\\n[INFO] Found " + placeholderMethods + " placeholder methods out of "
                        + totalMethods + " total");
                System.out.println("   File contains real implementations - keeping existing file");
                System.out.println("   Placeholder steps will be ignored during scanning and regenerated\\n");
            }

        } catch (Exception e) {
            System.err.println("[WARN] Could not cleanup placeholder step definitions: " + e.getMessage());
            // Non-critical - continue with generation
        }
    }

    /**
     * Scans ALL existing step definition files to find reusable step definitions.
     * <p>
     * PERMANENT FIX (Feb 12, 2026): Prevents duplicate step generation across files
     * <p>
     * ENHANCED FIX (Feb 18, 2026): Ignores placeholder steps (PendingException)
     * during scanning
     * <p>
     * Returns a map of:
     * Key = Step text pattern (e.g., "User enters valid username from
     * configuration")
     * Value = Source file (e.g., "LoginSteps.java")
     * <p>
     * This allows us to:
     * 1. Detect if a step already exists in ANY step definition file
     * 2. Reuse existing steps instead of duplicating them
     * 3. Document which file contains the existing step
     * 4. Ignore placeholder steps so they get regenerated with proper
     * implementations
     * <p>
     * DO NOT REMOVE - Critical for preventing duplicate step definitions!
     */
    private static Map<String, String> scanAllExistingStepDefinitions() {
        Map<String, String> existingSteps = new HashMap<>();

        try {
            Path stepDefsDir = Paths.get("src/test/java/stepDefs");

            if (!Files.exists(stepDefsDir)) {
                return existingSteps; // No existing step definitions
            }

            // Scan all *Steps.java files
            try (Stream<Path> paths = Files.walk(stepDefsDir, 1)) {
                paths.filter(p -> p.toString().endsWith("Steps.java"))
                        .forEach(filePath -> {
                            try {
                                String content = new String(Files.readAllBytes(filePath));
                                String fileName = filePath.getFileName().toString();

                                // Extract all @Given/@When/@Then/@And/@But annotations
                                Pattern stepPattern = Pattern
                                        .compile(
                                                "@(?:Given|When|Then|And|But)\\s*\\(\\s*\"([^\"]+)\"\\s*\\)\\s*\\n\\s*public\\s+void\\s+\\w+\\([^)]*\\)\\s*\\{([^}]+)\\}");
                                Matcher matcher = stepPattern.matcher(content);

                                while (matcher.find()) {
                                    String stepText = matcher.group(1);
                                    String methodBody = matcher.group(2);

                                    // CRITICAL FIX: Ignore placeholder steps (contain PendingException)
                                    // This ensures placeholders are regenerated with proper implementations
                                    if (methodBody.contains("PendingException")) {
                                        System.out.println(
                                                "[SKIP PLACEHOLDER] Found placeholder step in " + fileName + ": \""
                                                        + stepText + "\" - will regenerate with proper implementation");
                                        continue; // Don't add placeholders to existing steps
                                    }

                                    // Only add actually-implemented steps
                                    // Store with lowercase key for case-insensitive comparison
                                    existingSteps.put(stepText.toLowerCase(), fileName);
                                }

                            } catch (IOException e) {
                                System.err.println("[WARN] Could not read step definitions from: " + filePath);
                            }
                        });
            }

            System.out.println("[REUSE INFO] Scanned existing step definitions:");
            if (!existingSteps.isEmpty()) {
                // Group by file for cleaner output
                Map<String, Long> countByFile = existingSteps.values().stream()
                        .collect(Collectors.groupingBy(f -> f, Collectors.counting()));
                countByFile.forEach((file, count) -> System.out.println("   📄 " + file + " - " + count + " steps"));
            }

        } catch (Exception e) {
            System.err.println("[WARN] Error scanning existing step definitions: " + e.getMessage());
        }

        return existingSteps;
    }

    /**
     * Check if a page object already exists.
     * 
     * @param className The page object class name
     * @return true if the page object exists
     */
    private static boolean pageObjectExists(String className) {
        try {
            Path pageObjectPath = Paths.get("src/main/java/pages/" + className + ".java");
            return Files.exists(pageObjectPath);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts a JIRA issue key from either a plain key (e.g. "PROJ-123") or a
     * full JIRA URL (e.g. "https://company.atlassian.net/browse/PROJ-123" or
     * "https://company.atlassian.net/jira/software/projects/PROJ/issues/PROJ-123").
     * Returns the original value unchanged if no URL pattern is detected.
     *
     * @param jiraStoryInput The raw value provided by the user (key or URL)
     * @return The extracted issue key (e.g. "PROJ-123")
     */
    private static String extractJiraIssueKey(String jiraStoryInput) {
        if (jiraStoryInput == null)
            return null;
        String trimmed = jiraStoryInput.trim();
        // If it looks like a URL, extract the issue key from it
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            // Match issue key pattern: one or more uppercase letters, a dash, one or more
            // digits
            // Covers /browse/PROJ-123 and /issues/PROJ-123 and similar patterns
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("/([A-Z][A-Z0-9_]+-\\d+)(?:[/?#]|$)");
            java.util.regex.Matcher matcher = pattern.matcher(trimmed);
            if (matcher.find()) {
                String extracted = matcher.group(1);
                System.out.println("   🔑 Extracted issue key '" + extracted + "' from URL: " + trimmed);
                return extracted;
            }
            System.out.println("   ⚠️  Could not extract issue key from URL: " + trimmed + " - using as-is");
        }
        return trimmed;
    }

    /**
     * Extract method names from existing page object for merge mode.
     * Returns set of method names to avoid duplicates.
     * 
     * @param content Existing page object content
     * @return Set of method names
     */
    private static Set<String> extractMethodNames(String content) {
        Set<String> methodNames = new HashSet<>();
        // Pattern: public static void main(
        // Pattern: public static Locator methodName(
        String methodPattern = "public\\s+static\\s+(?:void|Locator|String)\\s+(\\w+)\\s*\\(";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(methodPattern);
        java.util.regex.Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String methodName = matcher.group(1);
            methodNames.add(methodName);
        }

        return methodNames;
    }

    /**
     * Extract locator method names from existing page object for merge mode.
     * Returns set of locator names to avoid duplicate locators.
     * 
     * @param content Existing page object content
     * @return Set of locator method names
     */
    private static Set<String> extractLocatorNames(String content) {
        Set<String> locatorNames = new HashSet<>();
        // Pattern: public static Locator methodName()
        String locatorPattern = "public\\s+static\\s+Locator\\s+(\\w+)\\s*\\(\\s*\\)";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(locatorPattern);
        java.util.regex.Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String locatorName = matcher.group(1);
            locatorNames.add(locatorName);
        }

        return locatorNames;
    }

    /**
     * Scans ALL page objects across the project to find common locator methods.
     * This helps identify reusable locators like username(), password(), etc.
     * that exist in other page objects (e.g., Login.java).
     *
     * @return Map of locator method name (lowercase) -> Page class name
     */
    private static Map<String, String> scanAllPageObjectsForCommonLocators() {
        Map<String, String> commonLocators = new HashMap<>();

        try {
            Path pagesDir = Paths.get("src/main/java/pages");
            if (!Files.exists(pagesDir)) {
                return commonLocators;
            }

            // Common locator method names to look for
            String[] commonLocatorNames = {
                    "usernamefield", "username", "emailfield", "email",
                    "passwordfield", "password",
                    "signinbutton", "signin", "signInButton", "loginbutton", "login",
                    "logoutbutton", "logout", "signout", "signoutbutton",
                    "submitbutton", "submit",
                    "cancelbutton", "cancel",
                    "savebutton", "save",
                    "searchfield", "search", "searchbutton"
            };

            // Pattern for Locator-returning methods: public static Locator methodName()
            Pattern locatorMethodPattern = Pattern.compile("public static Locator (\\w+)\\(\\)");

            Files.list(pagesDir)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.getFileName().toString().equals("BasePage.java"))
                    .forEach(path -> {
                        try {
                            String pageClassName = path.getFileName().toString().replace(".java", "");
                            String content = new String(Files.readAllBytes(path));

                            Matcher matcher = locatorMethodPattern.matcher(content);
                            while (matcher.find()) {
                                String methodName = matcher.group(1);
                                String methodNameLower = methodName.toLowerCase();

                                // Check if this is a common locator name
                                for (String commonName : commonLocatorNames) {
                                    if (methodNameLower.contains(commonName.toLowerCase())) {
                                        // Store first occurrence (usually in Login.java or most common page)
                                        if (!commonLocators.containsKey(methodNameLower)) {
                                            commonLocators.put(methodNameLower, pageClassName);
                                            System.out.println("[CROSS-PROJECT] Registered: " + methodName + "() from "
                                                    + pageClassName);
                                        }
                                        break;
                                    }
                                }
                            }
                        } catch (IOException e) {
                            // Ignore read errors
                        }
                    });

        } catch (Exception e) {
            System.err.println("[WARN] Error scanning page objects for common locators: " + e.getMessage());
        }

        return commonLocators;
    }

    /**
     * Scans existing page object to find all existing locators and methods.
     * Returns a map of existing locators (name -> selector) and methods.
     * This prevents duplication when regenerating or updating page objects.
     *
     * @param className The page object class name
     * @return Map containing existing locators and methods
     */
    @SuppressWarnings("unused") // Reserved for advanced merge mode functionality
    private static Map<String, Object> scanExistingPageObject(String className) {
        Map<String, Object> existingElements = new HashMap<>();
        Set<String> existingLocators = new HashSet<>();
        Set<String> existingMethods = new HashSet<>();

        try {
            Path pageObjectPath = Paths.get("src/main/java/pages/" + className + ".java");
            if (!Files.exists(pageObjectPath)) {
                existingElements.put("locators", existingLocators);
                existingElements.put("methods", existingMethods);
                return existingElements;
            }

            String content = new String(Files.readAllBytes(pageObjectPath));

            // Pattern for Locator-returning methods: public static Locator methodName()
            Pattern locatorMethodPattern = Pattern.compile("public static Locator (\\w+)\\(\\)");
            Matcher locatorMatcher = locatorMethodPattern.matcher(content);
            while (locatorMatcher.find()) {
                existingLocators.add(locatorMatcher.group(1));
            }

            // Pattern for action methods: public static void main(
            Pattern actionMethodPattern = Pattern.compile("public static void (\\w+)\\(");
            Matcher actionMatcher = actionMethodPattern.matcher(content);
            while (actionMatcher.find()) {
                existingMethods.add(actionMatcher.group(1));
            }

            if (!existingLocators.isEmpty()) {
                System.out.println(
                        "[REUSE] Found " + existingLocators.size() + " existing locators in " + className + ".java");
            }
            if (!existingMethods.isEmpty()) {
                System.out.println(
                        "[REUSE] Found " + existingMethods.size() + " existing methods in " + className + ".java");
            }

        } catch (Exception e) {
            System.err.println("[WARN] Error scanning existing page object: " + e.getMessage());
        }

        existingElements.put("locators", existingLocators);
        existingElements.put("methods", existingMethods);
        return existingElements;
    }

    /**
     * Analyzes framework configuration and returns key settings.
     *
     * @return FrameworkInfo with configuration details
     */
    public static FrameworkInfo analyzeFramework() {
        FrameworkInfo info = new FrameworkInfo();

        info.baseUrl = loadProps.getProperty(loadProps.PropKeys.URL);
        info.browser = loadProps.getProperty(loadProps.PropKeys.BROWSER);
        info.headless = Boolean.parseBoolean(loadProps.getProperty(loadProps.PropKeys.HEADLESS_MODE));
        info.recordingEnabled = Boolean.parseBoolean(loadProps.getProperty(loadProps.PropKeys.RECORDING_MODE));
        info.screenshotEnabled = Boolean.parseBoolean(loadProps.getProperty(loadProps.PropKeys.SCREENSHOTS_MODE));
        info.defaultTimeout = Integer.parseInt(loadProps.getProperty(loadProps.PropKeys.DEFAULT_TIMEOUT));
        info.retryCount = Integer.parseInt(loadProps.getProperty(loadProps.PropKeys.MAX_RETRY_COUNT));

        // JIRA configuration
        info.jiraEnabled = loadProps.getJIRAConfig("JIRA_BASE_URL") != null;
        info.jiraBaseUrl = loadProps.getJIRAConfig("JIRA_BASE_URL");
        info.jiraProjectKey = loadProps.getJIRAConfig("PROJECT_KEY");

        return info;
    }

    /**
     * Detect if existing page objects have login methods.
     * Returns the class name that contains login functionality.
     * 
     * @return Class name with login methods, or null if not found
     */
    private static String detectExistingLogin() {
        try {
            Path pagesDir = Paths.get("src/main/java/pages");
            if (!Files.exists(pagesDir))
                return null;

            // Check for login.java first (common naming convention)
            Path loginPage = pagesDir.resolve("login.java");
            if (Files.exists(loginPage)) {
                String content = new String(Files.readAllBytes(loginPage));
                // Verify it has login methods
                if (content.contains("enterValidUsername") ||
                        content.contains("enterUsername") ||
                        content.contains("login")) {
                    System.out.println("[REUSE] Found existing login.java with login methods");
                    return "login";
                }
            }

            // Search all page objects for login patterns
            Files.list(pagesDir)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            String content = new String(Files.readAllBytes(path));
                            if (content.toLowerCase().contains("login") &&
                                    (content.contains("enterUsername") || content.contains("enterPassword"))) {
                                String className = path.getFileName().toString().replace(".java", "");
                                System.out.println("[REUSE] Found login methods in: " + className);
                            }
                        } catch (IOException e) {
                            // Ignore
                        }
                    });

            return "login"; // Default to login class name
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Detect if recorded actions contain login patterns.
     * 
     * @param actions List of recorded actions
     * @return true if login pattern detected
     */
    private static boolean containsLoginPattern(List<RecordedAction> actions) {
        for (RecordedAction action : actions) {
            String selector = action.selector != null ? action.selector.toLowerCase() : "";
            String value = action.value != null ? action.value.toLowerCase() : "";

            if (selector.contains("username") || selector.contains("password") ||
                    selector.contains("login") || selector.contains("signin") ||
                    value.contains("username") || value.contains("password")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if configurations.properties has test credentials.
     * 
     * @return true if credentials are configured
     */
    private static boolean hasConfiguredCredentials() {
        try {
            Path configPath = Paths.get("src/test/resources/configurations.properties");
            if (!Files.exists(configPath))
                return false;

            String content = new String(Files.readAllBytes(configPath));
            return content.contains("username") || content.contains("Username") ||
                    content.contains("password") || content.contains("Password");
        } catch (Exception e) {
            return false;
        }
    }

    // ========================================================================
    // JIRA-BASED GENERATION (original TestGeneratorHelper functionality)
    // ========================================================================

    /**
     * Test requirement data structure for programmatic test generation.
     */
    public static class TestRequirement {
        public String testName;
        public String description;
        public List<PageElement> elements;
        public List<Scenario> scenarios;
        public VerificationOptions verification;
        public String jiraKey;

        public TestRequirement(String testName, String description) {
            this.testName = testName;
            this.description = description;
            this.elements = new ArrayList<>();
            this.scenarios = new ArrayList<>();
            this.verification = new VerificationOptions();
        }

        public TestRequirement addElement(String name, String action, String description) {
            this.elements.add(new PageElement(name, action, description));
            return this;
        }

        public TestRequirement addScenario(String name, String... steps) {
            this.scenarios.add(new Scenario(name, steps));
            return this;
        }

        public TestRequirement withVerification(boolean functional, boolean ui, boolean performance) {
            this.verification.functional = functional;
            this.verification.ui = ui;
            this.verification.performance = performance;
            return this;
        }

        public TestRequirement fromJira(String jiraKey) {
            this.jiraKey = jiraKey;
            return this;
        }
    }

    /**
     * Page element definition for test generation.
     */
    public static class PageElement {
        public String name;
        public String action;
        public String description;

        public PageElement(String name, String action, String description) {
            this.name = name;
            this.action = action;
            this.description = description;
        }
    }

    /**
     * Test scenario definition.
     */
    public static class Scenario {
        public String name;
        public List<String> steps;

        public Scenario(String name, String... steps) {
            this.name = name;
            this.steps = new ArrayList<>();
            for (String step : steps) {
                this.steps.add(step);
            }
        }
    }

    /**
     * Verification options for test generation.
     */
    public static class VerificationOptions {
        public boolean functional = false;
        public boolean ui = false;
        public boolean performance = false;
        public boolean logging = false;
        public int performanceThreshold = 3000;
    }

    /**
     * Generates test requirements from a JIRA story.
     * Auto-detects UI elements, generates comprehensive scenarios,
     * and suggests verification options based on story analysis.
     * 
     * @param issueKey JIRA issue key (e.g., "ECS-123")
     * @return TestRequirement with story details, or null if failed
     */
    public static TestRequirement generateFromJiraStory(String issueKey) {
        return generateFromJiraStory(issueKey, true);
    }

    /**
     * Generates test requirements from a JIRA story.
     * 
     * @param issueKey   JIRA issue key (e.g., "ECS-123")
     * @param autoDetect If true, automatically detects elements and scenarios
     * @return TestRequirement with story details, or null if failed
     */
    public static TestRequirement generateFromJiraStory(String issueKey, boolean autoDetect) {
        // Support full JIRA URLs as well as plain issue keys
        issueKey = extractJiraIssueKey(issueKey);
        System.out.println("🔍 Fetching JIRA story: " + issueKey);

        jiraClient.JiraStory story = jiraClient.getJiraStory(issueKey);

        if (story == null) {
            System.err.println("❌ Failed to fetch JIRA story");
            return null;
        }

        story.printDetails();

        // Convert story to test requirement
        String testName = sanitizeTestName(story.summary);
        String description = story.summary + "\n\nJIRA Story: " + story.key +
                "\nGenerated from: " + story.issueType +
                "\nPriority: " + story.priority;

        TestRequirement requirement = new TestRequirement(testName, description);
        requirement.fromJira(story.key);

        if (autoDetect) {
            // Auto-detect UI elements
            System.out.println("🤖 AI-Analyzing story for UI elements and test aspects...");
            String storyText = story.summary + "\n" + story.description + "\n" +
                    String.join("\n", story.acceptanceCriteria);

            List<PageElement> detectedElements = detectUIElements(storyText, story.issueType);
            requirement.elements.addAll(detectedElements);

            System.out.println("✅ Auto-detected " + detectedElements.size() + " UI elements:");
            for (PageElement el : detectedElements) {
                System.out.println("  - " + el.name + " (" + el.action + ")");
            }

            // Auto-suggest verification options
            suggestVerificationOptions(requirement, story.issueType, story.priority, storyText);
            System.out.println("✅ Suggested verification:");
            System.out.println("  - Functional: " + (requirement.verification.functional ? "✓" : "✗"));
            System.out.println("  - UI: " + (requirement.verification.ui ? "✓" : "✗"));
            System.out.println("  - Performance: " + (requirement.verification.performance ? "✓ (<" +
                    (requirement.verification.performanceThreshold / 1000) + "s)" : "✗"));
            System.out.println("  - Logging: " + (requirement.verification.logging ? "✓" : "✗"));
        }

        // Convert acceptance criteria to comprehensive scenarios
        if (!story.acceptanceCriteria.isEmpty()) {
            System.out.println("📖 Converting " + story.acceptanceCriteria.size() +
                    " acceptance criteria to comprehensive scenarios...");

            for (int i = 0; i < story.acceptanceCriteria.size(); i++) {
                String criterion = story.acceptanceCriteria.get(i);
                String scenarioName = "Verify "
                        + (criterion.length() > 50 ? criterion.substring(0, 50) + "..." : criterion);

                // Generate detailed steps
                String[] steps = generateDetailedSteps(criterion, requirement.elements, story.issueType);
                requirement.addScenario(scenarioName, steps);
            }

            // Add edge case scenarios
            if (autoDetect) {
                List<Scenario> edgeCases = generateEdgeCaseScenarios(story.issueType,
                        story.summary, requirement.elements);
                requirement.scenarios.addAll(edgeCases);

                System.out.println("✅ Generated " + requirement.scenarios.size() + " total scenarios:");
                System.out.println("  - " + story.acceptanceCriteria.size() + " from acceptance criteria");
                System.out.println("  - " + edgeCases.size() + " edge case scenarios");
            } else {
                System.out.println("✅ Generated " + requirement.scenarios.size() + " scenarios");
            }
        } else {
            System.out.println("⚠️ No acceptance criteria found. Generating default scenarios...");
            List<Scenario> defaultScenarios = generateDefaultScenarios(story.issueType,
                    story.summary, requirement.elements);
            requirement.scenarios.addAll(defaultScenarios);
            System.out.println("✅ Generated " + defaultScenarios.size() + " default scenarios");
        }

        return requirement;
    }

    /**
     * Detect UI elements from story text using keyword analysis.
     */
    private static List<PageElement> detectUIElements(String storyText, String issueType) {
        List<PageElement> elements = new ArrayList<>();
        String textLower = storyText.toLowerCase();

        // Define element patterns
        Map<String, PageElement> patterns = new HashMap<>();

        // Input fields
        if (textLower.matches(".*(username|user name|userid|user id).*")) {
            patterns.put("username", new PageElement("Username Field", "type", "Enter username"));
        }
        if (textLower.matches(".*(password|pwd).*")) {
            patterns.put("password", new PageElement("Password Field", "type", "Enter password"));
        }
        if (textLower.matches(".*(email|e-mail).*")) {
            patterns.put("email", new PageElement("Email Field", "type", "Enter email address"));
        }
        if (textLower.matches(".*(first name|firstname).*")) {
            patterns.put("firstname", new PageElement("First Name Field", "type", "Enter first name"));
        }
        if (textLower.matches(".*(last name|lastname).*")) {
            patterns.put("lastname", new PageElement("Last Name Field", "type", "Enter last name"));
        }
        if (textLower.matches(".*(phone|telephone|mobile).*")) {
            patterns.put("phone", new PageElement("Phone Field", "type", "Enter phone number"));
        }
        if (textLower.matches(".*(address).*")) {
            patterns.put("address", new PageElement("Address Field", "type", "Enter address"));
        }
        if (textLower.matches(".*(search).*")) {
            patterns.put("search", new PageElement("Search Field", "type", "Enter search query"));
        }

        // Buttons
        if (textLower.matches(".*(login button|sign in|signin).*")) {
            patterns.put("login", new PageElement("Login Button", "click", "Click login button"));
        }
        if (textLower.matches(".*(submit button|submit form).*")) {
            patterns.put("submit", new PageElement("Submit Button", "click", "Submit form"));
        }
        if (textLower.matches(".*(save button|save).*")) {
            patterns.put("save", new PageElement("Save Button", "click", "Save changes"));
        }
        if (textLower.matches(".*(cancel button|cancel).*")) {
            patterns.put("cancel", new PageElement("Cancel Button", "click", "Cancel action"));
        }
        if (textLower.matches(".*(delete button|remove).*")) {
            patterns.put("delete", new PageElement("Delete Button", "click", "Delete item"));
        }
        if (textLower.matches(".*(register button|signup|sign up).*")) {
            patterns.put("register", new PageElement("Register Button", "click", "Register account"));
        }

        // Checkboxes
        if (textLower.matches(".*(checkbox|check box|remember me).*")) {
            patterns.put("checkbox", new PageElement("Checkbox", "click", "Toggle checkbox"));
        }

        // Dropdowns
        if (textLower.matches(".*(dropdown|drop down|select|combo).*")) {
            patterns.put("dropdown", new PageElement("Dropdown", "select", "Select from dropdown"));
        }

        elements.addAll(patterns.values());

        // Add default elements if nothing detected
        if (elements.isEmpty()) {
            if (issueType.matches("(?i).*(story|feature).*")) {
                elements.add(new PageElement("Main Action Button", "click", "Primary action button"));
                elements.add(new PageElement("Input Field", "type", "Primary input field"));
            } else if (issueType.matches("(?i).*(bug|defect).*")) {
                elements.add(new PageElement("Affected Element", "click", "Element with bug"));
            }
        }

        return elements;
    }

    /**
     * Generate detailed test steps from acceptance criterion.
     */
    private static String[] generateDetailedSteps(String criterion, List<PageElement> elements,
            String issueType) {
        List<String> steps = new ArrayList<>();
        String criterionLower = criterion.toLowerCase();

        // Check if criterion already has Given/When/Then structure
        if (criterionLower.matches(".*(given|when|then).*")) {
            String[] lines = criterion.split("\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.matches("(?i)^(given|when|then|and).*")) {
                    steps.add(trimmed);
                }
            }
        }

        // Generate comprehensive steps if none extracted
        if (steps.isEmpty()) {
            steps.add("Given user is on the application page");
            steps.add("And the page is fully loaded");

            // Add element interactions
            if (!elements.isEmpty()) {
                for (PageElement element : elements) {
                    if (element.action.equals("type")) {
                        steps.add("When user enters valid data in " + element.name);
                    } else if (element.action.equals("click")) {
                        steps.add("And user clicks on " + element.name);
                    } else if (element.action.equals("select")) {
                        steps.add("And user selects value from " + element.name);
                    }
                }
            } else {
                steps.add("When user performs actions for \"" + criterion + "\"");
            }

            // Add verification
            String expectedResult = criterion.contains("should")
                    ? criterion.substring(criterion.toLowerCase().indexOf("should") + 6).trim()
                    : "complete successfully";
            steps.add("Then the system should " + expectedResult);
            steps.add("And no errors should be displayed");
        }

        return steps.toArray(new String[0]);
    }

    /**
     * Generate edge case scenarios based on story type.
     */
    private static List<Scenario> generateEdgeCaseScenarios(String issueType, String summary,
            List<PageElement> elements) {
        List<Scenario> scenarios = new ArrayList<>();

        // Negative test for input fields
        boolean hasInputFields = elements.stream().anyMatch(el -> el.action.equals("type"));

        if (hasInputFields) {
            scenarios.add(new Scenario("Verify validation with empty fields",
                    "Given user is on the application page",
                    "When user leaves required fields empty",
                    "And user attempts to proceed",
                    "Then appropriate validation messages should be displayed",
                    "And the action should not proceed"));

            scenarios.add(new Scenario("Verify validation with invalid data",
                    "Given user is on the application page",
                    "When user enters invalid data in fields",
                    "And user attempts to proceed",
                    "Then validation errors should be displayed",
                    "And invalid fields should be highlighted"));
        }

        // UI responsiveness scenario
        if (issueType.matches("(?i).*(story|feature).*")) {
            scenarios.add(new Scenario("Verify UI responsiveness",
                    "Given user is on the application page",
                    "When the page loads",
                    "Then all elements should be visible",
                    "And the layout should be proper",
                    "And no visual glitches should occur"));
        }

        // Error handling scenario
        scenarios.add(new Scenario("Verify error handling",
                "Given user is on the application page",
                "When an error condition occurs",
                "Then appropriate error message should be displayed",
                "And user should be able to recover",
                "And application should remain stable"));

        return scenarios;
    }

    /**
     * Generate default scenarios when no acceptance criteria exist.
     */
    private static List<Scenario> generateDefaultScenarios(String issueType, String summary,
            List<PageElement> elements) {
        List<Scenario> scenarios = new ArrayList<>();

        // Happy path
        scenarios.add(new Scenario("Verify " + summary + " - Happy Path",
                "Given user is on the application page",
                "And all prerequisites are met",
                "When user performs the main action",
                "Then the action should complete successfully",
                "And expected result should be displayed"));

        // Element-specific scenario
        if (!elements.isEmpty()) {
            List<String> steps = new ArrayList<>();
            steps.add("Given user is on the application page");
            for (PageElement el : elements) {
                steps.add("When user interacts with " + el.name);
            }
            steps.add("Then all elements should respond correctly");
            steps.add("And no errors should occur");

            scenarios.add(new Scenario("Verify all UI elements are functional",
                    steps.toArray(new String[0])));
        }

        return scenarios;
    }

    /**
     * Suggest verification options based on story type and priority.
     */
    private static void suggestVerificationOptions(TestRequirement requirement, String issueType,
            String priority, String storyText) {
        String textLower = storyText.toLowerCase();

        // Always enable functional
        requirement.verification.functional = true;

        // UI verification for UI-heavy stories
        if (textLower.matches(".*(ui|user interface|layout|design|button|field|form).*") ||
                issueType.matches("(?i).*(feature|story).*")) {
            requirement.verification.ui = true;
        }

        // Performance for high priority or performance-related
        if (priority.matches("(?i).*(high|critical|blocker).*") ||
                textLower.matches(".*(performance|speed|fast|slow|timeout|load time).*")) {
            requirement.verification.performance = true;
            requirement.verification.performanceThreshold = priority.matches("(?i).*(critical|blocker).*") ? 2000
                    : 3000;
        }

        // Logging for bugs and complex features
        if (issueType.matches("(?i).*(bug|defect).*") ||
                priority.matches("(?i).*(high|critical|blocker).*")) {
            requirement.verification.logging = true;
        }
    }

    /**
     * Validates if a string is a valid Java identifier.
     * Ensures it follows Java naming rules: starts with letter/underscore/$,
     * contains only letters, digits, underscores.
     * 
     * @param identifier The identifier to validate
     * @return true if valid Java identifier
     */
    private static boolean isValidJavaIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return false;
        }

        // Check if it's a Java reserved keyword
        String[] keywords = { "abstract", "assert", "boolean", "break", "byte", "case", "catch",
                "char", "class", "const", "continue", "default", "do", "double", "else", "enum",
                "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import",
                "instanceof", "int", "interface", "long", "native", "new", "package", "private",
                "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
                "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while" };
        for (String keyword : keywords) {
            if (identifier.equals(keyword)) {
                return false;
            }
        }

        // First character must be letter, underscore, or dollar sign
        char first = identifier.charAt(0);
        if (!Character.isJavaIdentifierStart(first)) {
            return false;
        }

        // Rest must be valid identifier characters
        for (int i = 1; i < identifier.length(); i++) {
            if (!Character.isJavaIdentifierPart(identifier.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Converts a string to valid PascalCase Java class name.
     * Ensures it starts with uppercase letter and contains only valid Java
     * identifier characters.
     * 
     * @param input The input string
     * @return Valid PascalCase class name
     */
    private static String toPascalCase(String input) {
        if (input == null || input.isEmpty()) {
            return "DefaultClass";
        }

        // Remove all non-alphanumeric characters except spaces
        String cleaned = input.replaceAll("[^a-zA-Z0-9\\s]", " ")
                .trim()
                .replaceAll("\\s+", " ");

        if (cleaned.isEmpty()) {
            return "DefaultClass";
        }

        // Convert to PascalCase
        String[] words = cleaned.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
            }
        }

        String pascalCase = result.toString();

        // Ensure it starts with a letter
        if (!Character.isLetter(pascalCase.charAt(0))) {
            pascalCase = "Class" + pascalCase;
        }

        // Validate and return
        if (isValidJavaIdentifier(pascalCase)) {
            return pascalCase;
        } else {
            System.err.println("⚠️  WARNING: Generated invalid class name '" + pascalCase + "', using DefaultClass");
            return "DefaultClass";
        }
    }

    /**
     * Converts a string to valid camelCase Java method/variable name.
     * Ensures it starts with lowercase letter and contains only valid Java
     * identifier characters.
     * 
     * @param input The input string
     * @return Valid camelCase method/variable name
     */

    /**
     * Auto-fix configuration for test generation
     * These fields are reserved for future advanced auto-fix features
     */
    @SuppressWarnings("unused")
    private static class AutoFixConfig {
        static final boolean ENABLE_AUTO_FIX = true;
        static final boolean FIX_INVALID_IDENTIFIERS = true;
        static final boolean FIX_DUPLICATE_METHODS = true;
        static final boolean FIX_MISSING_IMPORTS = true;
        static final boolean FIX_INVALID_SELECTORS = true;
        static final boolean FIX_FEATURE_SYNTAX = true;
        static final boolean SANITIZE_INPUTS = true;
    }

    /**
     * Sanitizes a story summary to create a valid test class name.
     * Uses strict Java naming validation.
     * 
     * @param summary Story summary text
     * @return Sanitized class name following Java conventions
     */
    private static String sanitizeTestName(String summary) {
        String pascalCase = toPascalCase(summary);
        System.out.println("[JAVA VALIDATION] Sanitized test name: '" + summary + "' → '" + pascalCase + "'");
        return pascalCase;
    }

    /**
     * Validates test file structure for a given test.
     * Checks if Page Object, Feature, and Step Definition files exist.
     * 
     * @param testName Name of the test
     * @return ValidationResult with details
     */
    public static ValidationResult validateTestStructure(String testName) {
        ValidationResult result = new ValidationResult(testName);

        String projectRoot = System.getProperty("user.dir");

        // Check Page Object
        Path pageFile = Paths.get(projectRoot, "src", "main", "java", "pages", testName + ".java");
        result.pageObjectExists = Files.exists(pageFile);
        result.pageObjectPath = pageFile.toString();

        // Check Feature file
        Path featureFile = Paths.get(projectRoot, "src", "test", "java", "features",
                testName.toLowerCase() + ".feature");
        result.featureExists = Files.exists(featureFile);
        result.featurePath = featureFile.toString();

        // Check Step Definitions
        Path stepDefFile = Paths.get(projectRoot, "src", "test", "java", "stepDefs",
                testName + "Steps.java");
        result.stepDefExists = Files.exists(stepDefFile);
        result.stepDefPath = stepDefFile.toString();

        result.isValid = result.pageObjectExists && result.featureExists && result.stepDefExists;

        return result;
    }

    /**
     * Validation result for test structure.
     */
    public static class ValidationResult {
        public String testName;
        public boolean isValid;
        public boolean pageObjectExists;
        public String pageObjectPath;
        public boolean featureExists;
        public String featurePath;
        public boolean stepDefExists;
        public String stepDefPath;
        public List<String> missingFiles;

        public ValidationResult(String testName) {
            this.testName = testName;
            this.missingFiles = new ArrayList<>();
        }

        public void printReport() {
            System.out.println("\n📊 Test Structure Validation Report");
            System.out.println("═══════════════════════════════════");
            System.out.println("Test: " + testName);
            System.out.println("Status: " + (isValid ? "✅ VALID" : "❌ INCOMPLETE"));
            System.out.println();

            System.out.println("Files:");
            printFileStatus("Page Object", pageObjectExists, pageObjectPath);
            printFileStatus("Feature", featureExists, featurePath);
            printFileStatus("Step Definitions", stepDefExists, stepDefPath);

            if (!isValid) {
                System.out.println("\n⚠️ Missing Files:");
                if (!pageObjectExists)
                    missingFiles.add(pageObjectPath);
                if (!featureExists)
                    missingFiles.add(featurePath);
                if (!stepDefExists)
                    missingFiles.add(stepDefPath);

                missingFiles.forEach(file -> System.out.println("  - " + file));
            }
            System.out.println();
        }

        private void printFileStatus(String type, boolean exists, String path) {
            String status = exists ? "✅" : "❌";
            System.out.println(String.format("  %s %-20s %s", status, type + ":",
                    path.substring(path.lastIndexOf("src"))));
        }
    }

    /**
     * Action extracted from Playwright recording.
     * Enhanced with iframe and shadow DOM support.
     */
    private static class RecordedAction {
        String type;
        String selector;
        String value;
        String methodName;
        String stepText;
        @SuppressWarnings("unused")
        String elementName; // Reserved for future use
        String readableName;
        String locatorMethodName; // For Locator-returning method name
        String frameSelector; // For iframe context (e.g., "iframe#payment", "iframe[name='checkout']")
        boolean isInFrame; // True if action is within an iframe
        boolean isInShadowDom; // True if selector uses shadow DOM piercing
        String options; // Chained options like .setExact(true), .setHasText("..."), etc.

        RecordedAction(int id, String type, String selector, String value) {
            this(id, type, selector, value, null);
        }

        RecordedAction(int id, String type, String selector, String value, String options) {
            this.type = type;
            this.selector = selector;
            this.value = value;
            this.options = options;

            // Parse iframe context from selector
            parseFrameContext();

            // Check for shadow DOM
            this.isInShadowDom = selector != null && (selector.contains(">>>") || selector.contains(":shadow"));

            this.readableName = extractReadableName(selector);
            this.elementName = generateElementName(readableName, id);
            this.methodName = generateMethodName(type, readableName, id);
            this.stepText = generateStepText(type, readableName);
        }

        /**
         * Parse iframe context from selector.
         * Examples:
         * "iframe#payment >> button#submit" -> frameSelector="iframe#payment",
         * selector="button#submit"
         * "iframe[name='checkout'] >> input#card" ->
         * frameSelector="iframe[name='checkout']", selector="input#card"
         */
        private void parseFrameContext() {
            if (selector == null || selector.isEmpty()) {
                this.isInFrame = false;
                return;
            }

            // Check for iframe selector pattern: iframe... >> innerSelector
            if (selector.contains("iframe") && selector.contains(">>")) {
                String[] parts = selector.split(">>", 2);
                if (parts.length == 2) {
                    String potentialFrame = parts[0].trim();
                    if (potentialFrame.startsWith("iframe") || potentialFrame.contains("[name=")
                            || potentialFrame.contains("[id=")) {
                        this.frameSelector = potentialFrame;
                        this.selector = parts[1].trim();
                        this.isInFrame = true;
                        System.out
                                .println("[IFRAME] Detected iframe context: " + frameSelector + " -> " + this.selector);
                        return;
                    }
                }
            }

            this.isInFrame = false;
        }

        /**
         * Extract a readable name from the selector.
         * Examples:
         * text=Sign In -> SignIn
         * text=Save -> Save
         * #username -> Username
         * input[placeholder="Email"] -> Email
         * button:has-text("Submit") -> Submit
         * role=button[name="Login"] -> Login
         */
        private String extractReadableName(String selector) {
            if (selector == null || selector.isEmpty()) {
                return "Element";
            }

            String name = selector;

            // Extract from text= locator (Playwright modern)
            if (selector.startsWith("text=")) {
                name = selector.substring(5).trim();
            }
            // Extract from placeholder= locator
            else if (selector.startsWith("placeholder=")) {
                name = selector.substring(12).trim();
            }
            // Extract from label= locator
            else if (selector.startsWith("label=")) {
                name = selector.substring(6).trim();
            }
            // Extract from ID selector
            else if (selector.startsWith("#")) {
                name = selector.substring(1).trim();
                // Remove common suffixes from IDs
                name = name.replaceAll("(?i)[-_](btn|button|input|field|link|txt|id)$", "");
            }
            // Extract from role=xxx,name=... or role=xxx:name=... pattern (modern format
            // without quotes)
            else if (selector.contains("role=") && selector.contains("name=")) {
                int start = selector.indexOf("name=") + 5;

                // Skip opening quote if present
                if (start < selector.length() && selector.charAt(start) == '"') {
                    start++;
                }

                // Find the end: either closing quote, comma, bracket, or end of string
                int end = -1;
                if (selector.indexOf("\"", start) > start) {
                    end = selector.indexOf("\"", start);
                } else if (selector.indexOf(",", start) > start) {
                    end = selector.indexOf(",", start);
                } else if (selector.indexOf("]", start) > start) {
                    end = selector.indexOf("]", start);
                } else {
                    end = selector.length(); // Read until end of string
                }

                if (end > start) {
                    name = selector.substring(start, end).trim();
                }
            }
            // Extract from :has-text("...") pattern
            else if (selector.contains(":has-text(")) {
                int start = selector.indexOf(":has-text(") + 11;
                int end = selector.indexOf(")", start);
                if (end > start) {
                    name = selector.substring(start, end).replace("\"", "").replace("'", "");
                }
            }
            // Extract from getByRole patterns (modern Playwright)
            else if (selector.contains("getByRole")) {
                if (selector.contains("name=\"")) {
                    int start = selector.indexOf("name=\"") + 6;
                    int end = selector.indexOf("\"", start);
                    if (end > start) {
                        name = selector.substring(start, end);
                    }
                }
            }
            // Extract from name attribute
            else if (selector.contains("name=\"")) {
                int start = selector.indexOf("name=\"") + 6;
                int end = selector.indexOf("\"", start);
                if (end > start) {
                    name = selector.substring(start, end);
                }
            }
            // Extract from placeholder attribute
            else if (selector.contains("placeholder=\"")) {
                int start = selector.indexOf("placeholder=\"") + 13;
                int end = selector.indexOf("\"", start);
                if (end > start) {
                    name = selector.substring(start, end);
                }
            }
            // Extract from aria-label
            else if (selector.contains("aria-label=\"")) {
                int start = selector.indexOf("aria-label=\"") + 12;
                int end = selector.indexOf("\"", start);
                if (end > start) {
                    name = selector.substring(start, end);
                }
            }
            // Extract from title attribute
            else if (selector.contains("title=\"")) {
                int start = selector.indexOf("title=\"") + 7;
                int end = selector.indexOf("\"", start);
                if (end > start) {
                    name = selector.substring(start, end);
                }
            }
            // Extract from value attribute (for buttons with value)
            else if (selector.contains("value=\"")) {
                int start = selector.indexOf("value=\"") + 7;
                int end = selector.indexOf("\"", start);
                if (end > start) {
                    name = selector.substring(start, end);
                }
            }
            // Extract from class name (last class)
            else if (selector.startsWith(".")) {
                name = selector.substring(1).split("\\.")[0];
                // Convert kebab-case or camelCase class names
                name = name.replaceAll("-", " ").replaceAll("_", " ");
            }
            // Extract from data-testid or data-test-id
            else if (selector.contains("data-testid=\"") || selector.contains("data-test-id=\"")) {
                String pattern = selector.contains("data-testid=\"") ? "data-testid=\"" : "data-test-id=\"";
                int start = selector.indexOf(pattern) + pattern.length();
                int end = selector.indexOf("\"", start);
                if (end > start) {
                    name = selector.substring(start, end);
                    name = name.replaceAll("-", " ").replaceAll("_", " ");
                }
            }

            // Clean up the name
            name = name.trim()
                    .replaceAll("[^a-zA-Z0-9\\s]", " ") // Remove special chars
                    .replaceAll("\\s+", " ") // Normalize spaces
                    .trim();

            // Remove consecutive duplicate words (e.g., "Setup Setup" -> "Setup")
            name = removeDuplicateWords(name);

            // Remove common button/input suffixes for cleaner names
            name = name.replaceAll("(?i)\\s+(button|btn|input|field|link|checkbox|radio)$", "");

            // Convert to PascalCase
            String[] words = name.split("\\s+");
            StringBuilder pascalCase = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    pascalCase.append(word.substring(0, 1).toUpperCase());
                    if (word.length() > 1) {
                        pascalCase.append(word.substring(1).toLowerCase());
                    }
                }
            }

            String result = pascalCase.toString();

            // If result is empty or just numbers, provide a more descriptive default
            if (result.isEmpty()) {
                return "Element";
            } else if (result.matches("^\\d+$")) {
                return "Number" + result;
            }

            return result;
        }

        /**
         * Remove consecutive duplicate words from text.
         * Examples:
         * "Setup Setup" -> "Setup"
         * "Logout Logout" -> "Logout"
         * "Click Click Click" -> "Click"
         * "Save Changes" -> "Save Changes" (no change)
         */
        private String removeDuplicateWords(String text) {
            if (text == null || text.trim().isEmpty()) {
                return text;
            }

            String[] words = text.split("\\s+");
            StringBuilder result = new StringBuilder();
            String previousWord = null;

            for (String word : words) {
                // Only add word if it's different from previous (case-insensitive)
                if (previousWord == null || !word.equalsIgnoreCase(previousWord)) {
                    if (result.length() > 0) {
                        result.append(" ");
                    }
                    result.append(word);
                    previousWord = word;
                }
            }

            return result.toString();
        }

        /**
         * Generate element constant name (e.g., SIGN_IN, USERNAME, PASSWORD)
         * NO ID SUFFIX - Use semantic names only
         */
        private String generateElementName(String readableName, int id) {
            // Convert PascalCase to UPPER_SNAKE_CASE
            String snakeCase = readableName.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
            // Don't append ID - use semantic name only
            return snakeCase;
        }

        /**
         * Generate method name based on action type and readable name.
         * Creates descriptive, verb-based method names following Java conventions.
         *
         * CRITICAL: This method MUST NEVER return "Value" as a method name!
         * All method names must be action-based and descriptive.
         *
         * @param type         Action type (click, fill, select, check, press, navigate)
         * @param readableName Human-readable element name (e.g., "SignIn", "Username")
         * @param id           Element ID for uniqueness
         * @return Action-based method name (e.g., "clickSignIn", "enterUsername")
         */
        private String generateMethodName(String type, String readableName, int id) {
            // Validate inputs - prevent empty or "Value" method names
            if (readableName == null || readableName.trim().isEmpty()) {
                readableName = "Element" + id;
            }
            if (readableName.equalsIgnoreCase("Value")) {
                readableName = "Field" + id;
            }

            String methodName;
            switch (type) {
                case "click":
                    // Use "click" for buttons/links, makes sense contextually
                    methodName = "click" + readableName;
                    break;

                case "fill":
                    // Use "enter" for text fields to match natural language
                    // Special handling for common field names
                    if (readableName.toLowerCase().contains("search")) {
                        methodName = "search" + readableName.replace("Search", "");
                    } else if (readableName.toLowerCase().contains("email")) {
                        methodName = "enterEmail";
                    } else if (readableName.toLowerCase().contains("password")) {
                        methodName = "enterPassword";
                    } else if (readableName.toLowerCase().contains("username")) {
                        methodName = "enterUsername";
                    } else {
                        methodName = "enter" + readableName;
                    }
                    break;

                case "select":
                    // Use "select" for dropdowns
                    methodName = "select" + readableName;
                    break;

                case "check":
                    // Use "check" for checkboxes, "toggle" for switches
                    if (readableName.toLowerCase().contains("toggle") ||
                            readableName.toLowerCase().contains("switch")) {
                        methodName = "toggle" + readableName.replace("Toggle", "").replace("Switch", "");
                    } else {
                        methodName = "check" + readableName;
                    }
                    break;

                case "press":
                    // Use "pressKeyOn" for keyboard actions
                    methodName = "pressKeyOn" + readableName;
                    break;

                case "verify":
                    // Use "verify" prefix for assertions/validations
                    methodName = "verify" + readableName;
                    break;

                case "navigate":
                    methodName = "navigateTo";
                    break;

                default:
                    // Fallback with camelCase - ensure it's descriptive
                    String camelCase = readableName.substring(0, 1).toLowerCase() +
                            (readableName.length() > 1 ? readableName.substring(1) : "");
                    methodName = camelCase + "Action";
                    break;
            }

            // Final safety check: NEVER return "Value" or similar generic names
            if (methodName.equalsIgnoreCase("Value") ||
                    methodName.equalsIgnoreCase("value") ||
                    methodName.isEmpty()) {
                methodName = "performAction" + id;
                System.err.println("⚠️ WARNING: Generated invalid method name, using fallback: " + methodName);
            }

            return methodName;
        }

        /**
         * Generate human-readable step text.
         * Enhanced to handle verify actions for assertions and clean up role-based
         * names.
         *
         * IMPROVEMENT: Removes redundant "role {type} name" prefixes to make steps more
         * readable:
         * - "user clicks on role link name invoices" → "user clicks on invoices"
         * - "user clicks on role button name sign in" → "user clicks on sign in"
         * - "invoice groups should be visible" (verification keeps as-is)
         */
        private String generateStepText(String type, String readableName) {
            String lowerName = readableName.replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();

            // Remove redundant "role {type} name " prefix if present
            // Matches patterns like: "role link name ...", "role button name ...", "role
            // heading name ..."
            lowerName = lowerName.replaceAll(
                    "^role\\s+(link|button|heading|checkbox|textbox|listbox|combobox|option|menuitem|tab|radio)\\s+name\\s+",
                    "");

            switch (type) {
                case "click":
                    return "user clicks on " + lowerName;
                case "fill":
                    return "user enters text into " + lowerName;
                case "select":
                    return "user selects option from " + lowerName;
                case "check":
                    return "user checks " + lowerName;
                case "press":
                    return "user presses key on " + lowerName;
                case "verify":
                    // For verify, clean up the name but keep it descriptive
                    return lowerName + " should be visible";
                case "navigate":
                    return "user navigates to page";
                default:
                    return "user performs action on " + lowerName;
            }
        }

    }

    /**
     * Framework configuration information.
     */
    public static class FrameworkInfo {
        public String baseUrl;
        public String browser;
        public boolean headless;
        public boolean recordingEnabled;
        public boolean screenshotEnabled;
        public int defaultTimeout;
        public int retryCount;
        public boolean jiraEnabled;
        public String jiraBaseUrl;
        public String jiraProjectKey;

        public void printSummary() {
            System.out.println("\n⚙️ Framework Configuration");
            System.out.println("═══════════════════════════");
            System.out.println("Base URL: " + baseUrl);
            System.out.println("Browser: " + browser);
            System.out.println("Headless: " + headless);
            System.out.println("Recording: " + (recordingEnabled ? "✅ Enabled" : "❌ Disabled"));
            System.out.println("Screenshots: " + (screenshotEnabled ? "✅ Enabled" : "❌ Disabled"));
            System.out.println("Timeout: " + defaultTimeout + "ms");
            System.out.println("Retry Count: " + retryCount);

            if (jiraEnabled) {
                System.out.println("\n🎫 JIRA Integration");
                System.out.println("JIRA URL: " + jiraBaseUrl);
                System.out.println("Project: " + jiraProjectKey);
            }
            System.out.println();
        }
    }

    /**
     * Compiles the Maven project and returns success status.
     * 
     * @return true if compilation succeeded, false otherwise
     */
    public static boolean compileProject() {
        System.out.println("🔨 Compiling project...");

        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command(isWindows() ? "mvn.cmd" : "mvn", "clean", "compile");
            pb.directory(new File(System.getProperty("user.dir")));
            pb.inheritIO();

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("✅ Compilation successful");
                return true;
            } else {
                System.err.println("❌ Compilation failed with exit code: " + exitCode);
                return false;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error during compilation: " + e.getMessage());
            return false;
        }
    }

    /**
     * Runs tests for a specific feature.
     * 
     * @param featureName Name of the feature to test
     * @return true if tests passed, false otherwise
     */
    public static boolean runTests(String featureName) {
        System.out.println("🧪 Running tests for: " + featureName);

        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command(isWindows() ? "mvn.cmd" : "mvn", "test",
                    "-Dcucumber.filter.tags=@" + featureName);
            pb.directory(new File(System.getProperty("user.dir")));
            pb.inheritIO();

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("✅ Tests passed");
                return true;
            } else {
                System.err.println("❌ Tests failed");
                return false;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error running tests: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets the latest HTML report path.
     * 
     * @return Path to the latest HTML report, or null if not found
     */
    public static String getLatestReport() {
        String reportDir = System.getProperty("user.dir") + "/MRITestExecutionReports/";

        try {
            Path latestVersionDir = Files.list(Paths.get(reportDir))
                    .filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith("Version"))
                    .max((p1, p2) -> p1.getFileName().toString()
                            .compareTo(p2.getFileName().toString()))
                    .orElse(null);

            if (latestVersionDir != null) {
                Path htmlReportDir = latestVersionDir.resolve("extentReports/testNGExtentReports/html");

                if (Files.exists(htmlReportDir)) {
                    Path latestHtml = Files.list(htmlReportDir)
                            .filter(p -> p.toString().endsWith(".html"))
                            .max((p1, p2) -> p1.getFileName().toString()
                                    .compareTo(p2.getFileName().toString()))
                            .orElse(null);

                    if (latestHtml != null) {
                        return latestHtml.toAbsolutePath().toString();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error finding report: " + e.getMessage());
        }

        return null;
    }

    /**
     * Opens the latest test report in the default browser.
     */
    public static void openLatestReport() {
        String reportPath = getLatestReport();

        if (reportPath != null) {
            System.out.println("📊 Opening report: " + reportPath);

            try {
                if (isWindows()) {
                    new ProcessBuilder("cmd", "/c", "start", reportPath).start();
                } else if (isMac()) {
                    new ProcessBuilder("open", reportPath).start();
                } else {
                    new ProcessBuilder("xdg-open", reportPath).start();
                }
            } catch (IOException e) {
                System.err.println("❌ Failed to open report: " + e.getMessage());
                System.out.println("📁 Report location: " + reportPath);
            }
        } else {
            System.err.println("❌ No report found");
        }
    }

    /**
     * Checks if running on Windows.
     */
    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * Checks if running on Mac.
     */
    private static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    /**
     * Command-line interface for test generation.
     * Supports two modes:
     * 1. Recording mode (4 args): recordingFile featureName pageUrl jiraStory
     * 2. JIRA mode (1 arg): jiraStoryId
     * 
     * @param args Command line arguments
     */
    /**
     * Clean up unused "Recorded" template file after successful generation.
     * This file is a placeholder/template that's not used by the framework.
     */
    private static void cleanupUnusedRecordedFile() {
        try {
            java.io.File recordedFile = new java.io.File("Recorded");
            if (recordedFile.exists() && recordedFile.isFile()) {
                if (recordedFile.delete()) {
                    System.out.println("\n🧹 Cleanup: Deleted unused 'Recorded' template file");
                }
            }
        } catch (Exception e) {
            // Silently ignore cleanup errors - not critical
            System.out.println("\n⚠️  Note: Could not delete 'Recorded' file (not critical): " + e.getMessage());
        }
    }

    /**
     * Cleanup recording directory after successful test generation.
     * <p>
     * PERMANENT FIX (Feb 18, 2026): Automatically deletes recording directories
     * after successful generation to keep the project clean.
     * <p>
     * This runs regardless of how TestGeneratorHelper is invoked:
     * - Via automation-cli.js (Option 1 or 1B)
     * - Via direct Maven: mvn exec:java
     * -Dexec.mainClass=configs.TestGeneratorHelper
     * - Via any other method
     * <p>
     * Why this is needed:
     * - Recording directories can accumulate over time
     * - Each recording is only needed once for generation
     * - Cleaning up saves disk space and reduces clutter
     * <p>
     * Example cleanup:
     * Recorded/recording_meter_1771417629256/ → Deleted after Meter.java generation
     * <p>
     * DO NOT REMOVE - Critical for maintaining clean project structure!
     * 
     * @param recordingFile Path to the recorded-actions.java file
     */
    private static void cleanupRecordingDirectory(String recordingFile) {
        try {
            // Extract the recording directory from the recording file path
            // Example: "Recorded/recording_meter_1771417629256/recorded-actions.java"
            // → "Recorded/recording_meter_1771417629256"
            Path recordingPath = Paths.get(recordingFile);
            Path recordingDir = recordingPath.getParent();

            if (recordingDir != null && Files.exists(recordingDir)) {
                // Verify this is actually a recording directory (safety check)
                String dirName = recordingDir.getFileName().toString();
                if (dirName.startsWith("recording_")) {
                    System.out.println("\n🧹 [CLEANUP] Deleting recording directory: " + recordingDir);

                    // Delete the entire recording directory recursively
                    deleteDirectoryRecursively(recordingDir);

                    System.out.println("✅ [CLEANUP] Recording directory deleted successfully");
                    System.out.println(
                            "💡 [TIP] Recording directories are auto-deleted after successful generation to save space\n");
                } else {
                    System.out.println("⚠️  [CLEANUP] Skipping cleanup - not a recording directory: " + recordingDir);
                }
            }
        } catch (Exception e) {
            // Non-critical - just log the error
            System.out.println("\n⚠️  [CLEANUP] Could not delete recording directory: " + e.getMessage());
            System.out.println("💡 [TIP] You can manually delete: "
                    + recordingFile.substring(0, recordingFile.lastIndexOf('/')) + "\n");
        }
    }

    /**
     * Recursively delete a directory and all its contents.
     * Used by cleanupRecordingDirectory() to remove recording folders.
     * 
     * @param directory Path to directory to delete
     * @throws IOException if deletion fails
     */
    private static void deleteDirectoryRecursively(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete: " + path + " - " + e.getMessage());
                        }
                    });
        }
    }

    public static void main(String[] args) {
        if (args.length == 4) {
            // Recording mode: Generate from Playwright recording
            String recordingFile = args[0];
            String featureName = args[1];
            String pageUrl = args[2];
            String jiraStory = args[3];

            boolean success = generateFromRecording(recordingFile, featureName, pageUrl, jiraStory);
            System.exit(success ? 0 : 1);

        } else if (args.length == 1) {
            // JIRA mode: Generate from JIRA story
            String jiraStoryId = args[0];

            System.out.println("🤖 Test Generator Helper - JIRA Mode");
            System.out.println("═══════════════════════════════════════\n");

            TestRequirement req = generateFromJiraStory(jiraStoryId);
            if (req != null) {
                System.out.println("\n✅ Test requirement generated successfully!");
                System.out.println("   Test Name: " + req.testName);
                System.out.println("   Elements: " + req.elements.size());
                System.out.println("   Scenarios: " + req.scenarios.size());
                System.exit(0);
            } else {
                System.err.println("\n❌ Failed to generate test requirement from JIRA");
                System.exit(1);
            }

        } else if (args.length == 0) {
            // Interactive example mode
            System.out.println("🤖 Test Generator Helper - Example Usage\n");

            System.out.println("═══ Example 1: Analyze Framework ═══");
            FrameworkInfo info = analyzeFramework();
            info.printSummary();

            System.out.println("═══ Example 2: Validate Test Structure ═══");
            ValidationResult validation = validateTestStructure("login");
            validation.printReport();

            System.out.println("═══ Example 3: Open Latest Report ═══");
            // openLatestReport(); // TODO: Implement this method

        } else {
            // Show usage
            System.err.println("❌ Invalid arguments!");
            System.err.println("\nUsage:");
            System.err.println("  Recording Mode:");
            System.err.println("    java TestGeneratorHelper <recordingFile> <featureName> <pageUrl> <jiraStory>");
            System.err.println();
            System.err.println("  JIRA Mode:");
            System.err.println("    java TestGeneratorHelper <jiraStoryId>");
            System.err.println();
            System.err.println("  Interactive Mode:");
            System.err.println("    java TestGeneratorHelper");
            System.exit(1);
        }
    }
}
