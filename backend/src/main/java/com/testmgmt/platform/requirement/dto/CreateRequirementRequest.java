package com.testmgmt.platform.requirement.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreateRequirementRequest(
        @NotBlank(message = "title is required") String title, String status, String priority, UUID releaseId) {}
