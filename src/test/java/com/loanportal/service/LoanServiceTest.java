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
        // Arrange
        LoanApplication app = new LoanApplication("Alice Cooper", "alice@example.com", "9876543210", 50000.0, "HOME");

        // Act
        LoanApplication created = loanService.createApplication(app);

        // Assert
        assertNotNull(created.getId(), "Database ID should be assigned");
        assertNotNull(created.getApplicationId(), "Application ID must be generated");
        assertTrue(created.getApplicationId().startsWith("LOAN-"), "Application ID should follow LOAN-XXXX format");
        assertEquals("SUBMITTED", created.getCurrentStatus(), "Initial status must be SUBMITTED");

        // Verify initial event
        List<LoanEvent> events = loanService.getEventsForApplication(created);
        assertFalse(events.isEmpty(), "Events list should have at least the intake event");
        assertEquals("SUBMITTED", events.get(0).getEventStage(), "Initial event stage must be SUBMITTED");
    }
}
