package configs;

import org.monte.media.Format;
import org.monte.media.Registry;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.monte.media.VideoFormatKeys.*;

/**
 * SCREEN VIDEO RECORDER (Monte Library)
 * <p>
 * ⚠️ IMPORTANT DISTINCTION:
 * This class records your SCREEN as a VIDEO file (.avi format).
 * It does NOT record Playwright browser actions or generate test scripts.
 * <p>
 * TWO DIFFERENT RECORDING TYPES:
 * <p>
 * 1. SCREEN VIDEO RECORDING (this class):
 * - Captures screen as video during test execution
 * - Saves to: MRITestExecutionReports/.../recordings/
 * - Used for: Visual documentation, debugging failures
 * - Controlled by: Recording_Mode=true in configurations.properties
 * - File type: .avi video files
 * <p>
 * 2. BROWSER ACTION RECORDING (Playwright CLI codegen):
 * - Records browser actions as code (clicks, fills, navigation)
 * - Saves to: temp_recording_XXX/recorded-actions.java
 * - Used for: Test script generation
 * - Triggered by: quick-start.bat Option 1 (Record Actions)
 * - File type: .java source files
 * <p>
 * USAGE:
 * - Enable screen recording: Set Recording_Mode=true in
 * configurations.properties
 * - Disable if not needed: Set Recording_Mode=false (saves resources)
 * - Recording starts: browserSelector.setUp() calls recoder.startRecording()
 * - Recording stops: browserSelector.tearDown() calls recoder.stopRecording()
 *
 * @author Framework Team
 * @see browserSelector for integration
 * @see quick-start.bat for browser action recording (codegen)
 */
public class recoder extends ScreenRecorder {
    public static ScreenRecorder screenRecorder;
    public String name;

    public recoder(GraphicsConfiguration cfg, Rectangle captureArea, Format fileFormat,
                   Format screenFormat, Format mouseFormat, Format audioFormat, File movieFolder,
                   String recordingName)
            throws IOException, AWTException {
        super(cfg, captureArea, fileFormat, screenFormat, mouseFormat, audioFormat, movieFolder);
        this.name = recordingName;
    }

    public static void startRecording() throws Exception {
        File file = new File(System.getProperty("user.dir") + "/MRITestExecutionReports/"
                + loadProps.getProperty("Version").replaceAll("[()-+.^:, ]", "") + "/recordings/");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width;
        int height = screenSize.height;

        Rectangle captureSize = new Rectangle(0, 0, width, height);

        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                .getDefaultConfiguration();

        screenRecorder = new recoder(gc, captureSize,
                new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey,
                        ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                        CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 24,
                        FrameRateKey,
                        Rational.valueOf(15), QualityKey, 1.0f, KeyFrameIntervalKey, 15 * 60),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black", FrameRateKey,
                        Rational.valueOf(30)),
                null, file, "TestRecording");

        screenRecorder.start();

    }

    public static void stopRecording() throws Exception {
        screenRecorder.stop();
    }

    @Override
    protected File createMovieFile(Format fileFormat) throws IOException {

        if (!movieFolder.exists()) {
            movieFolder.mkdirs();
        } else if (!movieFolder.isDirectory()) {
            throw new IOException("\"" + movieFolder + "\" is not a directory.");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
        return new File(movieFolder,
                "Recording" + "_" + dateFormat.format(new Date()) + "."
                        + Registry.getInstance().getExtension(fileFormat));

    }

}
