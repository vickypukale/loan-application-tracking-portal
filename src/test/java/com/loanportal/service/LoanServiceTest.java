package com.loanportal.service;

import com.loanportal.model.LoanApplication;
import com.loanportal.model.LoanEvent;
import com.loanportal.repository.LoanApplicationRepository;
import com.loanportal.repository.LoanEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoanServiceTest {

    @Autowired
    private LoanService loanService;

    @Autowired
    private LoanApplicationRepository loanRepository;

    @Autowired
    private LoanEventRepository eventRepository;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        loanRepository.deleteAll();
    }

    // ============================================================
    // Core CRUD Tests
    // ============================================================

    @Test
    @DisplayName("Create application: generates Application ID and initial SUBMITTED event")
    void testCreateApplication_GeneratesApplicationIdAndInitialEvent() {
        LoanApplication app = new LoanApplication("Alice Cooper", "alice@example.com", "9876543210", 50000.0, "HOME");
        LoanApplication created = loanService.createApplication(app);

        assertNotNull(created.getId(), "Database ID should be assigned");
        assertNotNull(created.getApplicationId(), "Application ID must be generated");
        assertTrue(created.getApplicationId().startsWith("LOAN-"), "Application ID should follow LOAN-XXXX format");
        assertEquals("SUBMITTED", created.getCurrentStatus(), "Initial status must be SUBMITTED");

        List<LoanEvent> events = loanService.getEventsForApplication(created);
        assertEquals(1, events.size(), "Initial submission should have 1 event");
        assertEquals("SUBMITTED", events.get(0).getEventStage());
    }

    @Test
    @DisplayName("Update status: appends event and updates state")
    void testUpdateStatus_AppendsEventAndUpdatesState() {
        LoanApplication app = loanService.createApplication(
                new LoanApplication("Bob Marley", "bob@example.com", "1234567890", 15000.0, "PERSONAL")
        );

        LoanApplication updated = loanService.updateStatus(app.getId(), "APPROVED", "All documents verified and credit score passed", null);
        assertEquals("APPROVED", updated.getCurrentStatus());

        List<LoanEvent> events = loanService.getEventsForApplication(updated);
        assertEquals(2, events.size(), "Should now have 2 events in timeline");
        assertEquals("APPROVED", events.get(1).getEventStage());
    }

    @Test
    @DisplayName("Reject application: saves rejection reason and appears in alerts")
    void testRejectApplication_SavesRejectionReason() {
        LoanApplication app = loanService.createApplication(
                new LoanApplication("Charlie Brown", "charlie@example.com", "5551234567", 80000.0, "AUTO")
        );

        LoanApplication rejected = loanService.updateStatus(app.getId(), "REJECTED", "High debt-to-income ratio", "Credit Score Below 650");
        assertEquals("REJECTED", rejected.getCurrentStatus());
        assertEquals("Credit Score Below 650", rejected.getRejectionReason());

        List<LoanApplication> alerts = loanService.getApplicationsByStatus("REJECTED");
        assertEquals(1, alerts.size());
    }

    @Test
    @DisplayName("Search: by application ID and by partial name")
    void testSearchApplications_ByIdAndName() {
        LoanApplication app1 = loanService.createApplication(
                new LoanApplication("David Miller", "david@example.com", "1112223333", 30000.0, "EDUCATION")
        );
        LoanApplication app2 = loanService.createApplication(
                new LoanApplication("Eva Green", "eva@example.com", "4445556666", 45000.0, "PERSONAL")
        );

        List<LoanApplication> searchById = loanService.searchApplications(app1.getApplicationId());
        assertEquals(1, searchById.size());
        assertEquals("David Miller", searchById.get(0).getApplicantName());

        List<LoanApplication> searchByName = loanService.searchApplications("Green");
        assertEquals(1, searchByName.size());
        assertEquals(app2.getApplicationId(), searchByName.get(0).getApplicationId());
    }

    // ============================================================
    // SBI Phase 2 Tests — Roadmap §4 Step 3 & Step 5
    // ============================================================

    @Test
    @DisplayName("SBI FOIR Calculation: correct formula (existingEmi + proposedEmi) / monthlyIncome * 100")
    void testCalculateFoir_CorrectFormula() {
        LoanApplication app = buildSbiApp(780, 60000.0, 5000.0, 35, "SALARIED", 300000.0, 60);
        double foir = loanService.calculateFoir(app);
        // proposedEMI = 300000/60 = 5000, existingEMI = 5000
        // FOIR = (5000 + 5000) / 60000 * 100 = 16.67%
        assertEquals(16.67, foir, 0.01, "FOIR should be approximately 16.67%");
    }

    @Test
    @DisplayName("SBI Eligibility — Journey A (Eligible): CIBIL 800, low FOIR → PRE_APPROVED")
    void testEvaluateSbiEligibility_EligibleApplicant_ReturnsPreApproved() {
        // High CIBIL, good income, low existing EMI, valid age → PRE_APPROVED
        LoanApplication app = buildSbiApp(800, 70000.0, 2000.0, 30, "SALARIED", 500000.0, 60);
        double foir = loanService.calculateFoir(app);
        app.setCalculatedFoir(foir);

        String tag = loanService.evaluateSbiEligibility(app);
        assertEquals("PRE_APPROVED", tag,
                "Applicant with CIBIL=800, FOIR~12%, age=30 should be PRE_APPROVED");
    }

    @Test
    @DisplayName("SBI Eligibility — Journey B: Low CIBIL (600) → HIGH_RISK_EXCEPTION")
    void testEvaluateSbiEligibility_LowCibil_ReturnsHighRisk() {
        // CIBIL below 650 → auto-reject
        LoanApplication app = buildSbiApp(600, 50000.0, 5000.0, 32, "SALARIED", 300000.0, 60);
        double foir = loanService.calculateFoir(app);
        app.setCalculatedFoir(foir);

        String tag = loanService.evaluateSbiEligibility(app);
        assertEquals("HIGH_RISK_EXCEPTION", tag,
                "CIBIL score 600 is below 650 threshold → should be HIGH_RISK_EXCEPTION");
    }

    @Test
    @DisplayName("SBI Eligibility — Journey B: High FOIR (>50%) → HIGH_RISK_EXCEPTION")
    void testEvaluateSbiEligibility_HighFoir_ReturnsHighRisk() {
        // Income 20000, existingEMI 8000, loanAmount 500000/60 = 8333 → FOIR = (8000+8333)/20000*100 = 81.67%
        LoanApplication app = buildSbiApp(720, 20000.0, 8000.0, 35, "SALARIED", 500000.0, 60);
        double foir = loanService.calculateFoir(app);
        app.setCalculatedFoir(foir);
        assertTrue(foir > 50.0, "FOIR should exceed 50% for this test case");

        String tag = loanService.evaluateSbiEligibility(app);
        assertEquals("HIGH_RISK_EXCEPTION", tag,
                "FOIR > 50% should result in HIGH_RISK_EXCEPTION");
    }

    @Test
    @DisplayName("SBI Eligibility — Age outside 21-60 → HIGH_RISK_EXCEPTION")
    void testEvaluateSbiEligibility_InvalidAge_ReturnsHighRisk() {
        LoanApplication app = buildSbiApp(780, 50000.0, 2000.0, 18, "SALARIED", 200000.0, 60);
        double foir = loanService.calculateFoir(app);
        app.setCalculatedFoir(foir);

        String tag = loanService.evaluateSbiEligibility(app);
        assertEquals("HIGH_RISK_EXCEPTION", tag,
                "Age 18 is below SBI minimum age of 21 → should be HIGH_RISK_EXCEPTION");
    }

    @Test
    @DisplayName("SBI Eligibility — Mid-range CIBIL (700), acceptable FOIR → MANUAL_REVIEW")
    void testEvaluateSbiEligibility_MidRangeCibil_ReturnsManualReview() {
        // CIBIL 700 (≥650 but <750), FOIR ~20% → MANUAL_REVIEW (not PRE_APPROVED, not HIGH_RISK)
        LoanApplication app = buildSbiApp(700, 50000.0, 3000.0, 35, "SALARIED", 300000.0, 60);
        double foir = loanService.calculateFoir(app);
        app.setCalculatedFoir(foir);

        String tag = loanService.evaluateSbiEligibility(app);
        assertEquals("MANUAL_REVIEW", tag,
                "CIBIL 700 (≥650, <750) with acceptable FOIR should be MANUAL_REVIEW");
    }

    @Test
    @DisplayName("SBI Integration: createApplication auto-assigns eligibility tag when SBI fields present")
    void testCreateApplication_WithSbiFields_AutoAssignsEligibilityTag() {
        LoanApplication app = buildSbiApp(800, 70000.0, 2000.0, 30, "SALARIED", 500000.0, 60);
        app.setApplicantName("Priya Patel");
        app.setEmail("priya@example.com");
        app.setPhone("9123456789");
        app.setLoanType("HOME");
        app.setPanNumber("ABCDE1234F");

        LoanApplication created = loanService.createApplication(app);

        assertNotNull(created.getSbiEligibilityTag(), "SBI eligibility tag must be auto-assigned");
        assertNotNull(created.getCalculatedFoir(), "FOIR must be calculated");
        assertEquals("PRE_APPROVED", created.getSbiEligibilityTag(),
                "High CIBIL + low FOIR should produce PRE_APPROVED");
        assertTrue(created.getCalculatedFoir() < 40.0, "FOIR should be below 40% for PRE_APPROVED");

        // Verify the initial submission event note includes the SBI tag
        List<LoanEvent> events = loanService.getEventsForApplication(created);
        assertEquals(1, events.size());
        assertTrue(events.get(0).getRemarks().contains("PRE_APPROVED"),
                "Submission event note should mention the SBI eligibility tag");
    }

    @Test
    @DisplayName("SBI Integration: HIGH_RISK_EXCEPTION loan appears in alerts exception queue")
    void testHighRiskApplication_AppearsInAlertQueue() {
        // Create a high-risk application
        LoanApplication app = buildSbiApp(620, 18000.0, 5000.0, 45, "SALARIED", 200000.0, 60);
        app.setApplicantName("Ravi Kumar");
        app.setEmail("ravi@example.com");
        app.setPhone("9000000001");
        app.setLoanType("PERSONAL");

        LoanApplication created = loanService.createApplication(app);
        assertEquals("HIGH_RISK_EXCEPTION", created.getSbiEligibilityTag());

        // Should appear in the high-risk exception list
        List<LoanApplication> highRisk = loanService.getApplicationsByEligibilityTag("HIGH_RISK_EXCEPTION");
        assertFalse(highRisk.isEmpty(), "HIGH_RISK_EXCEPTION application should appear in exception queue");
        assertEquals(created.getId(), highRisk.get(0).getId());
    }

    // ---- Test Data Builder Helper ----

    private LoanApplication buildSbiApp(int cibilScore, double monthlyIncome, double existingEmi,
                                         int age, String employmentType,
                                         double loanAmount, int tenureMonths) {
        LoanApplication app = new LoanApplication();
        app.setCibilScore(cibilScore);
        app.setMonthlyIncome(monthlyIncome);
        app.setExistingEmi(existingEmi);
        app.setAge(age);
        app.setEmploymentType(employmentType);
        app.setLoanAmount(loanAmount);
        app.setTenureMonths(tenureMonths);
        return app;
    }
}
