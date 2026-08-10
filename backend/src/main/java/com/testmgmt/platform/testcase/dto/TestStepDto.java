package com.testmgmt.platform.testcase.dto;

import java.util.UUID;

public record TestStepDto(UUID id, int stepNumber, String action, String testData, String expectedResult) {}
