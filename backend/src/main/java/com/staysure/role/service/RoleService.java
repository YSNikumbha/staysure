package com.staysure.role.service;

import com.staysure.common.enums.RoleName;
import com.staysure.common.exception.ResourceNotFoundException;
import com.staysure.role.entity.Permission;
import com.staysure.role.entity.Role;
import com.staysure.role.repository.PermissionRepository;
import com.staysure.role.repository.RoleRepository;
import com.staysure.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional(readOnly = true)
    public Role getRole(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
    }

    public boolean hasRole(User user, RoleName roleName) {
        return user.getRoles().stream().anyMatch(role -> role.getName() == roleName);
    }

    public boolean assignRoleIfMissing(User user, RoleName roleName) {
        if (hasRole(user, roleName)) {
            return false;
        }
        user.getRoles().add(getRole(roleName));
        return true;
    }

    @Transactional
    public void ensureDefaultRolesAndPermissions() {
        Map<String, String> permissions = Map.of(
                "USER_VIEW", "View user records",
                "USER_MANAGE", "Manage user records",
                "OWNER_VIEW", "View owner applications",
                "OWNER_VERIFY", "Verify owner applications",
                "OWNER_SUSPEND", "Suspend owner access"
        );

        permissions.forEach((name, description) -> permissionRepository.findByName(name)
                .orElseGet(() -> {
                    Permission permission = new Permission();
                    permission.setName(name);
                    permission.setDescription(description);
                    return permissionRepository.save(permission);
                }));

        ensureRole(RoleName.SUPER_ADMIN, "Platform administrator with owner verification privileges",
                Set.of("USER_VIEW", "USER_MANAGE", "OWNER_VIEW", "OWNER_VERIFY", "OWNER_SUSPEND"));
        ensureRole(RoleName.PG_OWNER, "Verified PG owner", Set.of("OWNER_VIEW"));
        ensureRole(RoleName.USER, "Default registered platform user", Set.of("USER_VIEW"));
    }

    private void ensureRole(RoleName roleName, String description, Set<String> permissionNames) {
        Role role = roleRepository.findByName(roleName).orElseGet(() -> {
            Role created = new Role();
            created.setName(roleName);
            created.setSystemRole(true);
            return roleRepository.save(created);
        });
        role.setDescription(description);
        for (String permissionName : permissionNames) {
            Permission permission = permissionRepository.findByName(permissionName)
                    .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionName));
            role.getPermissions().add(permission);
        }
        roleRepository.save(role);
    }
}
