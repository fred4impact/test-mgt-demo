package com.testmgmt.platform.testexecution.dto;

import com.testmgmt.platform.testexecution.entity.TestExecutionStatus;
import java.time.Instant;
import java.util.UUID;

public record ExecutionStepDto(
        UUID id,
        UUID executionId,
        UUID testStepId,
        int stepNumber,
        TestExecutionStatus status,
        String actualResult,
        String comment,
        Instant createdAt,
        Instant updatedAt) {}
