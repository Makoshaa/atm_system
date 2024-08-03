package atm.system.project1.mapper;

import atm.system.project1.dto.TransactionDTO;
import atm.system.project1.dto.UserDTO;
import atm.system.project1.model.Transaction;
import atm.system.project1.model.User;
import org.springframework.stereotype.Component;

@Component
public class DTOMapper {

    public TransactionDTO toDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setType(transaction.getType());
        dto.setAmount(transaction.getAmount());
        dto.setDate(transaction.getDate());
        dto.setAccountNumber(transaction.getAccountNumber());
        return dto;
    }

    public Transaction toEntity(TransactionDTO dto) {
        Transaction transaction = new Transaction();
        transaction.setId(dto.getId());
        transaction.setType(dto.getType());
        transaction.setAmount(dto.getAmount());
        transaction.setDate(dto.getDate());
        transaction.setAccountNumber(dto.getAccountNumber());
        return transaction;
    }

    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setAccountNumber(user.getAccountNumber());
        dto.setBalance(user.getBalance());
        return dto;
    }

}