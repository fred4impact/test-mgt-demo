package com.testmgmt.platform.testcase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreateTestCaseRequest(
        @NotNull(message = "folderId is required") UUID folderId,
        @NotBlank(message = "title is required") String title,
        String priority,
        String severity,
        String testType,
        String automationStatus,
        UUID releaseId,
        @Valid List<CreateTestStepRequest> steps) {}
