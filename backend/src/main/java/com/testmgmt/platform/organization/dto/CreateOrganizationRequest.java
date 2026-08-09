package com.testmgmt.platform.organization.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOrganizationRequest(
        @NotBlank(message = "name is required") String name,
        @NotBlank(message = "slug is required") String slug,
        String description) {}
