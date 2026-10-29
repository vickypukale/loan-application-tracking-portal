package com.loanportal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loan_applications")
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String applicationId;

    @Column(nullable = false)
    private String applicantName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private Double loanAmount;

    @Column(nullable = false)
    private String loanType;

    @Column(nullable = false)
    private String currentStatus;

    private String rejectionReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ============================================================
    // SBI Phase 2: Enhanced Underwriting Fields (Roadmap §4 Step 1)
    // ============================================================

    /** CIBIL credit score. SBI threshold: >= 750 for auto-approval. */
    private Integer cibilScore;

    /** Net monthly income in INR (₹). Salaried: min ₹20,000–₹25,000. */
    private Double monthlyIncome;

    /** Sum of all existing monthly EMIs in INR (₹). Used for FOIR calc. */
    private Double existingEmi;

    /** Employment category: SALARIED or SELF_EMPLOYED */
    private String employmentType;

    /** Applicant age. SBI: must be 21–60 years at time of application. */
    private Integer age;

    /** PAN Card number. Format: [A-Z]{5}[0-9]{4}[A-Z]{1} */
    @Column(length = 10)
    private String panNumber;

    /** Loan tenure requested in months (e.g. 12, 24, 60, 120, 240). */
    private Integer tenureMonths;

    /**
     * SBI automated risk engine tag assigned at submission.
     * Values: PRE_APPROVED | MANUAL_REVIEW | HIGH_RISK_EXCEPTION
     */
    private String sbiEligibilityTag;

    /**
     * Calculated FOIR (Fixed Obligation to Income Ratio) in %.
     * Formula: (existingEmi + proposedEMI) / monthlyIncome * 100
     * SBI threshold: must not exceed 50%.
     */
    private Double calculatedFoir;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User applicantUser;

    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoanEvent> events = new ArrayList<>();

    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoanDocument> documents = new ArrayList<>();

    public LoanApplication() {
    }

    public LoanApplication(String applicantName, String email, String phone, Double loanAmount, String loanType) {
        this.applicantName = applicantName;
        this.email = email;
        this.phone = phone;
        this.loanAmount = loanAmount;
        this.loanType = loanType;
        this.currentStatus = "SUBMITTED";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ---- Getters and Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Double getLoanAmount() { return loanAmount; }
    public void setLoanAmount(Double loanAmount) { this.loanAmount = loanAmount; }

    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<LoanEvent> getEvents() { return events; }
    public void setEvents(List<LoanEvent> events) { this.events = events; }

    public void addEvent(LoanEvent event) {
        events.add(event);
        event.setLoanApplication(this);
    }

    public List<LoanDocument> getDocuments() { return documents; }
    public void setDocuments(List<LoanDocument> documents) { this.documents = documents; }

    // ---- SBI Phase 2 Getters and Setters ----

    public Integer getCibilScore() { return cibilScore; }
    public void setCibilScore(Integer cibilScore) { this.cibilScore = cibilScore; }

    public Double getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(Double monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public Double getExistingEmi() { return existingEmi; }
    public void setExistingEmi(Double existingEmi) { this.existingEmi = existingEmi; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }

    public String getSbiEligibilityTag() { return sbiEligibilityTag; }
    public void setSbiEligibilityTag(String sbiEligibilityTag) { this.sbiEligibilityTag = sbiEligibilityTag; }

    public Double getCalculatedFoir() { return calculatedFoir; }
    public void setCalculatedFoir(Double calculatedFoir) { this.calculatedFoir = calculatedFoir; }

    public User getApplicantUser() { return applicantUser; }
    public void setApplicantUser(User applicantUser) { this.applicantUser = applicantUser; }
}
