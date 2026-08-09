package com.testmgmt.platform.role.service;

import com.testmgmt.platform.common.error.ConflictException;
import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.permission.dto.PermissionDto;
import com.testmgmt.platform.permission.entity.Permission;
import com.testmgmt.platform.permission.mapper.PermissionMapper;
import com.testmgmt.platform.permission.repository.PermissionRepository;
import com.testmgmt.platform.role.dto.AddRolePermissionRequest;
import com.testmgmt.platform.role.entity.Role;
import com.testmgmt.platform.role.entity.RolePermission;
import com.testmgmt.platform.role.repository.RolePermissionRepository;
import com.testmgmt.platform.role.repository.RoleRepository;
import com.testmgmt.platform.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class RolePermissionService {

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final UserService userService;

    public RolePermissionService(
            RoleRepository roleRepository,
            RolePermissionRepository rolePermissionRepository,
            PermissionRepository permissionRepository,
            UserService userService) {
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
        this.userService = userService;
    }

    public PermissionDto addPermission(Jwt jwt, UUID roleId, AddRolePermissionRequest request) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Role role = roleRepository
                .findByIdAndOrganizationId(roleId, organizationId)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleId));

        Permission permission = permissionRepository
                .findById(request.permissionId())
                .orElseThrow(() -> new NotFoundException("Permission not found: " + request.permissionId()));

        if (rolePermissionRepository.existsByRoleIdAndPermissionId(role.getId(), permission.getId())) {
            throw new ConflictException("Permission is already attached to this role");
        }

        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(role.getId());
        rolePermission.setPermissionId(permission.getId());
        rolePermissionRepository.save(rolePermission);

        return PermissionMapper.toDto(permission);
    }

    public void removePermission(Jwt jwt, UUID roleId, UUID permissionId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Role role = roleRepository
                .findByIdAndOrganizationId(roleId, organizationId)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleId));

        RolePermission rolePermission = rolePermissionRepository
                .findByRoleIdAndPermissionId(role.getId(), permissionId)
                .orElseThrow(() -> new NotFoundException("Permission is not attached to this role: " + permissionId));

        rolePermissionRepository.delete(rolePermission);
    }

    public List<PermissionDto> listPermissions(Jwt jwt, UUID roleId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Role role = roleRepository
                .findByIdAndOrganizationId(roleId, organizationId)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleId));

        return rolePermissionRepository.findByRoleId(role.getId()).stream()
                .map(rp -> permissionRepository
                        .findById(rp.getPermissionId())
                        .orElseThrow(() -> new NotFoundException("Permission not found: " + rp.getPermissionId())))
                .map(PermissionMapper::toDto)
                .toList();
    }
}
