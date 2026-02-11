package configs;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;

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
    public static int folderCount = Integer.parseInt(loadProps.getProperty("Version_Record") != null ? loadProps.getProperty("Version_Record") : "5");

    /**
     * Clicks on an element after automatically waiting for its visibility.
     * Includes built-in retry logic and detailed logging.
     *
     * @param element The locator string for the element
     */
    public static void clickOnElement(String element) {
        try {
            System.out.println("⏳ Waiting for element to be visible: " + element);
            // Auto-wait for element to be visible (default timeout from Playwright config)
            page.locator(element).waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));

            page.locator(element).click();
            System.out.println("✅ Clicked on element: " + element);
        } catch (TimeoutError e) {
            System.err.println("❌ Timeout waiting for element to be visible: " + element);
            System.err.println("   Check if element exists, or increase timeout in configurations.properties");
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Error clicking element: " + element + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Selects a dropdown option by visible text after waiting for element visibility.
     *
     * @param element The locator string for the dropdown element
     * @param text    The visible text of the option to select
     */
    public static void selectDropDownValueByText(String element, String text) {
        try {
            System.out.println("⏳ Waiting for dropdown to be visible: " + element);
            // Auto-wait for element to be visible
            page.locator(element).waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));

            page.locator(element).selectOption(text);
            System.out.println("✅ Selected dropdown option: " + text);
        } catch (Exception e) {
            System.err.println("❌ Error selecting dropdown option: " + element + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Clears text from an element after waiting for visibility.
     *
     * @param element The locator string for the element
     */
    public static void clearText(String element) {
        try {
            System.out.println("⏳ Waiting for element to be visible: " + element);
            // Auto-wait for element to be visible
            page.locator(element).waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));

            page.locator(element).clear();
            System.out.println("✅ Cleared text from element: " + element);
        } catch (Exception e) {
            System.err.println("❌ Error clearing text from element: " + element + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Enters text into an element after automatically waiting for visibility.
     * Clears existing text before entering new text.
     *
     * @param element   The locator string for the element
     * @param inputText The text to enter
     */
    public static void enterText(String element, String inputText) {
        if (inputText != null && !inputText.isEmpty()) {
            try {
                System.out.println("⏳ Waiting for element to be visible: " + element);
                // Auto-wait for element to be visible
                page.locator(element).waitFor(new Locator.WaitForOptions()
                        .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));

                page.locator(element).clear();
                page.locator(element).fill(inputText);
                System.out.println("✅ Entered text in element: " + element);
            } catch (Exception e) {
                System.err.println("❌ Error entering text in element: " + element + " - " + e.getMessage());
                throw e;
            }
        } else {
            System.out.println("⚠️ Skipped entering text - input is null or empty");
        }
    }

    /**
     * Clears and enters text into an element after automatically waiting for visibility.
     *
     * @param element   The locator string for the element
     * @param inputText The text to enter
     */
    public static void clearAndEnterText(String element, String inputText) {
        if (inputText != null && !inputText.isEmpty()) {
            try {
                System.out.println("⏳ Waiting for element to be visible: " + element);
                // Auto-wait for element to be visible
                page.locator(element).waitFor(new Locator.WaitForOptions()
                        .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));

                page.locator(element).clear();
                page.locator(element).fill(inputText);
                System.out.println("✅ Cleared and entered text in element: " + element);
            } catch (Exception e) {
                System.err.println("❌ Error entering text in element: " + element + " - " + e.getMessage());
                throw e;
            }
        } else {
            System.out.println("⚠️ Skipped entering text - input is null or empty");
        }
    }

    /**
     * Gets the text content of an element after waiting for visibility.
     *
     * @param element The locator string for the element
     * @return The element's text content
     */
    public static String getElementText(String element) {
        try {
            System.out.println("⏳ Waiting for element to be visible: " + element);
            // Auto-wait for element to be visible
            page.locator(element).waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));

            String elementText = page.locator(element).innerText();
            System.out.println("📝 Element text: " + elementText);
            return elementText;
        } catch (Exception e) {
            System.err.println("❌ Error getting text from element: " + element + " - " + e.getMessage());
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
            // Validate page instance before screenshot
            if (page == null) {
                System.out.println("⚠️ Cannot take screenshot: Page is null");
                return null;
            }

            // Check if page is closed
            if (page.isClosed()) {
                System.out.println("⚠️ Cannot take screenshot: Page is closed");
                return null;
            }

            // Check browser connection
            try {
                if (page.context() != null && page.context().browser() != null &&
                        !page.context().browser().isConnected()) {
                    System.out.println("⚠️ Cannot take screenshot: Browser connection lost");
                    return null;
                }
            } catch (Exception e) {
                System.out.println("⚠️ Cannot verify browser connection: " + e.getMessage());
                return null;
            }

            String version = loadProps.getProperty("Version");
            String screenshotPath = System.getProperty("user.dir") + "/MRITestExecutionReports/" +
                    (version != null ? version.replaceAll("[()-+.^:, ]", "") : "default") +
                    "/screenShots/" + TestName + "_" + utils.timeStamp() + ".png";

            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get(screenshotPath))
                    .setFullPage(true));

            System.out.println("📸 Screenshot saved: " + screenshotPath);
            return screenshotPath;
        } catch (Exception e) {
            System.out.println("⚠️ Failed to create screenshot: " + e.getMessage());
            return null;
        }
    }

}
