package com.testmgmt.platform.tag.dto;

import java.time.Instant;
import java.util.UUID;

public record TagDto(UUID id, UUID projectId, String name, Instant createdAt) {}
