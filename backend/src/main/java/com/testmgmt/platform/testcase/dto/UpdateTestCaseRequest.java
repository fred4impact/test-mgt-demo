package com.testmgmt.platform.testcase.dto;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

public record UpdateTestCaseRequest(
        UUID folderId,
        String title,
        String priority,
        String severity,
        String status,
        String testType,
        String automationStatus,
        UUID releaseId,
        @Valid List<CreateTestStepRequest> steps,
        String changeSummary) {}
