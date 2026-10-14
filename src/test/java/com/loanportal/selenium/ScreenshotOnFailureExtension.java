package com.loanportal.selenium;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotOnFailureExtension implements TestWatcher {

    private static WebDriver driver;

    public static void setDriver(WebDriver webDriver) {
        driver = webDriver;
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        if (driver != null) {
            try {
                TakesScreenshot ts = (TakesScreenshot) driver;
                File source = ts.getScreenshotAs(OutputType.FILE);

                Path directory = Paths.get("target", "screenshots");
                if (!Files.exists(directory)) {
                    Files.createDirectories(directory);
                }

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String testName = context.getRequiredTestMethod().getName();
                Path destination = directory.resolve(testName + "_FAILURE_" + timestamp + ".png");

                Files.copy(source.toPath(), destination);
                System.err.println("📸 [SELENIUM] Failure screenshot captured and saved at: " + destination.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("⚠️ [SELENIUM] Failed to capture failure screenshot: " + e.getMessage());
            }
        }
    }
}
