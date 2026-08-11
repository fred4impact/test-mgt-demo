package com.testmgmt.platform.release.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateReleaseRequest(
        @NotBlank(message = "name is required") String name,
        String version,
        String status,
        LocalDate startDate,
        LocalDate releaseDate) {}
