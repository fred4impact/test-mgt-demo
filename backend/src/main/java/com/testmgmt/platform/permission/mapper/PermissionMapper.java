package com.testmgmt.platform.permission.mapper;

import com.testmgmt.platform.permission.dto.PermissionDto;
import com.testmgmt.platform.permission.entity.Permission;

public final class PermissionMapper {

    private PermissionMapper() {}

    public static PermissionDto toDto(Permission permission) {
        return new PermissionDto(permission.getId(), permission.getCode(), permission.getDescription());
    }
}
