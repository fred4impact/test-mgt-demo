package com.testmgmt.platform.testplan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTestPlanRequest(
        @NotBlank(message = "name is required") String name,
        @NotNull(message = "releaseId is required") UUID releaseId,
        String status,
        LocalDate startDate,
        LocalDate endDate) {}
