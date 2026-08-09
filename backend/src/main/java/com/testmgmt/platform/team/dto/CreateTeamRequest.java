package com.testmgmt.platform.team.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTeamRequest(@NotBlank(message = "name is required") String name, String description) {}
