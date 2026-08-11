package com.testmgmt.platform.testsuite.dto;

import java.time.Instant;
import java.util.UUID;

public record TestSuiteCaseDto(UUID testCaseId, String key, String title, int sortOrder, Instant addedAt) {}
