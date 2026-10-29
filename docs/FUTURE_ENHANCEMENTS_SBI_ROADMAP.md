# State Bank of India (SBI) Domain Integration & Architecture Blueprint
## Phase 2: Post-DevOps MVP Enhancement Roadmap

---

## 1. Executive Summary
This document outlines the architectural specification and implementation blueprint for upgrading the **Loan Application Tracking Portal** with real-world Indian banking criteria modeled after the **State Bank of India (SBI)** retail lending standards. 

This enhancement is scheduled for **Phase 2**, after completing the primary 15-task DevOps CI/CD pipeline (Jenkins, Selenium, Docker, Ansible).

---

## 2. SBI Retail Loan Products & Evaluation Criteria

### 2.1 Major Loan Categories
1. **Home Loan:** Purchase, construction, or renovation of residential property.
2. **Personal Loan:** Unsecured credit for medical, wedding, or personal requirements.
3. **Auto / Car Loan:** Financing for new or pre-owned vehicles.
4. **Education Loan:** Higher education funding in India or abroad.

### 2.2 Core Underwriting Criteria (The 4 SBI Pillars)
1. **Credit Score (CIBIL Score):**
   * Threshold: $\ge 750$ for standard approval and preferential interest rates.
   * Scores $< 650$ automatically route to the **Exceptions / Rejection** queue.
2. **Income & Employment:**
   * **Salaried Applicants:** Minimum 6–12 months in current employment; minimum monthly salary of ₹20,000–₹25,000.
   * **Self-Employed Applicants:** Business operational and profitable for a minimum of 3 years.
3. **Age Parameters:**
   * Applicant age must be between **21 and 60 years** at the time of application.
4. **FOIR (Fixed Obligation to Income Ratio) / Debt-to-Income:**
   * Formula:
     $$\text{FOIR} = \frac{\text{Existing Monthly EMIs} + \text{Proposed Loan EMI}}{\text{Net Monthly Income}} \times 100$$
   * Threshold: Must not exceed **50%** (i.e., total loan obligations must consume less than half of monthly income).

---

## 3. Modular Architecture & Role Blueprint

### 3.1 Authentication & Security Layer
* **User Registration:** 
  * Validate PAN (`[A-Z]{5}[0-9]{4}[A-Z]{1}`) and 12-digit Aadhaar number.
  * Hash passwords with **Argon2id** or **BCrypt** (salt rounds $\ge 12$).
  * PII Masking: Aadhaar stored with first 8 digits masked (`XXXX-XXXX-1234`).
* **Role-Based Access Control (RBAC):**
  * `ROLE_CUSTOMER`: Access to loan submission, KYC document upload, and personal timeline tracking.
  * `ROLE_LOAN_OFFICER`: Worklist queue access, document verification, and forwarding to credit analysis.
  * `ROLE_ADMIN / CREDIT_ANALYST`: Risk score evaluation, FOIR override, approval, and rejection sign-offs.

### 3.2 Customer Workflow
1. **Application Intake:**
   * Multi-step form capturing: Personal Info, Employment Category, Monthly Income, Existing EMIs, Requested Amount, and Tenure.
2. **Secure Document Ingestion:**
   * Document Types: PAN Card, Aadhaar Card, 6-Month Bank Statement, Salary Slips / Form 16.
   * Validation: Validate file magic bytes (PDF header `%PDF`, JPEG header `FF D8 FF`), enforce 5MB maximum limit.
   * Storage: Stored outside public web root with UUID-based keys.
3. **Self-Service Status Tracking:**
   * Live timeline showing transitions: `SUBMITTED` $\rightarrow$ `KYC_VERIFIED` $\rightarrow$ `UNDERWRITING` $\rightarrow$ `SANCTIONED` / `REJECTED`.

### 3.3 Admin & Underwriting Workflow
1. **Application Queue & Aging SLA:**
   * Filterable worklist with alerts for applications pending $> 48$ hours.
2. **Automated Risk Engine:**
   * Pre-screens application against CIBIL, FOIR, Age, and Income rules.
   * Assigns initial recommendation tag: `PRE_APPROVED`, `MANUAL_REVIEW`, or `HIGH_RISK_EXCEPTION`.
3. **Document Verification Viewer:**
   * In-portal document preview via authenticated byte streaming.
   * Review actions: Mark document as `VERIFIED` or `REJECTED` with remarks.
4. **Decisioning & Sanction Generation:**
   * Final approval triggers dynamic generation of an official **Sanction Letter** (PDF format with loan terms, interest rate, and repayment schedule).
   * Rejections mandate an explicit reason code (e.g., *"CIBIL Score below 750"*, *"FOIR exceeds 50%"*).

---

## 4. Phase 2 Implementation Steps

When implementing this after Task 15:
1. **Model Upgrades:** Add `cibilScore`, `monthlyIncome`, `existingEmi`, `employmentType`, `age`, and `panNumber` to `LoanApplication.java`.
2. **KYC Document Entity:** Create `LoanDocument.java` with file path, document type, and verification status.
3. **Service Logic:** Add `calculateFoir()` and `evaluateSbiEligibility()` methods inside `LoanService.java`.
4. **UI Extension:** Update `apply.html` with financial fields and file upload inputs; update `details.html` with risk analysis summary cards.
5. **Selenium Tests:** Add two new test journeys:
   * *Journey A (Eligible):* High CIBIL applicant auto-routes to Underwriting.
   * *Journey B (Ineligible):* Low CIBIL or high FOIR applicant auto-routes to Alerts/Exceptions.
