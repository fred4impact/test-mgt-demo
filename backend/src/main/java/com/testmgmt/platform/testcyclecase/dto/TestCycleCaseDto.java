package com.testmgmt.platform.testcyclecase.dto;

import java.time.Instant;
import java.util.UUID;

public record TestCycleCaseDto(
        UUID testCaseId, String key, String title, UUID assigneeId, int sortOrder, Instant addedAt) {}
