package com.testmgmt.platform.role.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddRolePermissionRequest(@NotNull(message = "permissionId is required") UUID permissionId) {}
