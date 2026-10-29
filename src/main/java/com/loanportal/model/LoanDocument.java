package com.loanportal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * SBI Phase 2 — Roadmap §4 Step 2: KYC Document Entity
 *
 * Represents a KYC document (PAN, Aadhaar, Bank Statement, Salary Slip, Form 16)
 * uploaded by the applicant. Files are stored OUTSIDE the public web root,
 * identified by a UUID-based key. Verification is performed by a Loan Officer.
 */
@Entity
@Table(name = "loan_documents")
public class LoanDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private LoanApplication loanApplication;

    /**
     * Type of KYC document.
     * Allowed values: PAN_CARD | AADHAAR_CARD | BANK_STATEMENT | SALARY_SLIP | FORM_16
     */
    @Column(nullable = false)
    private String documentType;

    /**
     * Secure file path using UUID-based key, stored outside public web root.
     * Example: ./uploads/kyc-documents/550e8400-e29b-41d4-a716-446655440000.pdf
     */
    @Column(nullable = false)
    private String filePath;

    /** Original file name uploaded by the applicant. */
    private String originalFileName;

    /**
     * Document verification status.
     * Values: PENDING | VERIFIED | REJECTED
     */
    @Column(nullable = false)
    private String verificationStatus = "PENDING";

    /** Remarks added by the Loan Officer during document review. */
    private String verificationRemarks;

    /** Timestamp when the document was uploaded. */
    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    /** Timestamp when the document was last reviewed by an officer. */
    private LocalDateTime reviewedAt;

    public LoanDocument() {
        this.uploadedAt = LocalDateTime.now();
        this.verificationStatus = "PENDING";
    }

    public LoanDocument(LoanApplication loanApplication, String documentType,
                        String filePath, String originalFileName) {
        this.loanApplication = loanApplication;
        this.documentType = documentType;
        this.filePath = filePath;
        this.originalFileName = originalFileName;
        this.uploadedAt = LocalDateTime.now();
        this.verificationStatus = "PENDING";
    }

    // ---- Getters and Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LoanApplication getLoanApplication() { return loanApplication; }
    public void setLoanApplication(LoanApplication loanApplication) { this.loanApplication = loanApplication; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getVerificationRemarks() { return verificationRemarks; }
    public void setVerificationRemarks(String verificationRemarks) { this.verificationRemarks = verificationRemarks; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
