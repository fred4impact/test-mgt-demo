package com.testmgmt.platform.role.mapper;

import com.testmgmt.platform.role.dto.RoleDto;
import com.testmgmt.platform.role.entity.Role;

public final class RoleMapper {

    private RoleMapper() {}

    public static RoleDto toDto(Role role) {
        return new RoleDto(
                role.getId(), role.getOrganizationId(), role.getName(), role.isSystemRole(), role.getCreatedAt());
    }
}
