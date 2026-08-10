package com.testmgmt.platform.testcase.dto;

import java.time.Instant;
import java.util.UUID;

public record TestCaseVersionDto(UUID id, int versionNumber, String snapshot, String changeSummary, Instant createdAt) {}
