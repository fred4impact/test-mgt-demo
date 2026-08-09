package com.testmgmt.platform.project.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateProjectRequest(
        @NotBlank(message = "key is required") String key, @NotBlank(message = "name is required") String name) {}
