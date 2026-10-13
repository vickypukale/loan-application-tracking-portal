package com.loanportal.service;

import com.loanportal.model.LoanApplication;
import com.loanportal.model.LoanEvent;
import com.loanportal.repository.LoanApplicationRepository;
import com.loanportal.repository.LoanEventRepository;
import org.junit.jupiter.api.BeforeEach;
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

    @Test
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
    void testUpdateStatus_AppendsEventAndUpdatesState() {
        LoanApplication app = loanService.createApplication(
                new LoanApplication("Bob Marley", "bob@example.com", "1234567890", 15000.0, "PERSONAL")
        );

        // Update to APPROVED
        LoanApplication updated = loanService.updateStatus(app.getId(), "APPROVED", "All documents verified and credit score passed", null);
        assertEquals("APPROVED", updated.getCurrentStatus());

        List<LoanEvent> events = loanService.getEventsForApplication(updated);
        assertEquals(2, events.size(), "Should now have 2 events in timeline");
        assertEquals("APPROVED", events.get(1).getEventStage());
    }

    @Test
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
    void testSearchApplications_ByIdAndName() {
        LoanApplication app1 = loanService.createApplication(
                new LoanApplication("David Miller", "david@example.com", "1112223333", 30000.0, "EDUCATION")
        );
        LoanApplication app2 = loanService.createApplication(
                new LoanApplication("Eva Green", "eva@example.com", "4445556666", 45000.0, "PERSONAL")
        );

        // Search by exact ID
        List<LoanApplication> searchById = loanService.searchApplications(app1.getApplicationId());
        assertEquals(1, searchById.size());
        assertEquals("David Miller", searchById.get(0).getApplicantName());

        // Search by partial Name
        List<LoanApplication> searchByName = loanService.searchApplications("Green");
        assertEquals(1, searchByName.size());
        assertEquals(app2.getApplicationId(), searchByName.get(0).getApplicationId());
    }
}
