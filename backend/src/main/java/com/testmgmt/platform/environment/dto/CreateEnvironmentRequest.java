package com.testmgmt.platform.environment.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateEnvironmentRequest(
        @NotBlank(message = "name is required") String name, String type, String url) {}
