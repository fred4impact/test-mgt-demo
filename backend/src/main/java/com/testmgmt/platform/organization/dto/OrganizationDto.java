package com.testmgmt.platform.organization.dto;

import java.time.Instant;
import java.util.UUID;

public record OrganizationDto(UUID id, String name, String slug, String status, Instant createdAt) {}
