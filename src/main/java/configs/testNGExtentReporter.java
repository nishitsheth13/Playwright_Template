package configs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Protocol;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class testNGExtentReporter {

    public static ExtentReports report = null;
    public static ExtentSparkReporter sparkReporter = null;
    public static ExtentTest test;
    static ExtentReports extent;
    static boolean update = false;

    public static ExtentReports extentReportGenerator() throws IOException {
        if (extent != null && update) {
            // Report already initialized and configured
            return extent;
        }

        String folderName = loadProps.getProperty("Version").replaceAll("[()-+.^:,. ]", "");
        Path reportPath = Paths.get(System.getProperty("user.dir") + "/MRITestExecutionReports/", folderName);
        Path htmlReportPath = reportPath.resolve("extentReports/testNGExtentReports/html");
        Path sparkReportPath = reportPath.resolve("extentReports/testNGExtentReports/spark");

        // Create the folders if they do not exist
        if (!Files.exists(htmlReportPath)) {
            Files.createDirectories(htmlReportPath);
            System.out.println("📁 Created HTML report directory: " + htmlReportPath);
        }
        if (!Files.exists(sparkReportPath)) {
            Files.createDirectories(sparkReportPath);
            System.out.println("📁 Created Spark report directory: " + sparkReportPath);
        }

        // Initialize extent reports if not already done
        if (extent == null) {
            extent = new ExtentReports();
            extent.setSystemInfo("QA Name", "Nishit Sheth");
            extent.setSystemInfo("OS", "Windows");
            extent.setSystemInfo("Browser", loadProps.getProperty("Browser"));
            extent.setSystemInfo("Environment", loadProps.getProperty("URL"));
            extent.setSystemInfo("Version", loadProps.getProperty("Version"));
            System.out.println("📊 ExtentReports initialized successfully");
        }

        // Attach reporters only once
        if (!update) {
            // HTML Reporter
            String htmlPath = htmlReportPath + "/extentReport_" + utils.timeStamp() + ".html";
            ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(htmlPath);
            htmlReporter.config().setReportName("MRI Energy Automation Test Report");
            htmlReporter.config().setDocumentTitle("MRI Energy Automation Test");
            htmlReporter.config().setJs("document.getElementsByClassName('logo')[0].style.display='none';");
            extent.attachReporter(htmlReporter);
            System.out.println("📄 HTML Reporter attached: " + htmlPath);

            // Spark Reporter
            String sparkPath = sparkReportPath + "/spark_" + utils.timeStamp() + ".html";
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(sparkPath);
            sparkReporter.config().setDocumentTitle("MRI Energy Automation Test");
            sparkReporter.config().setTimelineEnabled(true);
            sparkReporter.config().setEncoding("utf-8");
            sparkReporter.config().setProtocol(Protocol.HTTPS);
            sparkReporter.config().setJs("document.getElementsByClassName('logo')[0].style.display='none';");
            sparkReporter.config().setReportName("MRI Energy Automation Report");
            sparkReporter.config().setTheme(Theme.DARK);
            extent.attachReporter(sparkReporter);
            System.out.println("⚡ Spark Reporter attached: " + sparkPath);

            update = true;
            System.out.println("✅ All reporters attached successfully");
        }

        return extent;
    }
}
