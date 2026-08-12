package com.testmgmt.platform.testexecution.dto;

import com.testmgmt.platform.testexecution.entity.TestExecutionStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTestExecutionRequest(
        @NotNull(message = "status is required") TestExecutionStatus status, String actualResult, String comment) {}
