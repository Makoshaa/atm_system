package atm.system.project1;

import atm.system.project1.dto.UserDTO;
import atm.system.project1.dto.TransactionDTO;
import atm.system.project1.model.Transaction;
import atm.system.project1.services.TransactionService;
import atm.system.project1.services.UserService;
import atm.system.project1.repository.UserRepository;
import atm.system.project1.repository.TransactionRepository;
import atm.system.project1.repository.PermissionRepository;
import atm.system.project1.mapper.DTOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ATMSystemTests {

	private TransactionService transactionService;
	private UserService userService;

	@Mock
	private UserService mockUserService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private DTOMapper dtoMapper;

	@Mock
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		transactionService = new TransactionService();
		userService = new UserService();

		// Внедрение зависимостей вручную
		ReflectionTestUtils.setField(transactionService, "userService", mockUserService);
		ReflectionTestUtils.setField(transactionService, "dtoMapper", dtoMapper);
		ReflectionTestUtils.setField(transactionService, "transactionRepository", mock(TransactionRepository.class));

		ReflectionTestUtils.setField(userService, "userRepository", userRepository);
		ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);
		ReflectionTestUtils.setField(userService, "permissionRepository", mock(PermissionRepository.class));
		ReflectionTestUtils.setField(userService, "dtoMapper", dtoMapper);

		when(dtoMapper.toEntity(any(TransactionDTO.class))).thenReturn(new Transaction());
	}

	@Test
	void testDeposit() {
		String accountNumber = "123456";
		BigDecimal initialBalance = new BigDecimal("1000.00");
		BigDecimal depositAmount = new BigDecimal("500.00");

		UserDTO userDTO = new UserDTO();
		userDTO.setAccountNumber(accountNumber);
		userDTO.setBalance(initialBalance);

		when(mockUserService.findByAccountNumber(accountNumber)).thenReturn(Optional.of(userDTO));

		transactionService.deposit(accountNumber, depositAmount);

		verify(mockUserService).updateUser(argThat(updatedUser ->
				updatedUser.getBalance().compareTo(new BigDecimal("1500.00")) == 0
		), isNull());
	}

	@Test
	void testTransfer() {
		String fromAccount = "123456";
		String toAccount = "789012";
		BigDecimal transferAmount = new BigDecimal("300.00");

		UserDTO fromUserDTO = new UserDTO();
		fromUserDTO.setAccountNumber(fromAccount);
		fromUserDTO.setBalance(new BigDecimal("1000.00"));

		UserDTO toUserDTO = new UserDTO();
		toUserDTO.setAccountNumber(toAccount);
		toUserDTO.setBalance(new BigDecimal("500.00"));

		when(mockUserService.findByAccountNumber(fromAccount)).thenReturn(Optional.of(fromUserDTO));
		when(mockUserService.findByAccountNumber(toAccount)).thenReturn(Optional.of(toUserDTO));

		transactionService.transfer(fromAccount, toAccount, transferAmount);

		verify(mockUserService).updateUser(argThat(updatedUser ->
				updatedUser.getAccountNumber().equals(fromAccount) &&
						updatedUser.getBalance().compareTo(new BigDecimal("700.00")) == 0
		), isNull());

		verify(mockUserService).updateUser(argThat(updatedUser ->
				updatedUser.getAccountNumber().equals(toAccount) &&
						updatedUser.getBalance().compareTo(new BigDecimal("800.00")) == 0
		), isNull());
	}

	@Test
	void testWithdraw() {
		String accountNumber = "123456";
		BigDecimal initialBalance = new BigDecimal("1000.00");
		BigDecimal withdrawAmount = new BigDecimal("300.00");

		UserDTO userDTO = new UserDTO();
		userDTO.setAccountNumber(accountNumber);
		userDTO.setBalance(initialBalance);

		when(mockUserService.findByAccountNumber(accountNumber)).thenReturn(Optional.of(userDTO));

		transactionService.withdraw(accountNumber, withdrawAmount);

		verify(mockUserService).updateUser(argThat(updatedUser ->
				updatedUser.getBalance().compareTo(new BigDecimal("700.00")) == 0
		), isNull());
	}

	@Test
	void testDeleteUser() {
		Long userId = 1L;

		userService.deleteUser(userId);

		verify(userRepository).deleteById(userId);
	}
}