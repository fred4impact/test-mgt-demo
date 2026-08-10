package com.testmgmt.platform.requirement.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateRequirementRequest(
        @NotBlank(message = "title is required") String title, String status, String priority) {}
