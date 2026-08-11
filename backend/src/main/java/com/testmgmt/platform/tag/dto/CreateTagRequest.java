package com.testmgmt.platform.tag.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTagRequest(@NotBlank(message = "name is required") String name) {}
