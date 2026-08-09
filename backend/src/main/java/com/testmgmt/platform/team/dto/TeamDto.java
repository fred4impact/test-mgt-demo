package com.testmgmt.platform.team.dto;

import java.time.Instant;
import java.util.UUID;

public record TeamDto(UUID id, UUID organizationId, String name, String description, Instant createdAt) {}
