package configs;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;

import java.awt.*;
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
    public static int folderCount = Integer.parseInt(loadProps.getProperty("Version_Record"));


    /**
     * Clicks on an element after verifying its visibility.
     * 
     * @param element The locator string for the element
     */
    public static void clickOnElement(String element) {
        try {
            if (isElementPresent(element)) {
                page.locator(element).click();
                System.out.println("✅ Clicked on element: " + element);
            } else {
                System.err.println("❌ Element not visible: " + element);
            }
        } catch (TimeoutError e) {
            System.err.println("❌ Timeout waiting for element: " + element);
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Error clicking element: " + element + " - " + e.getMessage());
            throw e;
        }
    }

    public static void selectDropDownValueByText(String element, String Text) {
        page.locator(element).click();
    }

    public static void clearText(String by) {
        page.locator(by).clear();
    }

    /**
     * Enters text into an element after clearing and clicking it.
     * 
     * @param element The locator string for the element
     * @param inputText The text to enter
     */
    public static void enterText(String element, String inputText) {
        if (inputText != null && !inputText.isEmpty()) {
            try {
                isElementPresent(element);
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

    public static String getText(String element) {

        String elementText = page.locator(element).innerText();
        System.out.println("Visible text: " + elementText);
        return elementText;
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
     * Generates a timestamp string for file naming.
     * 
     * @return Formatted timestamp string (yyyy_MM_dd_HH_mm)
     */
    public static String timeStamp() {
        DateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static boolean isElementPresent(String element) {
        return page.locator(element).isVisible();

    }

    public static void UploadFile(String by, String args) {
        Locator fileInput = page.locator(by);
        fileInput.setInputFiles(Paths.get(System.getProperty("user.dir") + "/src/test/resources/attachments/mri_energy_logo.png"));
    }

    public static void moveFile(Path oldFilePath, Path newFilePath) {
        try {
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(oldFilePath);

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
     * Extracts version from application footer and creates version-specific report directory.
     * 
     * @return The sanitized version string
     * @throws IOException if directory creation fails
     */
    public static String version() throws IOException {
        try {
            String systemVersion = page.locator(VERSION_LOCATOR).innerText();
            String sanitizedVersion = systemVersion.replaceAll("[()-+.^:,. ]", "");
            
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

    public static Locator getShadowDomElement(String shadowHost, String targetSelector) {
        // Locate the shadow host
        Locator hostLocator = page.locator(shadowHost);

        // Navigate into the shadow root and locate the target element
        return hostLocator.locator(":scope >> shadow-root >> " + targetSelector);
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

//    public static void deleteOldBackup(String backupPath) throws IOException {
//
//
//        if (isDelete) {
//            File oldFolder = new File(System.getProperty("user.dir") + "/DBbackup/");
//
//            if (oldFolder.isDirectory()) {
//                File[] directories = oldFolder.listFiles(File::isFile); // Get only directories
//
//                int deletefoldercount = directories.length - DBCount;
//                if (directories != null && deletefoldercount > 0) {
//                    for (File file : directories) {
//
//                        // Check conditions for deletion
//                        String directoryName = file.getName();
//                        System.out.println(directoryName);
//                        if (deletefoldercount > 0 && !file.equals(backupPath)) {
//                            // Recursively delete the directory
//                            deleteDirectory(file.toPath());
//                            deletefoldercount--;
//                            if (deletefoldercount <= 0)
//                                break;
//                        }
//                    }
//                }
//            }
//        }
//
//    }


    public String getScreenShotPath(String TestName) throws IOException, AWTException {
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get(System.getProperty("user.dir") + "/MRITestExecutionReports/" + loadProps.getProperty("Version").replaceAll("[()-+.^:, ]", "") + "/screenShots/" + TestName + "_" + utils.timeStamp() + ".png"))
                .setFullPage(true));

        String destPath = "/MRITestExecutionReports/" + loadProps.getProperty("Version").replaceAll("[()-+.^:, ]", "") + "/screenShots/" + TestName + "_" + utils.timeStamp() + ".png";

        return destPath;
    }

}
