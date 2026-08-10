package com.testmgmt.platform.requirement.dto;

import java.time.Instant;
import java.util.UUID;

public record RequirementDto(
        UUID id,
        UUID projectId,
        String key,
        String title,
        String status,
        String priority,
        UUID ownerId,
        Instant createdAt) {}
