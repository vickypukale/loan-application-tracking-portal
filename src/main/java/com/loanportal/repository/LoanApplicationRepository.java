package com.loanportal.repository;

import com.loanportal.model.LoanApplication;
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
}
