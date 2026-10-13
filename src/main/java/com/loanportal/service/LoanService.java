package com.loanportal.service;

import com.loanportal.model.LoanApplication;
import com.loanportal.model.LoanEvent;
import com.loanportal.repository.LoanApplicationRepository;
import com.loanportal.repository.LoanEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LoanService {

    private final LoanApplicationRepository loanRepository;
    private final LoanEventRepository eventRepository;

    public LoanService(LoanApplicationRepository loanRepository, LoanEventRepository eventRepository) {
        this.loanRepository = loanRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public LoanApplication createApplication(LoanApplication application) {
        // Auto-generate unique Application ID: LOAN-1001, LOAN-1002, etc.
        long count = loanRepository.count();
        String generatedId = String.format("LOAN-%04d", (1001 + count));
        application.setApplicationId(generatedId);

        application.setCurrentStatus("SUBMITTED");
        application.setCreatedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());

        // Create initial submission audit event
        LoanEvent initialEvent = new LoanEvent(application, "SUBMITTED", "Loan application initiated and registered into portal.");
        application.addEvent(initialEvent);

        return loanRepository.save(application);
    }

    public List<LoanApplication> getAllApplications() {
        return loanRepository.findAll();
    }

    public Optional<LoanApplication> getApplicationById(Long id) {
        return loanRepository.findById(id);
    }

    public Optional<LoanApplication> getApplicationByAppId(String applicationId) {
        return loanRepository.findByApplicationId(applicationId);
    }

    public List<LoanApplication> searchApplications(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllApplications();
        }
        String trimmed = keyword.trim();
        Optional<LoanApplication> byId = loanRepository.findByApplicationId(trimmed);
        if (byId.isPresent()) {
            return List.of(byId.get());
        }
        return loanRepository.findByApplicantNameContainingIgnoreCase(trimmed);
    }

    public List<LoanApplication> getApplicationsByStatus(String status) {
        return loanRepository.findByCurrentStatus(status);
    }

    public long countTotal() {
        return loanRepository.count();
    }

    public long countByStatus(String status) {
        return loanRepository.countByCurrentStatus(status);
    }

    @Transactional
    public LoanApplication updateStatus(Long id, String newStatus, String remarks, String rejectionReason) {
        LoanApplication app = loanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Loan application not found with ID: " + id));

        app.setCurrentStatus(newStatus);
        app.setUpdatedAt(LocalDateTime.now());
        if ("REJECTED".equalsIgnoreCase(newStatus)) {
            app.setRejectionReason(rejectionReason);
        }

        LoanEvent statusEvent = new LoanEvent(app, newStatus, remarks);
        app.addEvent(statusEvent);

        return loanRepository.save(app);
    }

    public List<LoanEvent> getEventsForApplication(LoanApplication application) {
        return eventRepository.findByLoanApplicationOrderByEventTimestampAsc(application);
    }
}
