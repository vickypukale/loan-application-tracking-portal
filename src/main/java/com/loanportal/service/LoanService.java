package com.loanportal.service;

import com.loanportal.model.LoanApplication;
import com.loanportal.model.LoanDocument;
import com.loanportal.model.LoanEvent;
import com.loanportal.model.User;
import com.loanportal.repository.LoanApplicationRepository;
import com.loanportal.repository.LoanDocumentRepository;
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
    private final LoanDocumentRepository documentRepository;

    // ============================================================
    // SBI Underwriting Thresholds (Roadmap §2.2 — The 4 SBI Pillars)
    // ============================================================
    private static final int    SBI_CIBIL_THRESHOLD_GOOD    = 750;
    private static final int    SBI_CIBIL_THRESHOLD_MIN     = 650;
    private static final double SBI_FOIR_MAX_PERCENT        = 50.0;
    private static final int    SBI_AGE_MIN                 = 21;
    private static final int    SBI_AGE_MAX                 = 60;
    private static final double SBI_MIN_MONTHLY_INCOME      = 20000.0;

    public LoanService(LoanApplicationRepository loanRepository,
                       LoanEventRepository eventRepository,
                       LoanDocumentRepository documentRepository) {
        this.loanRepository = loanRepository;
        this.eventRepository = eventRepository;
        this.documentRepository = documentRepository;
    }

    // ============================================================
    // Core CRUD Operations
    // ============================================================

    @Transactional
    public LoanApplication createApplication(LoanApplication application) {
        // Auto-generate unique Application ID: LOAN-1001, LOAN-1002, etc.
        long count = loanRepository.count();
        String generatedId = String.format("LOAN-%04d", (1001 + count));
        application.setApplicationId(generatedId);

        application.setCurrentStatus("SUBMITTED");
        application.setCreatedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());

        // SBI Phase 2: Run automated eligibility pre-screen on submission
        if (hasSbiFields(application)) {
            double foir = calculateFoir(application);
            application.setCalculatedFoir(foir);
            String tag = evaluateSbiEligibility(application);
            application.setSbiEligibilityTag(tag);
        }

        // Create initial submission audit event
        String eventNote = buildSubmissionEventNote(application);
        LoanEvent initialEvent = new LoanEvent(application, "SUBMITTED", eventNote);
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

    public List<LoanApplication> getApplicationsByEligibilityTag(String tag) {
        return loanRepository.findBySbiEligibilityTag(tag);
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

    public List<LoanDocument> getDocumentsForApplication(LoanApplication application) {
        return documentRepository.findByLoanApplication(application);
    }

    // ============================================================
    // End-User & Role-Aware Operations
    // ============================================================

    public List<LoanApplication> getApplicationsForUser(User user) {
        if (user == null || user.isAdmin()) {
            return getAllApplications();
        }
        return loanRepository.findByApplicantUserOrderByCreatedAtDesc(user);
    }

    public List<LoanApplication> searchApplicationsForUser(User user, String keyword) {
        if (user == null || user.isAdmin()) {
            return searchApplications(keyword);
        }
        if (keyword == null || keyword.trim().isEmpty()) {
            return getApplicationsForUser(user);
        }
        String trimmed = keyword.trim().toLowerCase();
        return loanRepository.findByApplicantUserOrderByCreatedAtDesc(user).stream()
                .filter(app -> (app.getApplicationId() != null && app.getApplicationId().toLowerCase().contains(trimmed))
                        || (app.getApplicantName() != null && app.getApplicantName().toLowerCase().contains(trimmed))
                        || (app.getLoanType() != null && app.getLoanType().toLowerCase().contains(trimmed)))
                .toList();
    }

    public long countTotalForUser(User user) {
        if (user == null || user.isAdmin()) {
            return countTotal();
        }
        return loanRepository.countByApplicantUser(user);
    }

    public long countByStatusForUser(User user, String status) {
        if (user == null || user.isAdmin()) {
            return countByStatus(status);
        }
        return loanRepository.countByApplicantUserAndCurrentStatus(user, status);
    }

    public boolean canUserViewLoan(User user, LoanApplication loan) {
        if (user == null || user.isAdmin()) {
            return true;
        }
        if (loan.getApplicantUser() == null) {
            return true; // backwards compatibility
        }
        return loan.getApplicantUser().getId().equals(user.getId());
    }

    public boolean canUserUpdateLoan(User user) {
        return user != null && user.isAdmin();
    }

    // ============================================================
    // SBI Phase 2 — Risk Engine Methods (Roadmap §4 Step 3)
    // ============================================================

    /**
     * Calculates FOIR (Fixed Obligation to Income Ratio) as per SBI underwriting.
     *
     * Formula (Roadmap §2.2 Pillar 4):
     *   FOIR = (Existing Monthly EMIs + Proposed Loan EMI) / Net Monthly Income × 100
     *
     * Proposed EMI is approximated as: (loanAmount / tenureMonths).
     * If tenureMonths is not set, defaults to 60 months (5 years).
     *
     * @param app the loan application with financial fields populated
     * @return FOIR percentage (e.g. 35.5 means 35.5%)
     */
    public double calculateFoir(LoanApplication app) {
        double existingEmi = (app.getExistingEmi() != null) ? app.getExistingEmi() : 0.0;
        double monthlyIncome = (app.getMonthlyIncome() != null && app.getMonthlyIncome() > 0)
                ? app.getMonthlyIncome() : 1.0; // Avoid division by zero

        int tenure = (app.getTenureMonths() != null && app.getTenureMonths() > 0)
                ? app.getTenureMonths() : 60;
        double proposedEmi = (app.getLoanAmount() != null) ? (app.getLoanAmount() / tenure) : 0.0;

        return ((existingEmi + proposedEmi) / monthlyIncome) * 100.0;
    }

    /**
     * Evaluates SBI eligibility against the 4 SBI Pillars and returns a risk tag.
     *
     * Rules applied (Roadmap §2.2 & §3.3 Automated Risk Engine):
     *  - CIBIL < 650   → HIGH_RISK_EXCEPTION (auto-reject route)
     *  - Age out of 21–60 range → HIGH_RISK_EXCEPTION
     *  - FOIR > 50%    → HIGH_RISK_EXCEPTION
     *  - Income < ₹20,000 → HIGH_RISK_EXCEPTION
     *  - CIBIL >= 750 AND FOIR <= 40% AND age OK AND income OK → PRE_APPROVED
     *  - All others    → MANUAL_REVIEW
     *
     * @param app the loan application with SBI fields populated
     * @return one of: "PRE_APPROVED", "MANUAL_REVIEW", "HIGH_RISK_EXCEPTION"
     */
    public String evaluateSbiEligibility(LoanApplication app) {
        int cibil = (app.getCibilScore() != null) ? app.getCibilScore() : 0;
        int age = (app.getAge() != null) ? app.getAge() : 0;
        double monthlyIncome = (app.getMonthlyIncome() != null) ? app.getMonthlyIncome() : 0.0;
        double foir = (app.getCalculatedFoir() != null) ? app.getCalculatedFoir() : calculateFoir(app);

        // High-risk rejection conditions
        if (cibil < SBI_CIBIL_THRESHOLD_MIN) {
            return "HIGH_RISK_EXCEPTION";
        }
        if (age < SBI_AGE_MIN || age > SBI_AGE_MAX) {
            return "HIGH_RISK_EXCEPTION";
        }
        if (foir > SBI_FOIR_MAX_PERCENT) {
            return "HIGH_RISK_EXCEPTION";
        }
        if (monthlyIncome < SBI_MIN_MONTHLY_INCOME) {
            return "HIGH_RISK_EXCEPTION";
        }

        // Preferred approval conditions
        if (cibil >= SBI_CIBIL_THRESHOLD_GOOD && foir <= 40.0
                && age >= SBI_AGE_MIN && age <= SBI_AGE_MAX
                && monthlyIncome >= SBI_MIN_MONTHLY_INCOME) {
            return "PRE_APPROVED";
        }

        // All other cases need manual credit analyst review
        return "MANUAL_REVIEW";
    }

    // ============================================================
    // Private Helpers
    // ============================================================

    /** Returns true if the application has SBI financial fields populated. */
    private boolean hasSbiFields(LoanApplication app) {
        return app.getCibilScore() != null
                || app.getMonthlyIncome() != null
                || app.getAge() != null;
    }

    /** Builds a rich initial submission audit event note including SBI tag if available. */
    private String buildSubmissionEventNote(LoanApplication app) {
        StringBuilder sb = new StringBuilder("Loan application initiated and registered into portal.");
        if (app.getSbiEligibilityTag() != null) {
            sb.append(String.format(
                    " SBI Risk Engine Tag: %s. FOIR: %.1f%%.",
                    app.getSbiEligibilityTag(),
                    app.getCalculatedFoir() != null ? app.getCalculatedFoir() : 0.0
            ));
        }
        return sb.toString();
    }
}
