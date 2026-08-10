package com.testmgmt.platform.project.dto;

import java.time.Instant;
import java.util.UUID;

public record ProjectMemberDto(
        UUID userId,
        String email,
        String firstName,
        String lastName,
        UUID roleId,
        String roleName,
        Instant joinedAt) {}
