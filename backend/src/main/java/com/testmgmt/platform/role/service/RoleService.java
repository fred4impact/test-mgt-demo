package com.testmgmt.platform.role.service;

import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.role.dto.CreateRoleRequest;
import com.testmgmt.platform.role.dto.RoleDto;
import com.testmgmt.platform.role.entity.Role;
import com.testmgmt.platform.role.mapper.RoleMapper;
import com.testmgmt.platform.role.repository.RoleRepository;
import com.testmgmt.platform.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserService userService;

    public RoleService(RoleRepository roleRepository, UserService userService) {
        this.roleRepository = roleRepository;
        this.userService = userService;
    }

    public RoleDto create(Jwt jwt, CreateRoleRequest request) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();

        Role role = new Role();
        role.setOrganizationId(organizationId);
        role.setName(request.name());
        role.setSystemRole(request.systemRoleOrDefault());
        return RoleMapper.toDto(roleRepository.save(role));
    }

    public RoleDto getById(Jwt jwt, UUID id) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Role role = roleRepository
                .findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new NotFoundException("Role not found: " + id));
        return RoleMapper.toDto(role);
    }

    public List<RoleDto> list(Jwt jwt) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        return roleRepository.findByOrganizationId(organizationId).stream()
                .map(RoleMapper::toDto)
                .toList();
    }
}
