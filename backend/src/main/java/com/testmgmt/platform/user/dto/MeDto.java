package com.testmgmt.platform.user.dto;

import java.util.UUID;

public record MeDto(
        UUID id, String email, String firstName, String lastName, UUID organizationId, String organizationName) {}
