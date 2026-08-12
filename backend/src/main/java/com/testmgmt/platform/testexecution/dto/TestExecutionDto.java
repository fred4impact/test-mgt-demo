package com.testmgmt.platform.testexecution.dto;

import com.testmgmt.platform.testexecution.entity.TestExecutionStatus;
import java.time.Instant;
import java.util.UUID;

public record TestExecutionDto(
        UUID id,
        UUID projectId,
        UUID cycleId,
        UUID testCaseId,
        UUID assigneeId,
        UUID environmentId,
        UUID buildId,
        TestExecutionStatus status,
        Instant startedAt,
        Instant completedAt,
        Long durationMs,
        String actualResult,
        String comment,
        Instant createdAt,
        Instant updatedAt) {}
