package com.testmgmt.platform.testsuite.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreateTestSuiteRequest(@NotBlank(message = "name is required") String name, UUID parentId) {}
