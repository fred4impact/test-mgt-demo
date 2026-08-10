package com.testmgmt.platform.testfolder.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreateTestFolderRequest(@NotBlank(message = "name is required") String name, UUID parentId) {}
