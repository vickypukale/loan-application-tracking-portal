package com.loanportal.repository;

import com.loanportal.model.LoanApplication;
import com.loanportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    Optional<LoanApplication> findByApplicationId(String applicationId);
    List<LoanApplication> findByApplicantNameContainingIgnoreCase(String applicantName);
    List<LoanApplication> findByCurrentStatus(String currentStatus);
    long countByCurrentStatus(String currentStatus);

    /** SBI Phase 2: Find applications by automated risk engine tag. */
    List<LoanApplication> findBySbiEligibilityTag(String sbiEligibilityTag);

    /** Role-based End-User filtering */
    List<LoanApplication> findByApplicantUserOrderByCreatedAtDesc(User applicantUser);
    List<LoanApplication> findByApplicantUserAndCurrentStatus(User applicantUser, String currentStatus);
    long countByApplicantUser(User applicantUser);
    long countByApplicantUserAndCurrentStatus(User applicantUser, String currentStatus);
}
