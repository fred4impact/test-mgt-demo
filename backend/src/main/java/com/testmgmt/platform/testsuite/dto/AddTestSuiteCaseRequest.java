package com.testmgmt.platform.testsuite.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddTestSuiteCaseRequest(@NotNull(message = "testCaseId is required") UUID testCaseId) {}
