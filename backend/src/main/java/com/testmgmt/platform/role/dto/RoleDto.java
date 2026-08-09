package com.testmgmt.platform.role.dto;

import java.time.Instant;
import java.util.UUID;

public record RoleDto(UUID id, UUID organizationId, String name, boolean systemRole, Instant createdAt) {}
