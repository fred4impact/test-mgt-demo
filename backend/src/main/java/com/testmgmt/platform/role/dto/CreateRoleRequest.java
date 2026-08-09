package com.testmgmt.platform.role.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateRoleRequest(@NotBlank(message = "name is required") String name, Boolean systemRole) {

    public boolean systemRoleOrDefault() {
        return systemRole != null && systemRole;
    }
}
