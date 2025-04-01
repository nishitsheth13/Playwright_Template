package configs;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

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


public class utils extends base {

    public static boolean isDelete = Boolean.parseBoolean(loadProps.getProperty("Remove_Old_Reports"));

    public static int folderCount = Integer.parseInt(loadProps.getProperty("Version_Record"));


    public static void clickOnElement(String element) {
        isElementPresent(element);
        page.locator(element).click();
    }

    public static void selectDropDownValueByText(String element, String Text) {
        page.locator(element).click();
    }

    public static void clearText(String by) {
        page.locator(by).clear();
    }

    public static void enterText(String element, String inputText) {
        if (inputText != null && !inputText.isEmpty()) {
            isElementPresent(element);
            clickOnElement(element);
            page.locator(element).fill(inputText);
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

    public static String timeStamp() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
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

    public static String version() throws IOException {
        String path = "";
        /*UpdatingVersionFile*/
        String systemVersion = page.locator("xpath=/html/body/div/footer/div[1]/div[1]").innerText();
        path = systemVersion.replaceAll("[()-+.^:,. ]", "");
        Path reportPath = Paths.get(System.getProperty("user.dir") + "/MRITestExecutionReports/", path);
        loadProps.setProperty("Version", path);
        if (!Files.exists(reportPath)) {
            Files.createDirectories(reportPath);
        }

        return path;
    }

    public static Locator getShadowDomElement(String shadowHost, String targetSelector) {
        // Locate the shadow host
        Locator hostLocator = page.locator(shadowHost);

        // Navigate into the shadow root and locate the target element
        return hostLocator.locator(":scope >> shadow-root >> " + targetSelector);
    }

    public static void deleteOldReports() throws IOException {


        if (isDelete) {
            File oldFolder = new File(System.getProperty("user.dir") + "/MRITestExecutionReports/");

            if (oldFolder.isDirectory()) {
                File[] directories = oldFolder.listFiles(File::isDirectory); // Get only directories

                int deletefoldercount = directories.length - folderCount - 1;
                if (directories != null && deletefoldercount > 0) {
                    for (File directory : directories) {

                        // Check conditions for deletion
                        String directoryName = directory.getName();
                        if (!directoryName.equals(loadProps.getProperty("Version")) &&
                                !directoryName.equals("cucumberExtentReports")) {
                            // Recursively delete the directory
                            deleteDirectory(directory.toPath());
                            deletefoldercount--;
                            if (deletefoldercount <= 0)
                                break;
                        }
                    }
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
