package com.testmgmt.platform.team.dto;

import java.time.Instant;
import java.util.UUID;

public record TeamMemberDto(UUID userId, String email, String firstName, String lastName, Instant joinedAt) {}
