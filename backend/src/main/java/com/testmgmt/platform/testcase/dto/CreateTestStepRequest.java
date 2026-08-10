package com.testmgmt.platform.testcase.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTestStepRequest(
        @NotBlank(message = "action is required") String action, String testData, String expectedResult) {}
