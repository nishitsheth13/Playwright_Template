package configs;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import pages.login;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class browserSelector extends base {
    public static String brows = loadProps.getProperty("Browser");
    public static String URL = loadProps.getProperty("URL");
    public static boolean Recording = Boolean.parseBoolean(loadProps.getProperty("Recording_Mode"));
    public static boolean Screenshot = Boolean.parseBoolean(loadProps.getProperty("Screenshots_Mode"));
    public static boolean headless = Boolean.parseBoolean(loadProps.getProperty("Headless_Mode"));
    public static Playwright playwright;
    public static Browser browser;
    public static BrowserContext context;

    public static void setUp() throws Exception {
        if (brows.equalsIgnoreCase("chrome")) {
            Playwright playwright = Playwright.create();
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setArgs(Collections.singletonList("--start-maximized"))
                    .setHeadless(headless));

            Path videoDir = Paths.get(System.getProperty("user.dir") + "/MRITestExecutionReports/" + loadProps.getProperty("Version").replaceAll("[()-+.^:, ]", "") + "/recordings/");
            if (!Files.exists(videoDir)) {
                Files.createDirectories(videoDir);
            }
            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setViewportSize(null)
                    .setIgnoreHTTPSErrors(true);
            BrowserContext context = browser.newContext(contextOptions);
            context.setDefaultTimeout(20000);
            context.setDefaultNavigationTimeout(20000);
            page = context.newPage();
            if (Recording && !headless) {
                recoder.startRecording();
            }
            page.navigate(URL);
            utils.version();


        } else if (brows.equalsIgnoreCase("edge")) {
            Playwright playwright = Playwright.create();
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setChannel("msedge")
                    .setArgs(Collections.singletonList("--start-maximized"))
                    .setHeadless(headless));
            Path videoDir = Paths.get(System.getProperty("user.dir") + "/MRITestExecutionReports/" + loadProps.getProperty("Version").replaceAll("[()-+.^:, ]", "") + "/recordings/");
            if (!Files.exists(videoDir)) {
                Files.createDirectories(videoDir);
            }
            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setViewportSize(null)
                    .setIgnoreHTTPSErrors(true);
            BrowserContext context = browser.newContext(contextOptions);
            context.setDefaultTimeout(20000);
            context.setDefaultNavigationTimeout(20000);
            page = context.newPage();
            if (Recording && !headless) {
                recoder.startRecording();
            }

            page.navigate(URL);
            utils.version();
        } else if (brows.equalsIgnoreCase("firefox")) {
            Playwright playwright = Playwright.create();
            Browser browser = playwright.firefox().launch(new BrowserType.LaunchOptions()
                    .setArgs(Collections.singletonList("--start-maximized"))
                    .setHeadless(headless));
            Path videoDir = Paths.get(System.getProperty("user.dir") + "/MRITestExecutionReports/" + loadProps.getProperty("Version").replaceAll("[()-+.^:, ]", "") + "/recordings/");
            if (!Files.exists(videoDir)) {
                Files.createDirectories(videoDir);
            }
            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setViewportSize(null)
                    .setIgnoreHTTPSErrors(true);
            BrowserContext context = browser.newContext(contextOptions);
            context.setDefaultTimeout(20000);
            context.setDefaultNavigationTimeout(20000);
            page = context.newPage();
            if (Recording && !headless) {
                recoder.startRecording();
            }

            page.navigate(URL);
            utils.version();

        } else {
            System.out.println("This Browser is not found in your system.");
        }


    }


    public static void tearDown() throws Exception {
        login.logout();
        if (page != null) {
            page.close();
            if (Recording && !headless) {
                recoder.stopRecording();
            }
            utils.deleteOldReports();
        }
        if (context != null) {
            context.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }

    }


    //For Parallel Execution using Testng XML

    public static void LaunchBrowser(String browserSelection) throws Exception {
        if (browserSelection.equalsIgnoreCase("chrome")) {
//            utils.backupDatabase(DBName, System.getProperty("user.dir")+"/DBBackup/"+DBName+"_"+utils.timeStamp()+".bak");
            Playwright playwright = Playwright.create();
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setArgs(Collections.singletonList("--start-maximized"))
                    .setHeadless(headless));

            Path videoDir = Paths.get(System.getProperty("user.dir") + "/MRITestExecutionReports/" + loadProps.getProperty("Version").replaceAll("[()-+.^:, ]", "") + "/recordings/");
            if (!Files.exists(videoDir)) {
                Files.createDirectories(videoDir);
            }

            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setViewportSize(null)
                    .setIgnoreHTTPSErrors(true);
            BrowserContext context = browser.newContext(contextOptions);
            context.setDefaultTimeout(20000);
            context.setDefaultNavigationTimeout(20000);
            page = context.newPage();
            if (Recording && !headless) {
                recoder.startRecording();
            }

            page.navigate(URL);
            utils.version();
            testNGExtentReporter.extentReportGenerator();
        } else if (browserSelection.equalsIgnoreCase("edge")) {
            Playwright playwright = Playwright.create();
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setChannel("msedge")
                    .setArgs(Collections.singletonList("--start-maximized"))
                    .setHeadless(headless));
            Path videoDir = Paths.get(System.getProperty("user.dir") + "/MRITestExecutionReports/" + loadProps.getProperty("Version").replaceAll("[()-+.^:, ]", "") + "/recordings/");
            if (!Files.exists(videoDir)) {
                Files.createDirectories(videoDir);
            }
            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setViewportSize(null)
                    .setIgnoreHTTPSErrors(true);
            BrowserContext context = browser.newContext(contextOptions);
            context.setDefaultTimeout(20000);
            context.setDefaultNavigationTimeout(20000);

            page = context.newPage();
            if (Recording && !headless) {
                recoder.startRecording();
            }

            page.navigate(URL);
            utils.version();
            testNGExtentReporter.extentReportGenerator();
        } else if (browserSelection.equalsIgnoreCase("firefox")) {
            Playwright playwright = Playwright.create();
            Browser browser = playwright.firefox().launch(new BrowserType.LaunchOptions()
                    .setArgs(Collections.singletonList("--start-maximized"))
                    .setHeadless(headless));
            Path videoDir = Paths.get(System.getProperty("user.dir") + "/MRITestExecutionReports/" + loadProps.getProperty("Version").replaceAll("[()-+.^:, ]", "") + "/recordings/");
            if (!Files.exists(videoDir)) {
                Files.createDirectories(videoDir);
            }
            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setViewportSize(null)
                    .setIgnoreHTTPSErrors(true);
            BrowserContext context = browser.newContext(contextOptions);
            context.setDefaultTimeout(20000);
            context.setDefaultNavigationTimeout(20000);

            page = context.newPage();
            if (Recording && !headless) {
                recoder.startRecording();
            }

            page.navigate(URL);
            utils.version();
            testNGExtentReporter.extentReportGenerator();
        } else {
            System.out.println("This Browser is not found in your system.");
        }

    }

    public static void closeBrowser() throws Exception {
        if (page != null) {
            page.close();
            if (Recording && !headless) {
                recoder.stopRecording();
            }
            utils.deleteOldReports();
        }

        if (context != null) {
            context.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }


}
