package com.loanportal.selenium;

import com.loanportal.repository.LoanApplicationRepository;
import com.loanportal.repository.LoanEventRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(ScreenshotOnFailureExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoanPortalSeleniumE2ETest {

    @LocalServerPort
    private int port;

    private static WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @Autowired
    private LoanApplicationRepository loanRepository;

    @Autowired
    private LoanEventRepository eventRepository;

    @BeforeAll
    static void initDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        ScreenshotOnFailureExtension.setDriver(driver);
    }

    @AfterAll
    static void tearDownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Test
    @Order(1)
    @DisplayName("Journey 1: Submit new loan application and verify on dashboard")
    void testJourney1_SubmitApplication_AppearsOnDashboard() {
        driver.get(baseUrl + "/apply");

        // Assert Apply Page loaded
        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pageTitle")));
        assertEquals("Submit Loan Application", pageTitle.getText());

        // Fill Form
        driver.findElement(By.id("applicantName")).sendKeys("Rahul Sharma");
        driver.findElement(By.id("email")).sendKeys("rahul.sharma@example.com");
        driver.findElement(By.id("phone")).sendKeys("9876543210");
        driver.findElement(By.id("loanAmount")).sendKeys("45000");

        Select loanTypeSelect = new Select(driver.findElement(By.id("loanType")));
        loanTypeSelect.selectByValue("PERSONAL");

        // Submit
        driver.findElement(By.id("submitBtn")).click();

        // Verify Redirection to Dashboard with Success Alert
        WebElement successAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("successAlert")));
        assertTrue(successAlert.getText().contains("registered successfully"));

        // Verify Applicant Name in Table
        WebElement table = driver.findElement(By.id("applicationsTable"));
        assertTrue(table.getText().contains("Rahul Sharma"));
        assertTrue(table.getText().contains("SUBMITTED"));
    }

    @Test
    @Order(2)
    @DisplayName("Journey 2: Search filter displays matching records and reset works")
    void testJourney2_SearchFilter_FindsMatchingApplication() {
        driver.get(baseUrl + "/dashboard");

        // Search for Rahul
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchBox")));
        searchBox.clear();
        searchBox.sendKeys("Rahul");
        driver.findElement(By.id("searchBtn")).click();

        // Verify only Rahul is displayed
        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("applicationsTable")));
        assertTrue(table.getText().contains("Rahul Sharma"));

        // Click Reset
        WebElement resetBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("resetBtn")));
        resetBtn.click();

        // Verify Search Box is cleared
        WebElement clearedSearchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchBox")));
        assertEquals("", clearedSearchBox.getAttribute("value"));
    }

    @Test
    @Order(3)
    @DisplayName("Journey 3: Status drill-down and advance stage to DOCUMENT_VERIFIED")
    void testJourney3_DrillDownTimeline_AdvanceStatus() {
        driver.get(baseUrl + "/dashboard");

        // Click Drill-down on first record
        WebElement drillDownBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@id, 'viewDetailsBtn_')]")));
        drillDownBtn.click();

        // Assert Detail Page loaded
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("detailHeading")));
        WebElement applicantName = driver.findElement(By.id("detailApplicantName"));
        assertEquals("Rahul Sharma", applicantName.getText());

        // Update Stage to DOCUMENT_VERIFIED
        Select statusSelect = new Select(driver.findElement(By.id("newStatus")));
        statusSelect.selectByValue("DOCUMENT_VERIFIED");

        driver.findElement(By.id("remarks")).sendKeys("Aadhaar and PAN KYC cards verified successfully.");
        driver.findElement(By.id("updateStatusBtn")).click();

        // Assert Status badge updated
        WebElement currentStatusBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("currentStatusBadge")));
        assertEquals("DOCUMENT_VERIFIED", currentStatusBadge.getText());

        // Assert Event Timeline contains the new event
        WebElement timeline = driver.findElement(By.id("eventTimeline"));
        assertTrue(timeline.getText().contains("DOCUMENT_VERIFIED"));
        assertTrue(timeline.getText().contains("Aadhaar and PAN KYC cards verified successfully."));
    }

    @Test
    @Order(4)
    @DisplayName("Journey 4: Reject application and verify routing into Alerts View")
    void testJourney4_RejectApplication_RoutesToAlertsView() {
        driver.get(baseUrl + "/dashboard");

        // Navigate to details
        WebElement drillDownBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@id, 'viewDetailsBtn_')]")));
        drillDownBtn.click();

        // Select REJECTED
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("newStatus")));
        Select statusSelect = new Select(driver.findElement(By.id("newStatus")));
        statusSelect.selectByValue("REJECTED");

        // Fill Rejection Reason & Remarks
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rejectionReason")));
        driver.findElement(By.id("rejectionReason")).sendKeys("CIBIL Score Below 650");
        driver.findElement(By.id("remarks")).sendKeys("Credit bureau score check failed; applicant does not meet minimum risk criteria.");
        driver.findElement(By.id("updateStatusBtn")).click();

        // Assert Rejection banner is present on details page
        WebElement rejectionBanner = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rejectionBanner")));
        assertTrue(rejectionBanner.getText().contains("CIBIL Score Below 650"));

        // Navigate to /alerts
        driver.get(baseUrl + "/alerts");

        // Assert loan appears in the Alerts table
        WebElement alertsTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("alertsTable")));
        assertTrue(alertsTable.getText().contains("Rahul Sharma"));
        assertTrue(alertsTable.getText().contains("CIBIL Score Below 650"));
    }
}
