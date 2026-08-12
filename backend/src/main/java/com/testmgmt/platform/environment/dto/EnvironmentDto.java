package com.testmgmt.platform.environment.dto;

import java.time.Instant;
import java.util.UUID;

public record EnvironmentDto(UUID id, UUID projectId, String name, String type, String url, Instant createdAt) {}
