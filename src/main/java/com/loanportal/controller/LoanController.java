package com.loanportal.controller;

import com.loanportal.model.LoanApplication;
import com.loanportal.service.LoanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping({"/", "/dashboard"})
    public String viewDashboard(@RequestParam(value = "search", required = false) String search, Model model) {
        model.addAttribute("applications", loanService.searchApplications(search));
        model.addAttribute("searchQuery", search != null ? search : "");
        model.addAttribute("totalCount", loanService.countTotal());
        model.addAttribute("submittedCount", loanService.countByStatus("SUBMITTED"));
        model.addAttribute("approvedCount", loanService.countByStatus("APPROVED"));
        model.addAttribute("rejectedCount", loanService.countByStatus("REJECTED"));
        return "dashboard";
    }

    @GetMapping("/apply")
    public String showApplyForm(Model model) {
        model.addAttribute("loanApplication", new LoanApplication());
        return "apply";
    }

    @PostMapping("/apply")
    public String submitApplication(@ModelAttribute("loanApplication") LoanApplication application,
                                    RedirectAttributes redirectAttributes) {
        LoanApplication created = loanService.createApplication(application);
        redirectAttributes.addFlashAttribute("successMessage", 
                "Loan Application registered successfully! Generated Application ID: " + created.getApplicationId());
        return "redirect:/dashboard";
    }

    @GetMapping("/loans/{id}")
    public String viewLoanDetails(@PathVariable("id") Long id, Model model) {
        LoanApplication loan = loanService.getApplicationById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid loan ID: " + id));
        model.addAttribute("loan", loan);
        model.addAttribute("events", loanService.getEventsForApplication(loan));
        return "details";
    }

    @PostMapping("/loans/{id}/status")
    public String updateLoanStatus(@PathVariable("id") Long id,
                                   @RequestParam("newStatus") String newStatus,
                                   @RequestParam("remarks") String remarks,
                                   @RequestParam(value = "rejectionReason", required = false) String rejectionReason,
                                   RedirectAttributes redirectAttributes) {
        loanService.updateStatus(id, newStatus, remarks, rejectionReason);
        redirectAttributes.addFlashAttribute("successMessage", 
                "Status updated to " + newStatus + " and audit event logged successfully!");
        return "redirect:/loans/" + id;
    }

    @GetMapping("/alerts")
    public String viewAlerts(Model model) {
        model.addAttribute("rejectedLoans", loanService.getApplicationsByStatus("REJECTED"));
        return "alerts";
    }
}
