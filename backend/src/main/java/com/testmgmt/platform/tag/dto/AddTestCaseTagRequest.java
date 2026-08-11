package com.testmgmt.platform.tag.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddTestCaseTagRequest(@NotNull(message = "tagId is required") UUID tagId) {}
