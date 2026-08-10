package com.testmgmt.platform.testcase.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TestCaseDto(
        UUID id,
        UUID projectId,
        UUID folderId,
        String key,
        String title,
        String priority,
        String severity,
        String status,
        String testType,
        String automationStatus,
        UUID ownerId,
        Instant createdAt,
        List<TestStepDto> steps) {}
