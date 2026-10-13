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
}
