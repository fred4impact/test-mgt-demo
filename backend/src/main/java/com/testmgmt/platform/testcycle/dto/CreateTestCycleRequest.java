package com.testmgmt.platform.testcycle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTestCycleRequest(
        @NotBlank(message = "name is required") String name,
        @NotNull(message = "testPlanId is required") UUID testPlanId,
        @NotNull(message = "releaseId is required") UUID releaseId,
        @NotNull(message = "buildId is required") UUID buildId,
        @NotNull(message = "environmentId is required") UUID environmentId,
        String status,
        LocalDate startDate,
        LocalDate endDate) {}
