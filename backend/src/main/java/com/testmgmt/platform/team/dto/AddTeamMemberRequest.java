package com.testmgmt.platform.team.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddTeamMemberRequest(@NotNull(message = "userId is required") UUID userId) {}
