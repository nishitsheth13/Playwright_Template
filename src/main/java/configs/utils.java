package configs;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;

/**
 * Utility class providing common helper methods for test automation.
 * Includes element interactions, file operations, and report management.
 */
public class utils extends base {

    // Constants
    private static final String TIMESTAMP_FORMAT = "yyyy_MM_dd_HH_mm";
    private static final String VERSION_LOCATOR = "xpath=/html/body/div/footer/div[1]/div[1]";
    private static final String REPORT_BASE_PATH = System.getProperty("user.dir") + "/MRITestExecutionReports/";
    private static final String CUCUMBER_REPORTS_FOLDER = "cucumberExtentReports";

    public static boolean isDelete = Boolean.parseBoolean(loadProps.getProperty("Remove_Old_Reports"));
    public static int folderCount = Integer
            .parseInt(loadProps.getProperty("Version_Record") != null ? loadProps.getProperty("Version_Record") : "5");

    /**
     * Clicks on an element with comprehensive auto-wait.
     * Auto-waits for: attached → visible → stable → enabled
     * Includes 3-attempt retry for stale elements and detailed logging.
     *
     * @param element The locator string for the element
     */
    public static void clickOnElement(String element) {
        Locator locator = page.locator(element);
        int maxRetries = 3;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("⏳ [Auto-Wait] Preparing click (attempt " + attempt + "/" + maxRetries + "): " + element);
                
                // Step 1: Wait for attached to DOM
                locator.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED)
                    .setTimeout(TimeoutConfig.getDefaultTimeout()));
                
                // Step 2: Wait for visible
                locator.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(TimeoutConfig.getDefaultTimeout()));
                
                // Step 3: Wait for animations to complete (stable state)
                try {
                    var initialBox = locator.boundingBox();
                    if (initialBox != null) {
                        Thread.sleep(300); // Wait for stability
                        var finalBox = locator.boundingBox();
                        if (finalBox == null || Math.abs(initialBox.x - finalBox.x) >= 1) {
                            Thread.sleep(200); // Extra wait if still animating
                        }
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                
                // Step 4: Ensure enabled for clicks
                int enabledChecks = 0;
                while (!locator.isEnabled() && enabledChecks < 10) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    enabledChecks++;
                }
                
                // Perform click
                locator.click();
                System.out.println("✅ Clicked successfully: " + element);
                return; // Success
                
            } catch (Exception e) {
                String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                
                // Handle stale element with retry
                if (errorMsg.contains("stale") && attempt < maxRetries) {
                    System.out.println("🔄 [Auto-Wait] Stale element detected, retrying (" + attempt + "/" + maxRetries + ")");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }
                
                // Final attempt failed
                if (attempt == maxRetries) {
                    System.err.println("❌ Click failed after " + maxRetries + " attempts: " + element);
                    System.err.println("💡 Suggestions:");
                    System.err.println("   1. Verify element selector is correct");
                    System.err.println("   2. Check if element is conditionally rendered");
                    System.err.println("   3. Increase timeout in configurations.properties");
                    throw e;
                }
            }
        }
    }

    /**
     * Selects a dropdown option by visible text with comprehensive auto-wait.
     * Auto-waits for: attached → visible → enabled before selection.
     * Includes 3-attempt retry for stale elements.
     *
     * @param element The locator string for the dropdown element
     * @param text    The visible text of the option to select
     */
    public static void selectDropDownValueByText(String element, String text) {
        Locator locator = page.locator(element);
        int maxRetries = 3;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("⏳ [Auto-Wait] Preparing dropdown selection (attempt " + attempt + "/" + maxRetries + ")");
                
                // Auto-wait for attached and visible
                locator.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED)
                    .setTimeout(TimeoutConfig.getDefaultTimeout()));
                    
                locator.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(TimeoutConfig.getDefaultTimeout()));
                
                // Perform selection
                locator.selectOption(text);
                System.out.println("✅ Selected dropdown option: " + text);
                return; // Success
                
            } catch (Exception e) {
                String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                
                // Handle stale element with retry
                if (errorMsg.contains("stale") && attempt < maxRetries) {
                    System.out.println("🔄 [Auto-Wait] Stale element detected, retrying (" + attempt + "/" + maxRetries + ")");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }
                
                // Final attempt failed
                if (attempt == maxRetries) {
                    System.err.println("❌ Dropdown selection failed after " + maxRetries + " attempts: " + element);
                    throw e;
                }
            }
        }
    }

    /**
     * Clears text from an element with comprehensive auto-wait.
     * Auto-waits for: attached → visible before clearing.
     *
     * @param element The locator string for the element
     */
    public static void clearText(String element) {
        Locator locator = page.locator(element);
        
        try {
            System.out.println("⏳ [Auto-Wait] Preparing to clear text");
            
            // Auto-wait for visible
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(TimeoutConfig.getDefaultTimeout()));

            locator.clear();
            System.out.println("✅ Cleared text successfully");
        } catch (Exception e) {
            System.err.println("❌ Error clearing text: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Enters text into an element with comprehensive auto-wait.
     * Auto-waits for: attached → visible → stable before typing.
     * Clears existing text before entering new text.
     * Includes 3-attempt retry for stale elements.
     *
     * @param element   The locator string for the element
     * @param inputText The text to enter
     */
    public static void enterText(String element, String inputText) {
        if (inputText == null || inputText.isEmpty()) {
            System.out.println("⚠️ Skipped entering text - input is null or empty");
            return;
        }
        
        Locator locator = page.locator(element);
        int maxRetries = 3;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("⏳ [Auto-Wait] Preparing to enter text (attempt " + attempt + "/" + maxRetries + ")");
                
                // Step 1: Wait for attached to DOM
                locator.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED)
                    .setTimeout(TimeoutConfig.getDefaultTimeout()));
                
                // Step 2: Wait for visible
                locator.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(TimeoutConfig.getDefaultTimeout()));
                
                // Step 3: Wait for stable (animations complete)
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                
                // Perform text entry
                locator.clear();
                locator.fill(inputText);
                System.out.println("✅ Entered text successfully: " + inputText);
                return; // Success
                
            } catch (Exception e) {
                String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                
                // Handle stale element with retry
                if (errorMsg.contains("stale") && attempt < maxRetries) {
                    System.out.println("🔄 [Auto-Wait] Stale element detected, retrying (" + attempt + "/" + maxRetries + ")");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }
                
                // Final attempt failed
                if (attempt == maxRetries) {
                    System.err.println("❌ Enter text failed after " + maxRetries + " attempts: " + element);
                    throw e;
                }
            }
        }
    }

    /**
     * Clears and enters text into an element with comprehensive auto-wait.
     * Auto-waits for: attached → visible → stable before typing.
     * Includes 3-attempt retry for stale elements.
     *
     * @param element   The locator string for the element
     * @param inputText The text to enter
     */
    public static void clearAndEnterText(String element, String inputText) {
        if (inputText == null || inputText.isEmpty()) {
            System.out.println("⚠️ Skipped entering text - input is null or empty");
            return;
        }
        
        Locator locator = page.locator(element);
        int maxRetries = 3;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("⏳ [Auto-Wait] Preparing to clear and enter text (attempt " + attempt + "/" + maxRetries + ")");
                
                // Auto-wait for visible
                locator.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(TimeoutConfig.getDefaultTimeout()));
                
                // Wait for stable
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                
                locator.clear();
                locator.fill(inputText);
                System.out.println("✅ Cleared and entered text successfully");
                return;
                
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().toLowerCase().contains("stale") && attempt < maxRetries) {
                    try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    continue;
                }
                if (attempt == maxRetries) {
                    System.err.println("❌ Clear and enter text failed after " + maxRetries + " attempts");
                    throw e;
                }
            }
        }
    }

    /**
     * Gets the text content of an element with comprehensive auto-wait.
     * Auto-waits for: attached → visible → stable before reading text.
     *
     * @param element The locator string for the element
     * @return The element's text content
     */
    public static String getElementText(String element) {
        Locator locator = page.locator(element);
        
        try {
            System.out.println("⏳ [Auto-Wait] Waiting to read element text");
            
            // Auto-wait for visible
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(TimeoutConfig.getDefaultTimeout()));
            
            // Wait for stable (text might be loading)
            try {
                Thread.sleep(200);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            String elementText = locator.innerText();
            System.out.println("📝 Element text: " + elementText);
            return elementText;
        } catch (Exception e) {
            System.err.println("❌ Error getting text: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Gets the visible text of an element after waiting for visibility.
     *
     * @param element The locator string for the element
     * @return The element's visible text
     */
    public static String getText(String element) {
        try {
            System.out.println("⏳ Waiting for element to be visible: " + element);
            // Auto-wait for element to be visible
            page.locator(element).waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));

            String elementText = page.locator(element).innerText();
            System.out.println("📝 Visible text: " + elementText);
            return elementText;
        } catch (Exception e) {
            System.err.println("❌ Error getting text from element: " + element + " - " + e.getMessage());
            throw e;
        }
    }

    public static String generateRandomText(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    /**
     * Access elements inside Shadow DOM.
     * Useful for modern web components that use Shadow DOM encapsulation.
     * 
     * @param shadowHost     The selector for the shadow host element
     * @param targetSelector The selector for the element inside shadow root
     * @return Locator for the element inside shadow DOM
     */
    public static Locator getShadowDomElement(String shadowHost, String targetSelector) {
        Locator hostLocator = page.locator(shadowHost);
        return hostLocator.locator(":scope >> shadow-root >> " + targetSelector);
    }

    // ============================================================
    // DYNAMIC LOCATOR STRATEGIES (For Random/Dynamic IDs)
    // ============================================================

    /**
     * Find element by text content (best for dynamic IDs).
     * Uses relative XPath to locate elements by visible text.
     * 
     * Example: findByText("button", "Submit") -> //button[text()='Submit']
     * 
     * @param tagName HTML tag name (button, div, span, etc.)
     * @param text    Exact text content
     * @return Locator string
     */
    public static String findByText(String tagName, String text) {
        return String.format("xpath=//%s[text()='%s']", tagName, text);
    }

    /**
     * Find element by partial text (when text is dynamic or long).
     * 
     * Example: findByPartialText("button", "Submit") ->
     * //button[contains(text(),'Submit')]
     * 
     * @param tagName     HTML tag name
     * @param partialText Partial text content
     * @return Locator string
     */
    public static String findByPartialText(String tagName, String partialText) {
        return String.format("xpath=//%s[contains(text(),'%s')]", tagName, partialText);
    }

    /**
     * Find element by attribute (class, data-*, aria-*, etc.).
     * Best for elements with stable attributes but dynamic IDs.
     * 
     * Example: findByAttribute("button", "class", "btn-primary")
     * Result: //button[@class='btn-primary']
     * 
     * @param tagName   HTML tag name
     * @param attribute Attribute name
     * @param value     Attribute value
     * @return Locator string
     */
    public static String findByAttribute(String tagName, String attribute, String value) {
        return String.format("xpath=//%s[@%s='%s']", tagName, attribute, value);
    }

    /**
     * Find element by partial attribute match.
     * Useful when attribute values are dynamic or concatenated.
     * 
     * Example: findByPartialAttribute("button", "class", "submit")
     * Result: //button[contains(@class,'submit')]
     * 
     * @param tagName      HTML tag name
     * @param attribute    Attribute name
     * @param partialValue Partial attribute value
     * @return Locator string
     */
    public static String findByPartialAttribute(String tagName, String attribute, String partialValue) {
        return String.format("xpath=//%s[contains(@%s,'%s')]", tagName, attribute, partialValue);
    }

    /**
     * Find element by parent-child relationship (when child has dynamic ID).
     * Navigate from stable parent to dynamic child.
     * 
     * Example: findByParentChild("div[@class='form-group']", "input")
     * Result: //div[@class='form-group']//input
     * 
     * @param parentLocator Parent element xpath (without xpath= prefix)
     * @param childTag      Child element tag
     * @return Locator string
     */
    public static String findByParentChild(String parentLocator, String childTag) {
        return String.format("xpath=//%s//%s", parentLocator, childTag);
    }

    /**
     * Find element by label text (for form inputs).
     * Finds input/textarea associated with a label.
     * 
     * Example: findByLabel("Username")
     * Result: //label[text()='Username']/following-sibling::input
     * 
     * @param labelText Label text content
     * @return Locator string
     */
    public static String findByLabel(String labelText) {
        return String.format(
                "xpath=//label[text()='%s']/following-sibling::input | //label[text()='%s']/following-sibling::textarea",
                labelText, labelText);
    }

    /**
     * Find element by placeholder (for inputs with dynamic IDs).
     * 
     * Example: findByPlaceholder("Enter username")
     * Result: //input[@placeholder='Enter username']
     * 
     * @param placeholderText Placeholder text
     * @return Locator string
     */
    public static String findByPlaceholder(String placeholderText) {
        return String.format("xpath=//input[@placeholder='%s'] | //textarea[@placeholder='%s']",
                placeholderText, placeholderText);
    }

    /**
     * Find button by text or aria-label (most reliable for buttons).
     * 
     * Example: findButton("Submit")
     * Result: //button[text()='Submit' or @aria-label='Submit']
     * 
     * @param buttonText Button text or aria-label
     * @return Locator string
     */
    public static String findButton(String buttonText) {
        return String.format("xpath=//button[text()='%s' or @aria-label='%s' or @value='%s']",
                buttonText, buttonText, buttonText);
    }

    /**
     * Find element by position relative to another element.
     * Useful when target element has no unique attributes.
     * 
     * Example: findByRelativePosition("div[@class='header']", "following-sibling",
     * "button", 1)
     * Result: //div[@class='header']/following-sibling::button[1]
     * 
     * @param anchorLocator Anchor element xpath (without xpath= prefix)
     * @param axis          XPath axis (following-sibling, preceding-sibling,
     *                      parent, ancestor, etc.)
     * @param targetTag     Target element tag
     * @param position      Position index (1-based)
     * @return Locator string
     */
    public static String findByRelativePosition(String anchorLocator, String axis, String targetTag, int position) {
        return String.format("xpath=//%s/%s::%s[%d]", anchorLocator, axis, targetTag, position);
    }

    /**
     * Navigate through menu hierarchy with expand support.
     * Handles expandable menus, main menus, submenus, and final page navigation.
     * 
     * @param expandMenu Locator for expand button
     * @param mainMenu   Locator for main menu
     * @param subMenu    Locator for submenu
     * @param pageLink   Locator for final page link
     * @throws InterruptedException if thread is interrupted
     */
    public static void navigateToMenu(String expandMenu, String mainMenu, String subMenu, String pageLink)
            throws InterruptedException {
        Thread.sleep(500);
        Locator expand = page.locator(expandMenu);
        Locator main = page.locator(mainMenu);
        Locator sub = page.locator(subMenu);
        Locator pageLocator = page.locator(pageLink);

        boolean isExpanded = main.isVisible();
        if (!isExpanded && expand.isVisible()) {
            expand.click();
            Thread.sleep(300);
        }

        if (main.isVisible()) {
            main.click();
            if (sub.isVisible()) {
                sub.click();
                if (pageLocator.isVisible()) {
                    pageLocator.click();
                    System.out.println("✅ Navigated to: " + pageLink);
                } else {
                    System.err.println("❌ Page link not visible");
                }
            } else {
                System.err.println("❌ Submenu not visible");
            }
        } else {
            System.err.println("❌ Main menu not visible");
        }
    }

    /**
     * Handle file downloads with Playwright.
     * Waits for download to complete and returns the downloaded file path.
     * 
     * @param downloadTrigger Runnable that triggers the download
     * @return Path to the downloaded file
     */
    public static Path handleDownload(Runnable downloadTrigger) {
        try {
            com.microsoft.playwright.Download download = page.waitForDownload(downloadTrigger::run);

            String version = loadProps.getProperty("Version");
            Path downloadPath = Paths.get(System.getProperty("user.dir") + "/MRITestExecutionReports/" +
                    (version != null ? version.replaceAll("[()-+.^:, ]", "") : "default") + "/downloads/" +
                    download.suggestedFilename());

            if (!Files.exists(downloadPath.getParent())) {
                Files.createDirectories(downloadPath.getParent());
            }

            download.saveAs(downloadPath);
            System.out.println("✅ File downloaded: " + download.suggestedFilename());
            return downloadPath;
        } catch (Exception e) {
            System.err.println("❌ Download failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a timestamp string for file naming.
     * 
     * @return Formatted timestamp string (yyyy_MM_dd_HH_mm)
     */
    public static String timeStamp() {
        DateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * Checks if an element is present and visible on the page.
     * Uses a short timeout to avoid long waits.
     *
     * @param element The locator string for the element
     * @return true if element is visible, false otherwise
     */
    public static boolean isElementPresent(String element) {
        try {
            // Wait for element with a short timeout (3 seconds)
            page.locator(element).waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(3000));
            return page.locator(element).isVisible();
        } catch (TimeoutError e) {
            System.out.println("ℹ️ Element not visible: " + element);
            return false;
        } catch (Exception e) {
            System.err.println("⚠️ Error checking element presence: " + element + " - " + e.getMessage());
            return false;
        }
    }

    public static void UploadFile(String by, String args) {
        Locator fileInput = page.locator(by);
        fileInput.setInputFiles(
                Paths.get(System.getProperty("user.dir") + "/src/test/resources/attachments/mri_energy_logo.png"));
    }

    public static void moveFile(Path oldFilePath, Path newFilePath) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(oldFilePath)) {
            // Loop through each file in the source directory
            for (Path filePath : directoryStream) {
                // Define the target path for the file
                Path targetPath = newFilePath.resolve(filePath.getFileName());
                // Move file to target directory
                Files.move(filePath, targetPath);
                System.out.println("File moved successfully.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Extracts version from application footer and creates version-specific report
     * directory.
     * 
     * @return The sanitized version string
     * @throws IOException if directory creation fails
     */
    public static String version() throws IOException {
        try {
            String systemVersion = page.locator(VERSION_LOCATOR).innerText();
            String sanitizedVersion = systemVersion.replaceAll("[()\\-+.^:, ]", "");

            Path reportPath = Paths.get(REPORT_BASE_PATH, sanitizedVersion);
            loadProps.setProperty("Version", sanitizedVersion);

            if (!Files.exists(reportPath)) {
                Files.createDirectories(reportPath);
                System.out.println("📁 Created version directory: " + sanitizedVersion);
            }

            return sanitizedVersion;
        } catch (Exception e) {
            System.err.println("❌ Error extracting version: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Deletes old report folders based on retention policy.
     * Keeps the current version, cucumber reports, and the last N versions.
     * 
     * @throws IOException if deletion fails
     */
    public static void deleteOldReports() throws IOException {
        if (!isDelete) {
            System.out.println("⏭️ Report cleanup skipped (Remove_Old_Reports=false)");
            return;
        }

        File oldFolder = new File(REPORT_BASE_PATH);

        if (oldFolder.isDirectory()) {
            File[] directories = oldFolder.listFiles(File::isDirectory);

            if (directories != null) {
                int deleteFolderCount = directories.length - folderCount - 1;

                if (deleteFolderCount > 0) {
                    System.out.println("🗑️ Cleaning up old reports (keeping last " + folderCount + " versions)...");

                    for (File directory : directories) {
                        String directoryName = directory.getName();

                        if (!directoryName.equals(loadProps.getProperty("Version")) &&
                                !directoryName.equals(CUCUMBER_REPORTS_FOLDER)) {

                            deleteDirectory(directory.toPath());
                            System.out.println("✅ Deleted old report folder: " + directoryName);
                            deleteFolderCount--;

                            if (deleteFolderCount <= 0) {
                                break;
                            }
                        }
                    }
                    System.out.println("✅ Report cleanup completed");
                } else {
                    System.out.println("⏭️ No old reports to delete");
                }
            }
        }
    }

    public static void deleteDirectory(Path path) throws IOException {
        // Check if the path is a directory
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path entry : stream) {
                    // Recursively delete each entry
                    deleteDirectory(entry);
                }
            }
        }
        // Finally, delete the directory or file
        Files.delete(path);
    }

    /**
     * Click element using smart locator strategy with automatic fallbacks.
     * Tries multiple locator strategies until one succeeds.
     *
     * @param strategies Variable number of locator strategies to try
     */
    public static void clickElementSmart(String... strategies) {
        try {
            System.out.println("🎯 Smart click: Trying " + strategies.length + " strategies...");
            Locator element = SmartLocatorStrategy.findElement(page, strategies);
            element.click();
            System.out.println("✅ Smart click successful");
        } catch (Exception e) {
            System.err.println("❌ Smart click failed: " + e.getMessage());
            throw e;
        }
    }

    // ============================================================
    // SMART LOCATOR METHODS (Auto-fallback to multiple strategies)
    // ============================================================

    /**
     * Enter text using smart locator strategy with automatic fallbacks.
     *
     * @param text       Text to enter
     * @param strategies Variable number of locator strategies to try
     */
    public static void enterTextSmart(String text, String... strategies) {
        if (text != null && !text.isEmpty()) {
            try {
                System.out.println("⌨️ Smart text entry: Trying " + strategies.length + " strategies...");
                Locator element = SmartLocatorStrategy.findElement(page, strategies);
                element.clear();
                element.fill(text);
                System.out.println("✅ Smart text entry successful");
            } catch (Exception e) {
                System.err.println("❌ Smart text entry failed: " + e.getMessage());
                throw e;
            }
        } else {
            System.out.println("⚠️ Skipped text entry - input is null or empty");
        }
    }

    /**
     * Get text using smart locator strategy with automatic fallbacks.
     *
     * @param strategies Variable number of locator strategies to try
     * @return Element's text content
     */
    public static String getTextSmart(String... strategies) {
        try {
            System.out.println("📝 Smart get text: Trying " + strategies.length + " strategies...");
            Locator element = SmartLocatorStrategy.findElement(page, strategies);
            String text = element.innerText();
            System.out.println("✅ Smart get text successful: " + text);
            return text;
        } catch (Exception e) {
            System.err.println("❌ Smart get text failed: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Check if element exists using smart locator strategy.
     *
     * @param strategies Variable number of locator strategies to try
     * @return true if element found and visible, false otherwise
     */
    public static boolean isElementPresentSmart(String... strategies) {
        try {
            System.out.println("🔍 Smart element check: Trying " + strategies.length + " strategies...");
            boolean exists = SmartLocatorStrategy.elementExists(page, strategies);
            System.out.println(exists ? "✅ Element found" : "ℹ️ Element not found");
            return exists;
        } catch (Exception e) {
            System.err.println("⚠️ Smart element check error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if element exists using smart locator strategy.
     * Alias for isElementPresentSmart() - both methods work the same.
     *
     * @param strategies Variable number of locator strategies to try
     * @return true if element found and visible, false otherwise
     */
    public static boolean elementExists(String... strategies) {
        return isElementPresentSmart(strategies);
    }

    // ============================================================
    // LOCATOR-BASED METHODS (Modern Playwright Pattern)
    // ============================================================

    /**
     * Clicks on a Locator element with comprehensive auto-wait.
     * Auto-waits for: attached → visible → stable → enabled.
     * Use this when you have a Locator object from page object methods.
     * Includes 3-attempt retry for stale elements.
     *
     * @param locator The Locator object for the element
     */
    public static void clickOnElement(Locator locator) {
        int maxRetries = 3;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("⏳ [Auto-Wait] Preparing click on locator (attempt " + attempt + "/" + maxRetries + ")");
                
                // Step 1: Wait for attached
                locator.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED)
                    .setTimeout(TimeoutConfig.getDefaultTimeout()));
                
                // Step 2: Wait for visible
                locator.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(TimeoutConfig.getDefaultTimeout()));
                
                // Step 3: Wait for stable
                try {
                    var initialBox = locator.boundingBox();
                    if (initialBox != null) {
                        Thread.sleep(300);
                        var finalBox = locator.boundingBox();
                        if (finalBox == null || Math.abs(initialBox.x - finalBox.x) >= 1) {
                            Thread.sleep(200);
                        }
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                
                // Step 4: Ensure enabled
                int enabledChecks = 0;
                while (!locator.isEnabled() && enabledChecks < 10) {
                    try { Thread.sleep(200); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
                    enabledChecks++;
                }
                
                // Perform click
                locator.click();
                System.out.println("✅ Clicked successfully on locator");
                return;
                
            } catch (Exception e) {
                String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                if (errorMsg.contains("stale") && attempt < maxRetries) {
                    System.out.println("🔄 [Auto-Wait] Stale element, retrying (" + attempt + "/" + maxRetries + ")");
                    try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    continue;
                }
                if (attempt == maxRetries) {
                    System.err.println("❌ Click failed after " + maxRetries + " attempts");
                    throw e;
                }
            }
        }
    }

    /**
     * Enters text into a Locator element with comprehensive auto-wait.
     * Auto-waits for: attached → visible → stable before typing.
     * Clears existing text before entering new text.
     * Includes 3-attempt retry for stale elements.
     *
     * @param locator   The Locator object for the element
     * @param inputText The text to enter
     */
    public static void enterText(Locator locator, String inputText) {
        if (inputText == null || inputText.isEmpty()) {
            System.out.println("⚠️ Skipped entering text - input is null or empty");
            return;
        }
        
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("⏳ [Auto-Wait] Preparing to enter text on locator (attempt " + attempt + "/" + maxRetries + ")");
                
                // Auto-wait for visible and stable
                locator.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(TimeoutConfig.getDefaultTimeout()));
                
                try { Thread.sleep(300); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                
                locator.clear();
                locator.fill(inputText);
                System.out.println("✅ Entered text successfully: " + inputText);
                return;
                
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().toLowerCase().contains("stale") && attempt < maxRetries) {
                    try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    continue;
                }
                if (attempt == maxRetries) {
                    System.err.println("❌ Enter text failed after " + maxRetries + " attempts");
                    throw e;
                }
            }
        }
    }

    /**
     * Selects a dropdown option by visible text on a Locator element.
     * Auto-waits for: attached → visible before selection.
     * Includes 3-attempt retry for stale elements.
     *
     * @param locator The Locator object for the dropdown
     * @param text    The visible text of the option to select
     */
    public static void selectDropDownValueByText(Locator locator, String text) {
        int maxRetries = 3;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("⏳ [Auto-Wait] Preparing dropdown selection on locator (attempt " + attempt + "/" + maxRetries + ")");
                
                // Auto-wait for visible
                locator.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(TimeoutConfig.getDefaultTimeout()));
                
                locator.selectOption(text);
                System.out.println("✅ Selected dropdown option: " + text);
                return;
                
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().toLowerCase().contains("stale") && attempt < maxRetries) {
                    try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    continue;
                }
                if (attempt == maxRetries) {
                    System.err.println("❌ Dropdown selection failed after " + maxRetries + " attempts");
                    throw e;
                }
            }
        }
    }

    /**
     * Gets the text content of a Locator element with comprehensive auto-wait.
     * Auto-waits for: visible → stable before reading text.
     * Includes retry mechanism for stale elements.
     *
     * @param locator The Locator object for the element
     * @return The element's text content
     */
    public static String getElementText(Locator locator) {
        try {
            System.out.println("⏳ [Auto-Wait] Waiting to read text from locator");
            
            // Auto-wait for visible
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(TimeoutConfig.getDefaultTimeout()));
            
            // Wait for stable
            try { Thread.sleep(200); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }

            String elementText = locator.innerText();
            System.out.println("📝 Element text: " + elementText);
            return elementText;
        } catch (Exception e) {
            System.err.println("❌ Error getting text from locator: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Checks if a Locator element is present and visible.
     *
     * @param locator The Locator object for the element
     * @return true if element is visible, false otherwise
     */
    public static boolean isElementPresent(Locator locator) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(3000));
            return locator.isVisible();
        } catch (TimeoutError e) {
            System.out.println("ℹ️ Element not visible");
            return false;
        } catch (Exception e) {
            System.err.println("⚠️ Error checking element presence: " + e.getMessage());
            return false;
        }
    }

    /**
     * Presses a key on a Locator element.
     *
     * @param locator The Locator object for the element
     * @param key     The key to press (e.g., "Enter", "Tab", "Escape")
     */
    public static void pressKey(Locator locator, String key) {
        try {
            System.out.println("⏳ Waiting for element to be visible (Locator)");
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));

            locator.press(key);
            System.out.println("✅ Pressed key: " + key);
        } catch (Exception e) {
            System.err.println("❌ Error pressing key: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Checks/unchecks a checkbox or radio button Locator element.
     *
     * @param locator The Locator object for the checkbox/radio
     */
    public static void checkElement(Locator locator) {
        try {
            System.out.println("⏳ Waiting for element to be visible (Locator)");
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));

            locator.check();
            System.out.println("✅ Checked element");
        } catch (Exception e) {
            System.err.println("❌ Error checking element: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Select dropdown option using smart locator strategy.
     *
     * @param option     Option text to select
     * @param strategies Variable number of locator strategies to try
     */
    public static void selectDropdownSmart(String option, String... strategies) {
        try {
            System.out.println("🔽 Smart dropdown select: Trying " + strategies.length + " strategies...");
            Locator element = SmartLocatorStrategy.findElement(page, strategies);
            element.selectOption(option);
            System.out.println("✅ Smart dropdown select successful: " + option);
        } catch (Exception e) {
            System.err.println("❌ Smart dropdown select failed: " + e.getMessage());
            throw e;
        }
    }

    public String getScreenShotPath(String TestName) {
        try {
            System.out.println("📸 [getScreenShotPath] Starting screenshot capture for: " + TestName);
            System.out.println("   - Current thread: " + Thread.currentThread().getName());
            System.out.println("   - Page instance (from base.page): " + (page != null ? "NOT NULL" : "NULL"));

            // PERMANENT FIX (Feb 12, 2026): Check Screenshots_Mode configuration flag
            // If Screenshots_Mode is false, skip taking screenshot
            Boolean screenshotsEnabled = Boolean
                    .parseBoolean(loadProps.getProperty(loadProps.PropKeys.SCREENSHOTS_MODE));
            System.out.println("   - Screenshots enabled in config: " + screenshotsEnabled);

            if (!screenshotsEnabled) {
                System.out.println("   ⏭️  Skipping screenshot (Screenshots_Mode=false)");
                return null;
            }

            // Validate page instance before screenshot
            if (page == null) {
                System.err.println("   ❌ Cannot take screenshot: Page is null");
                System.err.println("   ❌ HINT: The listener should have updated base.page before calling this method");
                return null;
            }
            System.out.println("   ✅ Page instance is available");

            // Check if page is closed
            if (page.isClosed()) {
                System.err.println("   ❌ Cannot take screenshot: Page is closed");
                return null;
            }
            System.out.println("   ✅ Page is not closed");

            // Check browser connection
            try {
                if (page.context() != null && page.context().browser() != null &&
                        !page.context().browser().isConnected()) {
                    System.err.println("   ❌ Cannot take screenshot: Browser connection lost");
                    return null;
                }
                System.out.println("   ✅ Browser is connected");
            } catch (Exception e) {
                System.err.println("   ❌ Cannot verify browser connection: " + e.getMessage());
                return null;
            }

            String version = loadProps.getProperty(loadProps.PropKeys.VERSION);
            String screenshotDir = System.getProperty("user.dir") + "/MRITestExecutionReports/" +
                    (version != null ? version.replaceAll("[()-+.^:, ]", "") : "default") +
                    "/screenShots/";
            String screenshotPath = screenshotDir + TestName + "_" + utils.timeStamp() + ".png";

            System.out.println("   📁 Screenshot directory: " + screenshotDir);
            System.out.println("   📁 Screenshot path: " + screenshotPath);

            // CRITICAL FIX: Create screenshot directory if it doesn't exist
            try {
                java.nio.file.Path dirPath = java.nio.file.Paths.get(screenshotDir);
                if (!java.nio.file.Files.exists(dirPath)) {
                    java.nio.file.Files.createDirectories(dirPath);
                    System.out.println("   ✅ Created screenshot directory: " + screenshotDir);
                } else {
                    System.out.println("   ✅ Screenshot directory exists");
                }
            } catch (Exception e) {
                System.err.println("   ❌ Failed to create screenshot directory: " + e.getMessage());
                e.printStackTrace();
                return null;
            }

            System.out.println("   📸 Attempting to capture screenshot...");

            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get(screenshotPath))
                    .setFullPage(true));

            System.out.println("   ✅ Screenshot captured successfully!");
            return screenshotPath;
        } catch (Exception e) {
            System.err.println("   ❌ Failed to create screenshot: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}
