package com.testmgmt.platform.build.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateBuildRequest(
        @NotBlank(message = "name is required") String name,
        @NotNull(message = "releaseId is required") UUID releaseId,
        String version,
        String branch,
        String commitSha,
        String status) {}
