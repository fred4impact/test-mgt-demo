package com.testmgmt.platform.project.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddProjectMemberRequest(
        @NotNull(message = "userId is required") UUID userId,
        @NotNull(message = "roleId is required") UUID roleId) {}
