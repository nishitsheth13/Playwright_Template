@echo off
REM Set MAVEN_OPTS to suppress sun.misc.Unsafe warnings from Guice
REM These warnings come from Maven's internal Guice library, not from project code

setx MAVEN_OPTS "--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-exports java.base/sun.misc=ALL-UNNAMED -XX:+IgnoreUnrecognizedVMOptions -Djava.util.logging.config.file=logging.properties" /M

echo.
echo ================================================================
echo Maven environment variables updated successfully!
echo ================================================================
echo.
echo The following MAVEN_OPTS have been set:
echo   --add-opens java.base/java.lang=ALL-UNNAMED
echo   --add-opens java.base/java.util=ALL-UNNAMED
echo   --add-opens java.base/sun.nio.ch=ALL-UNNAMED
echo   --add-exports java.base/sun.misc=ALL-UNNAMED
echo   -XX:+IgnoreUnrecognizedVMOptions
echo   -Djava.util.logging.config.file=logging.properties
echo.
echo ⚠️  NOTE: You may need to restart your terminal/IDE for changes to take effect.
echo ⚠️  NOTE: Run this script as Administrator if you want system-wide settings.
echo.
echo Press any key to exit...
pause > nul

