package com.testmgmt.platform.testsuite.dto;

import java.time.Instant;
import java.util.UUID;

public record TestSuiteDto(UUID id, UUID projectId, UUID parentId, String name, Instant createdAt) {}
