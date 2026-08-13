package com.staysure.config;

import com.staysure.audit.service.AuditService;
import com.staysure.common.enums.RoleName;
import com.staysure.common.enums.UserStatus;
import com.staysure.role.service.RoleService;
import com.staysure.user.entity.User;
import com.staysure.user.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements ApplicationRunner {

    private final RoleService roleService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapProperties bootstrapProperties;
    private final AuditService auditService;

    public DataInitializer(RoleService roleService,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           BootstrapProperties bootstrapProperties,
                           AuditService auditService) {
        this.roleService = roleService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapProperties = bootstrapProperties;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        roleService.ensureDefaultRolesAndPermissions();
        initializeSuperAdmin();
    }

    private void initializeSuperAdmin() {
        boolean hasSuperAdmin = !userRepository.findAllByRole(RoleName.SUPER_ADMIN).isEmpty();
        if (hasSuperAdmin || isBlank(bootstrapProperties.superAdminEmail())
                || isBlank(bootstrapProperties.superAdminPassword())
                || isBlank(bootstrapProperties.superAdminPhone())) {
            return;
        }

        String email = bootstrapProperties.superAdminEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElseGet(User::new);
        if (user.getId() == null) {
            user.setEmail(email);
            user.setPhone(bootstrapProperties.superAdminPhone().trim().replaceAll("[\\s()\\-]", ""));
            user.setFirstName(defaultText(bootstrapProperties.superAdminFirstName(), "Super"));
            user.setLastName(defaultText(bootstrapProperties.superAdminLastName(), "Admin"));
            user.setPasswordHash(passwordEncoder.encode(bootstrapProperties.superAdminPassword()));
            user.setStatus(UserStatus.ACTIVE);
        }
        roleService.assignRoleIfMissing(user, RoleName.USER);
        roleService.assignRoleIfMissing(user, RoleName.SUPER_ADMIN);
        User saved = userRepository.save(user);
        auditService.log(saved, "ROLE_ASSIGNED", "ROLE", "User", saved.getId(),
                "Initial SUPER_ADMIN role assigned from environment configuration", null, "SUPER_ADMIN", null);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String defaultText(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }
}
