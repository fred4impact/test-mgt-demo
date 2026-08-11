package com.testmgmt.platform.build.dto;

import java.time.Instant;
import java.util.UUID;

public record BuildDto(
        UUID id,
        UUID projectId,
        UUID releaseId,
        String name,
        String version,
        String branch,
        String commitSha,
        String status,
        Instant createdAt) {}
