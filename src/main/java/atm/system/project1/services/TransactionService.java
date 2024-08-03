package atm.system.project1.services;

import atm.system.project1.dto.TransactionDTO;
import atm.system.project1.dto.UserDTO;
import atm.system.project1.mapper.DTOMapper;
import atm.system.project1.model.Transaction;
import atm.system.project1.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private DTOMapper dtoMapper;

    @Transactional
    public void deposit(String accountNumber, BigDecimal amount) {
        UserDTO userDTO = userService.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal newBalance = userDTO.getBalance().add(amount);
        userDTO.setBalance(newBalance);
        userService.updateUser(userDTO, null);

        createTransaction(accountNumber, "DEPOSIT", amount);
    }

    @Transactional
    public void withdraw(String accountNumber, BigDecimal amount) {
        UserDTO userDTO = userService.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (userDTO.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        BigDecimal newBalance = userDTO.getBalance().subtract(amount);
        userDTO.setBalance(newBalance);
        userService.updateUser(userDTO, null);

        createTransaction(accountNumber, "WITHDRAW", amount);
    }

    @Transactional
    public void transfer(String fromAccount, String toAccount, BigDecimal amount) {
        withdraw(fromAccount, amount);
        deposit(toAccount, amount);
        createTransaction(fromAccount, "TRANSFER_OUT", amount);
        createTransaction(toAccount, "TRANSFER_IN", amount);
    }

    private void createTransaction(String accountNumber, String type, BigDecimal amount) {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAccountNumber(accountNumber);
        transactionDTO.setType(type);
        transactionDTO.setAmount(amount);
        transactionDTO.setDate(LocalDateTime.now());
        Transaction transaction = dtoMapper.toEntity(transactionDTO);
        transactionRepository.save(transaction);
    }

    public List<TransactionDTO> getTransactionHistory(String accountNumber) {
        List<Transaction> transactions = transactionRepository.findByAccountNumber(accountNumber);
        return transactions.stream()
                .map(dtoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelTransfer(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getType().equals("TRANSFER_OUT") && !transaction.getType().equals("TRANSFER_IN")) {
            throw new RuntimeException("Only transfers can be cancelled");
        }

        // Reverse the transaction
        if (transaction.getType().equals("TRANSFER_OUT")) {
            deposit(transaction.getAccountNumber(), transaction.getAmount());
        } else {
            withdraw(transaction.getAccountNumber(), transaction.getAmount());
        }

        // Mark transaction as cancelled
        transaction.setType("CANCELLED_" + transaction.getType());
        transactionRepository.save(transaction);
    }

    public List<TransactionDTO> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        return transactions.stream()
                .map(dtoMapper::toDTO)
                .collect(Collectors.toList());
    }
}