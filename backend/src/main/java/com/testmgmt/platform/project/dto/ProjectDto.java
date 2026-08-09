package com.testmgmt.platform.project.dto;

import java.time.Instant;
import java.util.UUID;

public record ProjectDto(
        UUID id, UUID organizationId, String key, String name, String status, UUID ownerId, Instant createdAt) {}
