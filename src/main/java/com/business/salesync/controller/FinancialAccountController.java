package com.business.salesync.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.business.salesync.models.FinancialAccount;
import com.business.salesync.repository.FinancialAccountRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class FinancialAccountController {

    private final FinancialAccountRepository financialAccountRepository;

    // -----------------------
    // List all accounts
    // -----------------------
    @GetMapping
    public String listAccounts(Model model) {
        List<FinancialAccount> accounts = financialAccountRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        double totalBalance = accounts.stream().mapToDouble(FinancialAccount::getCurrentBalance).sum();
        double totalDebit = accounts.stream().mapToDouble(a -> a.getDebitAmount() != null ? a.getDebitAmount() : 0).sum();
        double totalCredit = accounts.stream().mapToDouble(a -> a.getCreditAmount() != null ? a.getCreditAmount() : 0).sum();

        model.addAttribute("accounts", accounts);
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("totalDebit", totalDebit);
        model.addAttribute("totalCredit", totalCredit);

        return "fragments/accounts"; // existing template: accounts.htm
    }

    // -----------------------
    // Show form to create new account
    // -----------------------
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        FinancialAccount account = new FinancialAccount();
        account.setFinAccId(generateUniqueAccountId());
        model.addAttribute("account", account);
        return "fragments/account_form"; // existing template: account_form.htm
    }

    // -----------------------
    // Save new or edited account
    // -----------------------
    @PostMapping("/save")
    public String saveAccount(@ModelAttribute("account") FinancialAccount account, RedirectAttributes redirectAttributes) {
        if (account.getCurrentBalance() == null) {
            account.setCurrentBalance(account.getOpeningBalance() != null ? account.getOpeningBalance() : 0.0);
        }

        if (account.getFinAccId() == null || account.getFinAccId().isEmpty()) {
            account.setFinAccId(generateUniqueAccountId());
        }

        financialAccountRepository.save(account);
        redirectAttributes.addFlashAttribute("successMessage", "Account saved successfully!");
        return "redirect:/accounts";
    }

    // -----------------------
    // Edit account (optional)
    // -----------------------
    @GetMapping("/edit")
    public String editAccount(@RequestParam("id") Long id, Model model) {
        FinancialAccount account = financialAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid account ID: " + id));
        model.addAttribute("account", account);
        return "fragments/account_form";
    }

    // -----------------------
    // Delete account (optional)
    // -----------------------
    @GetMapping("/delete")
    public String deleteAccount(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        financialAccountRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Account deleted successfully!");
        return "redirect:/accounts";
    }

    // -----------------------
    // Latest balance per account (used for summary tabs)
    // -----------------------
    @GetMapping("/tabs")
    public String accountsTabs(Model model) {
        List<FinancialAccount> latestAccounts = financialAccountRepository.findLatestBalancePerAccount();
        List<FinancialAccount> accounts = financialAccountRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        model.addAttribute("latestAccounts", latestAccounts);
        model.addAttribute("accounts", accounts);

        double totalBalance = latestAccounts.stream().mapToDouble(FinancialAccount::getCurrentBalance).sum();
        double totalDebit = latestAccounts.stream().mapToDouble(a -> a.getDebitAmount() != null ? a.getDebitAmount() : 0).sum();
        double totalCredit = latestAccounts.stream().mapToDouble(a -> a.getCreditAmount() != null ? a.getCreditAmount() : 0).sum();

        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("totalDebit", totalDebit);
        model.addAttribute("totalCredit", totalCredit);

        return "fragments/accounts"; // reuse the main accounts list template
    }

    // -----------------------
    // AJAX: Get accounts by type
    // -----------------------
    @GetMapping("/by-type")
    @ResponseBody
    public List<FinancialAccount> getAccountsByType(@RequestParam("type") String type) {
        if (type == null) return new ArrayList<>();

        String normalized = type.trim().toUpperCase();
        String category = switch (normalized) {
            case "CASH" -> "CASH";
            case "BKASH", "MFS", "MOBILE" -> "MFS";
            case "BANK TRANSFER", "BANK" -> "BANK";
            default -> "CASH";
        };

        return financialAccountRepository.findByFinAccType(category)
                .stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(FinancialAccount::getFinAccName, fa -> fa, (a, b) -> a),
                        map -> new ArrayList<>(map.values())
                ));
    }

    // -----------------------
    // Helper: generate unique account ID
    // -----------------------
    private String generateUniqueAccountId() {
        String prefix = "FIN-1001-";
        long maxNumber = 90000;

        Optional<FinancialAccount> lastAccount = financialAccountRepository.findTopByOrderByIdDesc();
        if (lastAccount.isPresent()) {
            String lastId = lastAccount.get().getFinAccId();
            if (lastId != null && lastId.startsWith(prefix)) {
                try {
                    long lastNumber = Long.parseLong(lastId.substring(prefix.length()));
                    maxNumber = lastNumber;
                } catch (NumberFormatException ignored) {}
            }
        }
        return prefix + (maxNumber + 1);
    }
}
