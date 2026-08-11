package com.testmgmt.platform.release.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReleaseDto(
        UUID id,
        UUID projectId,
        String name,
        String version,
        String status,
        LocalDate startDate,
        LocalDate releaseDate,
        Instant createdAt) {}
