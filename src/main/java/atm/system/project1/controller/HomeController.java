package atm.system.project1.controller;

import atm.system.project1.config.SecurityConfig;
import atm.system.project1.dto.TransactionDTO;
import atm.system.project1.services.TransactionService;
import atm.system.project1.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Tag(name = "Home Controller", description = "Endpoints for basic account operations")
@Controller
@RequestMapping("/accounts")
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @Operation(summary = "View home page")
    @GetMapping("/")
    public String home() {
        return "index";
    }

    @Operation(summary = "Get account balance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved balance"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found")
    })
    @GetMapping("/balance")
    public String showBalancePage(Model model, Principal principal) {
        String accountNumber = principal.getName();
        logger.info("Showing balance page for account number: {}", accountNumber);
        try {
            BigDecimal balance = userService.getBalance(accountNumber);
            List<TransactionDTO> recentTransactions = transactionService.getTransactionHistory(accountNumber);

            model.addAttribute("accountNumber", accountNumber);
            model.addAttribute("balance", balance);
            model.addAttribute("recentTransactions", recentTransactions);
            return "balance";
        } catch (RuntimeException e) {
            logger.error("Error showing balance page: {}", e.getMessage());
            model.addAttribute("error",
                    "Unable to retrieve balance. Please try again later.");
            return "error";
        }
    }

    @Operation(summary = "Show deposit page")
    @GetMapping("/deposit")
    public String showDepositPage() {
        return "deposit";
    }

    @Operation(summary = "Show withdraw page")
    @GetMapping("/withdraw")
    public String showWithdrawPage() {
        return "withdraw";
    }

    @Operation(summary = "Show transfer page")
    @GetMapping("/transfer")
    public String showTransferPage() {
        return "transfer";
    }

    @Operation(summary = "Deposit money")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deposited money"),
            @ApiResponse(responseCode = "400", description = "Invalid amount"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to perform this operation")
    })
    @PostMapping("/deposit")
    public String deposit(
            @Parameter(description = "Amount to deposit") @RequestParam BigDecimal amount,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            String accountNumber = principal.getName();
            transactionService.deposit(accountNumber, amount);
            redirectAttributes.addFlashAttribute("successMessage", "Deposit successful");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/accounts/balance";
    }

    @Operation(summary = "Withdraw money")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully withdrawn money"),
            @ApiResponse(responseCode = "400", description = "Invalid amount or insufficient funds"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to perform this operation")
    })
    @PostMapping("/withdraw")
    public String withdraw(
            @Parameter(description = "Amount to withdraw") @RequestParam BigDecimal amount,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            if (amount.compareTo(new BigDecimal("70")) < 0) {
                redirectAttributes.addAttribute("insufficientAmount", true);
                return "redirect:/accounts/withdraw";
            }

            String accountNumber = principal.getName();
            transactionService.withdraw(accountNumber, amount);
            redirectAttributes.addAttribute("success", true);
            return "redirect:/accounts/withdraw";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/accounts/withdraw";
        }
    }

    @Operation(summary = "Transfer money")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully transferred money"),
            @ApiResponse(responseCode = "400", description = "Invalid amount, insufficient funds, or invalid recipient account"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to perform this operation")
    })
    @PostMapping("/transfer")
    public String transfer(
            @Parameter(description = "Recipient account number") @RequestParam String toAccount,
            @Parameter(description = "Amount to transfer") @RequestParam BigDecimal amount,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            String fromAccount = principal.getName();

            if (amount.compareTo(new BigDecimal("70")) < 0) {
                redirectAttributes.addAttribute("insufficientAmount", true);
                return "redirect:/accounts/transfer";
            }

            if (!userService.accountExists(toAccount)) {
                redirectAttributes.addAttribute("invalidAccount", true);
                return "redirect:/accounts/transfer";
            }

            transactionService.transfer(fromAccount, toAccount, amount);
            redirectAttributes.addAttribute("success", true);
            return "redirect:/accounts/transfer";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/accounts/transfer";
        }
    }
}