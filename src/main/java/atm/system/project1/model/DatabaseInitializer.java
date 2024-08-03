package atm.system.project1.model;

import atm.system.project1.repository.PermissionRepository;
import atm.system.project1.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;




@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) {
        initializePermissions();
        createAdminIfNotExists();
    }

    private void initializePermissions() {
        createPermissionIfNotExists("USER");
        createPermissionIfNotExists("BANK_ADMIN");
    }

    private void createPermissionIfNotExists(String role) {
        if (permissionRepository.findByRole(role).isEmpty()) {
            Permission permission = new Permission();
            permission.setRole(role);
            permissionRepository.save(permission);
        }
    }

    private void createAdminIfNotExists() {
        if (!userService.findByAccountNumber("admin").isPresent()) {
            User adminUser = new User();
            adminUser.setAccountNumber("admin");
            adminUser.setPin("admin");  // Будет зашифрован в UserService
            adminUser = userService.registerUser(adminUser);
            userService.updateUserRole(adminUser.getId(), "BANK_ADMIN");
        }
    }
}
