package com.testmgmt.platform.testplan.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TestPlanDto(
        UUID id,
        UUID projectId,
        UUID releaseId,
        String name,
        String status,
        UUID ownerId,
        LocalDate startDate,
        LocalDate endDate,
        Instant createdAt) {}
