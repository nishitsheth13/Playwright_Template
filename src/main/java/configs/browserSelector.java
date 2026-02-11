package configs;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class browserSelector extends base {
    public static String brows = loadProps.getProperty("Browser");
    public static String URL = loadProps.getProperty("URL");
    public static boolean Recording = Boolean.parseBoolean(loadProps.getProperty("Recording_Mode"));
    public static boolean headless = Boolean.parseBoolean(loadProps.getProperty("Headless_Mode"));
    public static boolean traceLog = Boolean.parseBoolean(loadProps.getProperty("TraceLog"));
    public static Playwright playwright;
    public static Browser browser;
    public static BrowserContext context;

    public static void setUp() throws Exception {
        playwright = Playwright.create();

        if (brows.equalsIgnoreCase("chrome")) {
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setArgs(Collections.singletonList("--start-maximized"))
                    .setHeadless(headless));
        } else if (brows.equalsIgnoreCase("edge")) {
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setChannel("msedge")
                    .setArgs(Collections.singletonList("--start-maximized"))
                    .setHeadless(headless));
        } else if (brows.equalsIgnoreCase("firefox")) {
            browser = playwright.firefox().launch(new BrowserType.LaunchOptions()
                    .setArgs(Collections.singletonList("--start-maximized"))
                    .setHeadless(headless));
        } else {
            System.out.println("This Browser is not found in your system.");
            return;
        }

        String version = loadProps.getProperty("Version");
        Path videoDir = Paths.get(System.getProperty("user.dir") + "/MRITestExecutionReports/"
                + (version != null ? version.replaceAll("[()-+.^:, ]", "") : "default") + "/recordings/");
        if (!Files.exists(videoDir)) {
            Files.createDirectories(videoDir);
        }

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(null)
                .setIgnoreHTTPSErrors(true);

        context = browser.newContext(contextOptions);
        context.setDefaultTimeout(20000);
        context.setDefaultNavigationTimeout(20000);

        if (traceLog) {
            context.tracing().start(new com.microsoft.playwright.Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true));
        }

        page = context.newPage();

        if (Recording && !headless) {
            recoder.startRecording();
        }

        page.navigate(URL);
        utils.version();
    }

    public static void tearDown() throws Exception {
        if (page != null) {
            page.close();
            if (Recording && !headless) {
                recoder.stopRecording();
            }
            utils.deleteOldReports();
        }

        if (context != null) {
            try {
                if (traceLog) {
                    String version = loadProps.getProperty("Version");
                    context.tracing().stop(new com.microsoft.playwright.Tracing.StopOptions()
                            .setPath(Paths.get(System.getProperty("user.dir") + "/MRITestExecutionReports/" +
                                    (version != null ? version.replaceAll("[()-+.^:, ]", "") : "default") + "/trace.zip")));
                }
                context.close();
            } catch (Exception e) {
                System.out.println("⚠️ Error closing context: " + e.getMessage());
            }
        }

        if (browser != null) {
            try {
                if (browser.isConnected()) {
                    browser.close();
                }
            } catch (Exception e) {
                System.out.println("⚠️ Error closing browser: " + e.getMessage());
            }
        }

        if (playwright != null) {
            try {
                playwright.close();
            } catch (Exception e) {
                System.out.println("⚠️ Error closing playwright: " + e.getMessage());
            }
        }
    }
}
