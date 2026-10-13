package com.loanportal.repository;

import com.loanportal.model.LoanApplication;
import com.loanportal.model.LoanEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanEventRepository extends JpaRepository<LoanEvent, Long> {
    List<LoanEvent> findByLoanApplicationOrderByEventTimestampAsc(LoanApplication loanApplication);
}
