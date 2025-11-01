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

    // List accounts
    @GetMapping
    public String listAccounts(Model model) {
        List<FinancialAccount> accounts = financialAccountRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        double totalBalance = accounts.stream()
                .mapToDouble(FinancialAccount::getCurrentBalance)
                .sum();
        double totalDebit = accounts.stream()
                .mapToDouble(a -> a.getDebitAmount() != null ? a.getDebitAmount() : 0)
                .sum();
        double totalCredit = accounts.stream()
                .mapToDouble(a -> a.getCreditAmount() != null ? a.getCreditAmount() : 0)
                .sum();

        model.addAttribute("accounts", accounts);
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("totalDebit", totalDebit);
        model.addAttribute("totalCredit", totalCredit);

        return "fragments/accounts";
    }
    
    @GetMapping("/latest-balance")
    public String latestBalance(Model model) {
        List<FinancialAccount> latestAccounts = financialAccountRepository.findLatestBalancePerAccount();

        double totalBalance = latestAccounts.stream()
                .mapToDouble(FinancialAccount::getCurrentBalance)
                .sum();
        double totalDebit = latestAccounts.stream()
                .mapToDouble(a -> a.getDebitAmount() != null ? a.getDebitAmount() : 0)
                .sum();
        double totalCredit = latestAccounts.stream()
                .mapToDouble(a -> a.getCreditAmount() != null ? a.getCreditAmount() : 0)
                .sum();

        model.addAttribute("accounts", latestAccounts);
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("totalDebit", totalDebit);
        model.addAttribute("totalCredit", totalCredit);

        return "fragments/accounts_latest";
    }
    
    @GetMapping("/tabs")
    public String accountsTabs(Model model) {
        List<FinancialAccount> latestAccounts = financialAccountRepository.findLatestBalancePerAccount();
        List<FinancialAccount> accounts = financialAccountRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        model.addAttribute("latestAccounts", latestAccounts);
        model.addAttribute("accounts", accounts);

        // compute totals for summary cards if needed
        double totalBalance = latestAccounts.stream().mapToDouble(FinancialAccount::getCurrentBalance).sum();
        double totalDebit = latestAccounts.stream().mapToDouble(a -> a.getDebitAmount() != null ? a.getDebitAmount() : 0).sum();
        double totalCredit = latestAccounts.stream().mapToDouble(a -> a.getCreditAmount() != null ? a.getCreditAmount() : 0).sum();

        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("totalDebit", totalDebit);
        model.addAttribute("totalCredit", totalCredit);

        return "fragments/accounts"; // name of the Thymeleaf page
    }



    // Show form to create new account
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        FinancialAccount account = new FinancialAccount();
        account.setFinAccId(generateUniqueAccountId());
        model.addAttribute("account", account);
        return "fragments/account_form";
    }

    // Handle form submission
    @PostMapping("/save")
    public String saveAccount(@ModelAttribute("account") FinancialAccount account, RedirectAttributes redirectAttributes) {
        if (account.getCurrentBalance() == null) {
            account.setCurrentBalance(account.getOpeningBalance() != null ? account.getOpeningBalance() : 0.0);
        }

        // Ensure account ID is set (in case someone submitted manually)
        if (account.getFinAccId() == null || account.getFinAccId().isEmpty()) {
            account.setFinAccId(generateUniqueAccountId());
        }

        financialAccountRepository.save(account);
        redirectAttributes.addFlashAttribute("successMessage", "Account created successfully!");
        return "redirect:/accounts";
    }

    // Helper method to generate unique 12-digit ID: FIN-1001-90001
    private String generateUniqueAccountId() {
        String prefix = "FIN-1001-";
        long maxNumber = 90000; // starting number (so first will be 90001)
        // find last account
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
    
    // ✅ New endpoint for AJAX (by account type)
    @GetMapping("/by-type")
    @ResponseBody
    public List<FinancialAccount> getAccountsByType(@RequestParam("type") String type) {
        String normalizedType = type.trim().toUpperCase();

        switch (normalizedType) {
            case "CASH":
                normalizedType = "CASH";
                break;
            case "BKASH":
            case "MFS":
            case "MOBILE":
                normalizedType = "MFS";
                break;
            case "BANK TRANSFER":
            case "BANK":
                normalizedType = "BANK";
                break;
            default:
                normalizedType = "CASH";
        }
        //return financialAccountRepository.findByFinAccTypeDistinct(normalizedType);
        //return financialAccountRepository.findByFinAccType(normalizedType);
        return financialAccountRepository.findByFinAccType(normalizedType)
                .stream()
                .collect(Collectors.collectingAndThen(
                    Collectors.toMap(FinancialAccount::getFinAccName, fa -> fa, (a, b) -> a),
                    map -> new ArrayList<>(map.values())
                ));
    }
}
