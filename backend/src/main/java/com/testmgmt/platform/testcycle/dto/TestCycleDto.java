package com.testmgmt.platform.testcycle.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TestCycleDto(
        UUID id,
        UUID projectId,
        UUID testPlanId,
        UUID releaseId,
        UUID buildId,
        UUID environmentId,
        String name,
        String status,
        UUID ownerId,
        LocalDate startDate,
        LocalDate endDate,
        Instant createdAt) {}
