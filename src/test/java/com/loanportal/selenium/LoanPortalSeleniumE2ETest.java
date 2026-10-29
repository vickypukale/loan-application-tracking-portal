package com.loanportal.selenium;

import com.loanportal.repository.LoanApplicationRepository;
import com.loanportal.repository.LoanEventRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
        options.addArguments("--window-size=1920,1400");

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

    private void safeClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'instant', block: 'center'});", element);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private void loginAsAdmin() {
        driver.get(baseUrl + "/quick-login/admin");
        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }

    private void loginAsUser() {
        driver.get(baseUrl + "/quick-login/user");
        wait.until(ExpectedConditions.urlContains("/my-applications"));
    }

    private void logout() {
        driver.get(baseUrl + "/logout");
        wait.until(ExpectedConditions.urlContains("/login"));
    }

    // ============================================================
    // Original E2E Journey Tests (preserved & enhanced)
    // ============================================================

    @Test
    @Order(1)
    @DisplayName("Journey 1: Submit new loan application and verify on dashboard")
    void testJourney1_SubmitApplication_AppearsOnDashboard() {
        loginAsAdmin();
        driver.get(baseUrl + "/apply");

        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pageTitle")));
        assertEquals("Submit Loan Application", pageTitle.getText());

        // Fill basic fields
        driver.findElement(By.id("applicantName")).sendKeys("Rahul Sharma");
        driver.findElement(By.id("panNumber")).sendKeys("ABCDE1234F");
        driver.findElement(By.id("email")).sendKeys("rahul.sharma@example.com");
        driver.findElement(By.id("phone")).sendKeys("9876543210");
        driver.findElement(By.id("age")).sendKeys("32");

        Select employmentSelect = new Select(driver.findElement(By.id("employmentType")));
        employmentSelect.selectByValue("SALARIED");

        // Loan details
        Select loanTypeSelect = new Select(driver.findElement(By.id("loanType")));
        loanTypeSelect.selectByValue("PERSONAL");
        driver.findElement(By.id("loanAmount")).sendKeys("45000");

        Select tenureSelect = new Select(driver.findElement(By.id("tenureMonths")));
        tenureSelect.selectByValue("60");

        // Financial profile
        driver.findElement(By.id("cibilScore")).sendKeys("750");
        driver.findElement(By.id("monthlyIncome")).sendKeys("50000");
        driver.findElement(By.id("existingEmi")).sendKeys("2000");

        safeClick(driver.findElement(By.id("submitBtn")));

        // Verify success on dashboard
        WebElement successAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("successAlert")));
        assertTrue(successAlert.getText().contains("registered successfully"));

        WebElement table = driver.findElement(By.id("applicationsTable"));
        assertTrue(table.getText().contains("Rahul Sharma"));
        assertTrue(table.getText().contains("SUBMITTED"));
    }

    @Test
    @Order(2)
    @DisplayName("Journey 2: Search filter displays matching records and reset works")
    void testJourney2_SearchFilter_FindsMatchingApplication() {
        loginAsAdmin();
        driver.get(baseUrl + "/dashboard");

        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchBox")));
        searchBox.clear();
        searchBox.sendKeys("Rahul");
        safeClick(driver.findElement(By.id("searchBtn")));

        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("applicationsTable")));
        assertTrue(table.getText().contains("Rahul Sharma"));

        WebElement resetBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("resetBtn")));
        safeClick(resetBtn);

        WebElement clearedSearchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchBox")));
        assertEquals("", clearedSearchBox.getAttribute("value"));
    }

    @Test
    @Order(3)
    @DisplayName("Journey 3: Status drill-down and advance stage to DOCUMENT_VERIFIED")
    void testJourney3_DrillDownTimeline_AdvanceStatus() {
        loginAsAdmin();
        driver.get(baseUrl + "/dashboard");

        WebElement drillDownBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@id, 'viewDetailsBtn_')]")));
        safeClick(drillDownBtn);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("detailHeading")));
        WebElement applicantName = driver.findElement(By.id("detailApplicantName"));
        assertEquals("Rahul Sharma", applicantName.getText());

        Select statusSelect = new Select(driver.findElement(By.id("newStatus")));
        statusSelect.selectByValue("DOCUMENT_VERIFIED");

        driver.findElement(By.id("remarks")).sendKeys("Aadhaar and PAN KYC cards verified successfully.");
        safeClick(driver.findElement(By.id("updateStatusBtn")));

        WebElement currentStatusBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("currentStatusBadge")));
        assertEquals("DOCUMENT_VERIFIED", currentStatusBadge.getText());

        WebElement timeline = driver.findElement(By.id("eventTimeline"));
        assertTrue(timeline.getText().contains("DOCUMENT_VERIFIED"));
        assertTrue(timeline.getText().contains("Aadhaar and PAN KYC cards verified successfully."));
    }

    @Test
    @Order(4)
    @DisplayName("Journey 4: Reject application and verify routing into Alerts View")
    void testJourney4_RejectApplication_RoutesToAlertsView() {
        loginAsAdmin();
        driver.get(baseUrl + "/dashboard");

        WebElement drillDownBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@id, 'viewDetailsBtn_')]")));
        safeClick(drillDownBtn);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("newStatus")));
        Select statusSelect = new Select(driver.findElement(By.id("newStatus")));
        statusSelect.selectByValue("REJECTED");
        ((JavascriptExecutor) driver).executeScript("document.getElementById('newStatus').dispatchEvent(new Event('change'));");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rejectionReason")));
        driver.findElement(By.id("rejectionReason")).sendKeys("CIBIL Score Below 650");
        driver.findElement(By.id("remarks")).sendKeys("Credit bureau score check failed; applicant does not meet minimum risk criteria.");
        safeClick(driver.findElement(By.id("updateStatusBtn")));

        WebElement rejectionBanner = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rejectionBanner")));
        assertTrue(rejectionBanner.getText().contains("CIBIL Score Below 650"));

        driver.get(baseUrl + "/alerts");

        WebElement alertsTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("alertsTable")));
        assertTrue(alertsTable.getText().contains("Rahul Sharma"));
        assertTrue(alertsTable.getText().contains("CIBIL Score Below 650"));
    }

    // ============================================================
    // SBI Phase 2 Selenium Tests — Roadmap §4 Step 5
    // ============================================================

    @Test
    @Order(5)
    @DisplayName("SBI Journey A (Eligible): CIBIL=800, low FOIR → PRE_APPROVED tag on details page")
    void testSbiJourneyA_EligibleApplicant_PreApprovedTag() {
        loginAsAdmin();
        driver.get(baseUrl + "/apply");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pageTitle")));

        // Fill all fields with ELIGIBLE data
        driver.findElement(By.id("applicantName")).sendKeys("Ananya Kapoor");
        driver.findElement(By.id("panNumber")).sendKeys("KAPAA1234B");
        driver.findElement(By.id("email")).sendKeys("ananya.kapoor@example.com");
        driver.findElement(By.id("phone")).sendKeys("9812345678");
        driver.findElement(By.id("age")).sendKeys("35");

        new Select(driver.findElement(By.id("employmentType"))).selectByValue("SALARIED");
        new Select(driver.findElement(By.id("loanType"))).selectByValue("HOME");
        driver.findElement(By.id("loanAmount")).sendKeys("2000000"); // ₹20 Lakhs
        new Select(driver.findElement(By.id("tenureMonths"))).selectByValue("240"); // 20 years

        // High CIBIL, good income, low existing EMI → low FOIR
        driver.findElement(By.id("cibilScore")).sendKeys("820");        // CIBIL: 820 ✅
        driver.findElement(By.id("monthlyIncome")).sendKeys("85000");   // Income: ₹85,000 ✅
        driver.findElement(By.id("existingEmi")).sendKeys("3000");      // Existing EMI: ₹3,000 ✅

        safeClick(driver.findElement(By.id("submitBtn")));

        // Verify success message mentions PRE_APPROVED
        WebElement successAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("successAlert")));
        String alertText = successAlert.getText();
        assertTrue(alertText.contains("registered successfully"),
                "Success message should confirm registration");
        assertTrue(alertText.contains("PRE_APPROVED"),
                "Success message should show PRE_APPROVED tag for eligible applicant");

        // Navigate to details page for this application
        WebElement table = driver.findElement(By.id("applicationsTable"));
        assertTrue(table.getText().contains("Ananya Kapoor"));

        // Click drill-down for Ananya
        WebElement drillDown = driver.findElement(By.xpath("//td[contains(text(),'Ananya Kapoor')]/..//a[contains(@id,'viewDetailsBtn_')]"));
        safeClick(drillDown);

        // Verify SBI Risk Card shows PRE_APPROVED
        WebElement sbiRiskCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sbiRiskCard")));
        assertTrue(sbiRiskCard.getText().contains("PRE_APPROVED"),
                "SBI Risk Analysis Card should display PRE_APPROVED tag");

        // Advance to UNDERWRITING
        Select statusSelect = new Select(driver.findElement(By.id("newStatus")));
        statusSelect.selectByValue("UNDERWRITING");
        driver.findElement(By.id("remarks")).sendKeys("PRE_APPROVED applicant — CIBIL 820, FOIR ~13%. Fast-tracking to underwriting.");
        safeClick(driver.findElement(By.id("updateStatusBtn")));

        WebElement statusBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("currentStatusBadge")));
        assertEquals("UNDERWRITING", statusBadge.getText(),
                "PRE_APPROVED applicant should be advanced to UNDERWRITING");
    }

    @Test
    @Order(6)
    @DisplayName("SBI Journey B (Ineligible): CIBIL=580 → HIGH_RISK_EXCEPTION, routes to Alerts queue")
    void testSbiJourneyB_IneligibleApplicant_HighRiskRoutedToAlerts() {
        loginAsAdmin();
        driver.get(baseUrl + "/apply");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pageTitle")));

        // Fill all fields with INELIGIBLE data (low CIBIL)
        driver.findElement(By.id("applicantName")).sendKeys("Vikram Singh");
        driver.findElement(By.id("panNumber")).sendKeys("SINGV5678X");
        driver.findElement(By.id("email")).sendKeys("vikram.singh@example.com");
        driver.findElement(By.id("phone")).sendKeys("9700000001");
        driver.findElement(By.id("age")).sendKeys("42");

        new Select(driver.findElement(By.id("employmentType"))).selectByValue("SELF_EMPLOYED");
        new Select(driver.findElement(By.id("loanType"))).selectByValue("PERSONAL");
        driver.findElement(By.id("loanAmount")).sendKeys("300000"); // ₹3 Lakhs
        new Select(driver.findElement(By.id("tenureMonths"))).selectByValue("36");

        // LOW CIBIL score → auto HIGH_RISK_EXCEPTION
        driver.findElement(By.id("cibilScore")).sendKeys("580");       // CIBIL: 580 ❌ (<650)
        driver.findElement(By.id("monthlyIncome")).sendKeys("40000");  // Income: ₹40,000
        driver.findElement(By.id("existingEmi")).sendKeys("5000");     // Existing EMI: ₹5,000

        safeClick(driver.findElement(By.id("submitBtn")));

        // Verify success message warns about HIGH RISK
        WebElement successAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("successAlert")));
        String alertText = successAlert.getText();
        assertTrue(alertText.contains("registered successfully"),
                "Success message should confirm registration");
        assertTrue(alertText.contains("HIGH RISK") || alertText.contains("HIGH_RISK_EXCEPTION"),
                "Success message should warn about HIGH RISK flag for ineligible applicant");

        // Navigate to Alerts page — should appear automatically WITHOUT manual rejection
        driver.get(baseUrl + "/alerts");

        WebElement alertsTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("alertsTable")));
        assertTrue(alertsTable.getText().contains("Vikram Singh"),
                "HIGH_RISK_EXCEPTION applicant should automatically appear in Alerts queue");
        assertTrue(alertsTable.getText().contains("HIGH_RISK_EXCEPTION"),
                "Alerts table should display the HIGH_RISK_EXCEPTION tag");

        // Also verify on the details page the high-risk banner is shown
        WebElement reviewBtn = alertsTable.findElement(By.xpath("//td[contains(text(),'Vikram Singh')]/..//a[contains(@id,'viewAlertBtn_')]"));
        safeClick(reviewBtn);

        WebElement highRiskBanner = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("highRiskBanner")));
        assertTrue(highRiskBanner.isDisplayed(),
                "HIGH_RISK_EXCEPTION banner should be visible on details page");
        assertTrue(highRiskBanner.getText().contains("HIGH_RISK_EXCEPTION"),
                "HIGH RISK banner should mention the exception type");
    }

    // ============================================================
    // Role-Based Authentication & Portal E2E Tests
    // ============================================================

    @Test
    @Order(7)
    @DisplayName("Journey 7: End-User (Applicant) Login, submit application, and view personal status & timeline")
    void testJourney7_EndUserLogin_SubmitAndTrackStatus() {
        // 1. Visit Login page and log in as End User
        logout();
        driver.get(baseUrl + "/login");
        WebElement userTab = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-tab")));
        safeClick(userTab);

        driver.findElement(By.id("username")).sendKeys("user");
        driver.findElement(By.id("password")).sendKeys("user123");
        safeClick(driver.findElement(By.id("loginBtn")));

        // 2. Verify redirect to /my-applications and personalized applicant greeting
        wait.until(ExpectedConditions.urlContains("/my-applications"));
        WebElement welcomeHeading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("applicantWelcomeHeading")));
        assertTrue(welcomeHeading.getText().contains("Rahul Sharma"),
                "User portal should welcome the logged-in applicant by name");

        // Assert that Admin Dashboard and Alerts tabs are NOT shown to End User
        assertTrue(driver.findElements(By.id("navDashboard")).isEmpty(),
                "End user navbar must NOT show Admin Dashboard tab");
        assertTrue(driver.findElements(By.id("navAlerts")).isEmpty(),
                "End user navbar must NOT show Alerts tab");

        // 3. Click Apply for New Loan from User Portal
        WebElement applyBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("applyNewLoanBtn")));
        safeClick(applyBtn);

        wait.until(ExpectedConditions.urlContains("/apply"));

        // Name and Email should be auto-filled from user profile
        WebElement nameInput = driver.findElement(By.id("applicantName"));
        assertEquals("Rahul Sharma", nameInput.getAttribute("value"));

        driver.findElement(By.id("panNumber")).sendKeys("ABCDE9876K");
        driver.findElement(By.id("age")).sendKeys("29");
        new Select(driver.findElement(By.id("employmentType"))).selectByValue("SALARIED");
        new Select(driver.findElement(By.id("loanType"))).selectByValue("PERSONAL");
        driver.findElement(By.id("loanAmount")).sendKeys("150000");
        new Select(driver.findElement(By.id("tenureMonths"))).selectByValue("36");
        driver.findElement(By.id("cibilScore")).sendKeys("760");
        driver.findElement(By.id("monthlyIncome")).sendKeys("60000");
        driver.findElement(By.id("existingEmi")).sendKeys("1500");

        safeClick(driver.findElement(By.id("submitBtn")));

        // 4. Verify redirected to /my-applications with success message and new record listed
        wait.until(ExpectedConditions.urlContains("/my-applications"));
        WebElement userTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userApplicationsTable")));
        assertTrue(userTable.getText().contains("Rahul Sharma"));
        assertTrue(userTable.getText().contains("SUBMITTED"));

        // 5. Click View Status & Timeline for this application
        WebElement timelineBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@id, 'trackTimelineBtn_')]")));
        safeClick(timelineBtn);

        wait.until(ExpectedConditions.urlContains("/loans/"));
        WebElement userTrackingCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userTrackingCard")));
        assertTrue(userTrackingCard.isDisplayed(),
                "End-user view should display the read-only Application Stage Tracking card");

        // Verify update form is NOT present for regular applicant
        assertTrue(driver.findElements(By.id("adminUpdateCard")).isEmpty(),
                "End user should NOT have access to the admin stage update form");

        // 6. Log out
        safeClick(driver.findElement(By.id("logoutBtn")));
        wait.until(ExpectedConditions.urlContains("/login"));
    }

    @Test
    @Order(8)
    @DisplayName("Journey 8: Admin (Officer) Login, manage all applications, and update application stage")
    void testJourney8_AdminLogin_ManageApplicationsAndUpdateStage() {
        // 1. Visit Login page and log in as Admin
        logout();
        driver.get(baseUrl + "/login");
        WebElement adminTab = wait.until(ExpectedConditions.elementToBeClickable(By.id("admin-tab")));
        safeClick(adminTab);

        driver.findElement(By.id("adminUsername")).sendKeys("admin");
        driver.findElement(By.id("adminPassword")).sendKeys("admin123");
        safeClick(driver.findElement(By.id("adminLoginBtn")));

        // 2. Verify redirect to /dashboard in Admin Console mode
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        WebElement dashboardHeading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dashboardHeading")));
        assertTrue(dashboardHeading.isDisplayed());

        WebElement adminBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("adminRoleBadge")));
        assertTrue(adminBadge.getText().contains("Admin"));

        // 3. Drill down into application to perform officer update
        WebElement drillDownBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@id, 'viewDetailsBtn_')]")));
        safeClick(drillDownBtn);

        // 4. Verify Admin has the Stage Transition form
        WebElement adminUpdateCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("adminUpdateCard")));
        assertTrue(adminUpdateCard.isDisplayed(),
                "Admin should have access to the stage update card");

        Select statusSelect = new Select(driver.findElement(By.id("newStatus")));
        statusSelect.selectByValue("APPROVED");
        driver.findElement(By.id("remarks")).sendKeys("Comprehensive underwriting check completed. Approved by Admin Officer.");
        safeClick(driver.findElement(By.id("updateStatusBtn")));

        WebElement statusBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("currentStatusBadge")));
        assertEquals("APPROVED", statusBadge.getText());

        // 5. Verify Admin can access Alerts & Exceptions queue
        driver.get(baseUrl + "/alerts");
        WebElement alertsHeading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("alertsHeading")));
        assertTrue(alertsHeading.isDisplayed(), "Admin should be able to access the Alerts & Exceptions queue");

        // 6. Clean up: Log out
        safeClick(driver.findElement(By.id("logoutBtn")));
        wait.until(ExpectedConditions.urlContains("/login"));
    }

    @Test
    @Order(9)
    @DisplayName("Journey 9: Before login, unauthenticated visitor cannot access dashboard, apply, or alerts tabs")
    void testJourney9_UnauthenticatedAccess_BlockedAndNoTabsShown() {
        driver.get(baseUrl + "/logout");
        wait.until(ExpectedConditions.urlContains("/login"));

        // 1. Assert that login page has NO internal app navigation tabs or loan content
        assertTrue(driver.findElements(By.id("navDashboard")).isEmpty(),
                "Unauthenticated login page must NOT show Dashboard tab");
        assertTrue(driver.findElements(By.id("navMyApplications")).isEmpty(),
                "Unauthenticated login page must NOT show My Applications tab");
        assertTrue(driver.findElements(By.id("navAlerts")).isEmpty(),
                "Unauthenticated login page must NOT show Alerts tab");
        assertTrue(driver.findElements(By.id("applicationsTable")).isEmpty(),
                "Unauthenticated login page must NOT show loan records table");
        assertTrue(driver.findElements(By.id("kpiIndicatorsRow")).isEmpty(),
                "Unauthenticated login page must NOT show KPI indicator cards");

        // 2. Direct attempt to access /dashboard must redirect to /login
        driver.get(baseUrl + "/dashboard");
        wait.until(ExpectedConditions.urlContains("/login"));
        WebElement errorAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("authErrorAlert")));
        assertTrue(errorAlert.getText().contains("Please log in"),
                "Redirected page must require login");

        // 3. Direct attempt to access /apply must redirect to /login
        driver.get(baseUrl + "/apply");
        wait.until(ExpectedConditions.urlContains("/login"));

        // 4. Direct attempt to access /alerts must redirect to /login
        driver.get(baseUrl + "/alerts");
        wait.until(ExpectedConditions.urlContains("/login"));

        // 5. Direct attempt to access /my-applications must redirect to /login
        driver.get(baseUrl + "/my-applications");
        wait.until(ExpectedConditions.urlContains("/login"));
    }
}
