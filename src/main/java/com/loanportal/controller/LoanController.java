package com.loanportal.controller;

import com.loanportal.model.LoanApplication;
import com.loanportal.model.User;
import com.loanportal.service.LoanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    private User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    @GetMapping({"/", "/dashboard"})
    public String viewDashboard(@RequestParam(value = "search", required = false) String search,
                                HttpSession session,
                                Model model) {
        User currentUser = getCurrentUser(session);

        // If logged in as regular applicant (End User), route directly to personal applications portal
        if (currentUser != null && currentUser.isUser()) {
            return "redirect:/my-applications";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAdmin", currentUser != null && currentUser.isAdmin());
        model.addAttribute("applications", loanService.searchApplications(search));
        model.addAttribute("searchQuery", search != null ? search : "");
        model.addAttribute("totalCount", loanService.countTotal());
        model.addAttribute("submittedCount", loanService.countByStatus("SUBMITTED"));
        model.addAttribute("approvedCount", loanService.countByStatus("APPROVED"));
        model.addAttribute("rejectedCount", loanService.countByStatus("REJECTED"));
        // SBI Phase 2: risk engine count stats for dashboard
        model.addAttribute("preApprovedCount", loanService.getApplicationsByEligibilityTag("PRE_APPROVED").size());
        model.addAttribute("highRiskCount", loanService.getApplicationsByEligibilityTag("HIGH_RISK_EXCEPTION").size());
        return "dashboard";
    }

    /**
     * Dedicated End-User Portal: Borrowers view and track their own applications.
     */
    @GetMapping("/my-applications")
    public String viewMyApplications(@RequestParam(value = "search", required = false) String search,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to view and track your applications.");
            return "redirect:/login";
        }

        // If an admin accidentally accesses /my-applications, route to the full admin dashboard
        if (currentUser.isAdmin()) {
            return "redirect:/dashboard";
        }

        List<LoanApplication> userApps = loanService.searchApplicationsForUser(currentUser, search);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("applications", userApps);
        model.addAttribute("searchQuery", search != null ? search : "");
        model.addAttribute("totalCount", loanService.countTotalForUser(currentUser));
        model.addAttribute("submittedCount", loanService.countByStatusForUser(currentUser, "SUBMITTED"));
        model.addAttribute("approvedCount", loanService.countByStatusForUser(currentUser, "APPROVED"));
        model.addAttribute("rejectedCount", loanService.countByStatusForUser(currentUser, "REJECTED"));
        return "user-applications";
    }

    @GetMapping("/apply")
    public String showApplyForm(HttpSession session, Model model) {
        User currentUser = getCurrentUser(session);
        LoanApplication app = new LoanApplication();

        // If an applicant is logged in, auto-fill applicant information
        if (currentUser != null) {
            app.setApplicantName(currentUser.getFullName());
            app.setEmail(currentUser.getEmail());
            app.setPhone(currentUser.getPhone());
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("loanApplication", app);
        return "apply";
    }

    @PostMapping("/apply")
    public String submitApplication(@ModelAttribute("loanApplication") LoanApplication application,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(session);
        if (currentUser != null) {
            application.setApplicantUser(currentUser);
        }

        LoanApplication created = loanService.createApplication(application);
        String message = "Loan Application registered successfully! Generated Application ID: "
                + created.getApplicationId();

        // SBI Phase 2: If HIGH_RISK, flag in the success message
        if ("HIGH_RISK_EXCEPTION".equals(created.getSbiEligibilityTag())) {
            message += " ⚠️ SBI Risk Engine has flagged this application as HIGH RISK (CIBIL/FOIR/Age criteria not met). Routed to Exceptions queue.";
        } else if ("PRE_APPROVED".equals(created.getSbiEligibilityTag())) {
            message += " ✅ SBI Risk Engine: PRE_APPROVED (CIBIL ≥ 750, FOIR ≤ 40%). Fast-tracked to Underwriting.";
        }
        redirectAttributes.addFlashAttribute("successMessage", message);

        // If an applicant is logged in, redirect them to their personal application tracking view
        if (currentUser != null && currentUser.isUser()) {
            return "redirect:/my-applications";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/loans/{id}")
    public String viewLoanDetails(@PathVariable("id") Long id,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        LoanApplication loan = loanService.getApplicationById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid loan ID: " + id));

        User currentUser = getCurrentUser(session);

        // Security check: ensure regular applicant can only view their own loan
        if (!loanService.canUserViewLoan(currentUser, loan)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access Denied: You cannot view loan applications submitted by other applicants.");
            return "redirect:/my-applications";
        }

        boolean canUpdate = (currentUser == null || currentUser.isAdmin());

        model.addAttribute("loan", loan);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("canUpdate", canUpdate);
        model.addAttribute("events", loanService.getEventsForApplication(loan));
        model.addAttribute("documents", loanService.getDocumentsForApplication(loan));
        return "details";
    }

    @PostMapping("/loans/{id}/status")
    public String updateLoanStatus(@PathVariable("id") Long id,
                                   @RequestParam("newStatus") String newStatus,
                                   @RequestParam("remarks") String remarks,
                                   @RequestParam(value = "rejectionReason", required = false) String rejectionReason,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(session);

        // Guard: End users cannot update loan statuses
        if (currentUser != null && currentUser.isUser()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access Denied: Only bank administrators and loan officers can update application statuses.");
            return "redirect:/loans/" + id;
        }

        loanService.updateStatus(id, newStatus, remarks, rejectionReason);
        redirectAttributes.addFlashAttribute("successMessage",
                "Status updated to " + newStatus + " and audit event logged successfully!");
        return "redirect:/loans/" + id;
    }

    /**
     * Alerts page shows REJECTED applications and HIGH_RISK_EXCEPTION applications.
     * Restricted to Admins / Loan Officers.
     */
    @GetMapping("/alerts")
    public String viewAlerts(HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        User currentUser = getCurrentUser(session);

        // Guard: End users cannot view bank alerts/exceptions queue
        if (currentUser != null && currentUser.isUser()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access Denied: Alerts and Underwriting Exceptions are restricted to bank loan officers.");
            return "redirect:/my-applications";
        }

        List<LoanApplication> rejectedLoans = loanService.getApplicationsByStatus("REJECTED");
        List<LoanApplication> highRiskLoans = loanService.getApplicationsByEligibilityTag("HIGH_RISK_EXCEPTION");

        // Merge without duplicates
        List<LoanApplication> exceptions = new ArrayList<>(rejectedLoans);
        for (LoanApplication hr : highRiskLoans) {
            if (exceptions.stream().noneMatch(e -> e.getId().equals(hr.getId()))) {
                exceptions.add(hr);
            }
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("rejectedLoans", exceptions);
        model.addAttribute("highRiskCount", highRiskLoans.size());
        model.addAttribute("rejectedCount", rejectedLoans.size());
        return "alerts";
    }
}
