package com.testmgmt.platform.testcyclecase.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddTestCycleCaseRequest(
        @NotNull(message = "testCaseId is required") UUID testCaseId, UUID assigneeId) {}
