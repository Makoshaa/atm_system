package atm.system.project1.controller;

import atm.system.project1.dto.TransactionDTO;
import atm.system.project1.dto.UserDTO;
import atm.system.project1.model.Permission;
import atm.system.project1.model.User;
import atm.system.project1.repository.PermissionRepository;
import atm.system.project1.services.TransactionService;
import atm.system.project1.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "Admin Controller", description = "Endpoints for admin operations")
@Controller
@RequestMapping("/accounts/admin")
@PreAuthorize("hasAuthority('BANK_ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private PermissionRepository permissionRepository;

    @Operation(summary = "Get all transactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of transactions"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden")
    })
    @GetMapping("/all-transactions")
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public String showAllTransactions(Model model) {
        List<TransactionDTO> allTransactions = transactionService.getAllTransactions();
        model.addAttribute("transactions", allTransactions);
        return "admin";
    }

    @Operation(summary = "Cancel a transfer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully cancelled the transfer"),
            @ApiResponse(responseCode = "400", description = "Invalid transaction ID supplied"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to perform this operation"),
            @ApiResponse(responseCode = "404", description = "The transaction with the given ID was not found")
    })
    @PostMapping("/cancel-transfer")
    public ResponseEntity<?> cancelTransfer(@RequestParam Long transactionId) {
        try {
            transactionService.cancelTransfer(transactionId);
            return ResponseEntity.ok().body(Map.of(
                    "success", true, "message", "Перевод успешно отменен"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('BANK_ADMIN')")
    public String showAllUsers(Model model) {
        List<User> allUsers = userService.getAllUsers();
        List<Permission> allPermissions = permissionRepository.findAll();
        model.addAttribute("users", allUsers);
        model.addAttribute("permissions", allPermissions);
        return "users";
    }

    @GetMapping("/users/{id}/edit")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        UserDTO userDTO = userService.getUserById(id);
        model.addAttribute("user", userDTO);
        return "edit-user";
    }

    @Operation(summary = "Update user role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the user role"),
            @ApiResponse(responseCode = "400", description = "Invalid role supplied"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to perform this operation"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The user with the given ID was not found")
    })
    @PostMapping("/users/{id}/edit")
    public String updateUser(
            @Parameter(description = "ID of the user to update") @PathVariable Long id,
            @Parameter(description = "New account number") @RequestParam String accountNumber,
            @Parameter(description = "New PIN (optional)") @RequestParam(required = false) String newPin,
            @Parameter(description = "New balance") @RequestParam BigDecimal balance,
            RedirectAttributes redirectAttributes) {
        try {
            UserDTO userDTO = userService.getUserById(id);

            if (userDTO == null) {
                throw new RuntimeException("User not found with id: " + id);
            }

            userDTO.setAccountNumber(accountNumber);
            userDTO.setBalance(balance);

            if (newPin != null && !newPin.isEmpty()) {
                userService.updateUser(userDTO, newPin);
            } else {
                userService.updateUser(userDTO);
            }
            redirectAttributes.addFlashAttribute("successMessage", "User information updated successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating user: " + e.getMessage());
        }

        return "redirect:/accounts/admin/users";
    }

    @PostMapping("/users/{id}/update-role")
    public String updateUserRole(@PathVariable Long id,
                                 @RequestParam String role,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserRole(id, role);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "User role updated successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Error updating user role: " + e.getMessage());
        }
        return "redirect:/accounts/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "User deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/accounts/admin/users";
    }
}