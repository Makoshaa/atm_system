package atm.system.project1.services;

import atm.system.project1.dto.UserDTO;
import atm.system.project1.mapper.DTOMapper;
import atm.system.project1.model.Permission;
import atm.system.project1.model.User;
import atm.system.project1.repository.PermissionRepository;
import atm.system.project1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;



@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private DTOMapper dtoMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByAccountNumber(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with account number: " + username));
    }



    @Transactional
    public User registerUser(User user) {
        if (userRepository.findByAccountNumber(user.getAccountNumber()).isPresent()) {
            throw new RuntimeException("Account number already exists");
        }

        user.setPin(passwordEncoder.encode(user.getPin()));
        user.setBalance(BigDecimal.ZERO);

        Set<Permission> defaultPermissions = getDefaultPermissions("USER");
        user.setPermissions(defaultPermissions);

        return userRepository.save(user);
    }


    private Set<Permission> getDefaultPermissions(String role) {
        return new HashSet<>(permissionRepository.findByRole(role));
    }


    public Optional<UserDTO> findByAccountNumber(String accountNumber) {
        return userRepository.findByAccountNumber(accountNumber)
                .map(dtoMapper::toDTO);
    }


    @Transactional
    public void updateUser(UserDTO userDTO, String newPin) {
        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAccountNumber(userDTO.getAccountNumber());
        user.setBalance(userDTO.getBalance());

        if (newPin != null && !newPin.isEmpty()) {
            user.setPin(passwordEncoder.encode(newPin));
        }

        userRepository.save(user);
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return dtoMapper.toDTO(user);
    }



    public BigDecimal getBalance(String accountNumber) {
        return findByAccountNumber(accountNumber)
                .map(UserDTO::getBalance)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean accountExists(String accountNumber) {
        return userRepository.findByAccountNumber(accountNumber).isPresent();
    }

    @Transactional
    public void updateUserRole(Long id, String newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Set<Permission> newPermissions = getDefaultPermissions(newRole);
        user.setPermissions(newPermissions);
        userRepository.save(user);
    }

    public void updateUser(UserDTO userDTO) {
        updateUser(userDTO, null);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }



}