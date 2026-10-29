package com.loanportal.repository;

import com.loanportal.model.LoanApplication;
import com.loanportal.model.LoanDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SBI Phase 2 — Repository for KYC Document management.
 * Provides queries for fetching and filtering documents by loan or status.
 */
@Repository
public interface LoanDocumentRepository extends JpaRepository<LoanDocument, Long> {

    /** Retrieve all documents for a given loan application. */
    List<LoanDocument> findByLoanApplication(LoanApplication loanApplication);

    /** Retrieve documents of a specific type for a loan application. */
    List<LoanDocument> findByLoanApplicationAndDocumentType(LoanApplication loanApplication, String documentType);

    /** Count pending documents awaiting officer review for a loan. */
    long countByLoanApplicationAndVerificationStatus(LoanApplication loanApplication, String verificationStatus);
}
