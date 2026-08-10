package com.testmgmt.platform.testfolder.dto;

import java.time.Instant;
import java.util.UUID;

public record TestFolderDto(UUID id, UUID projectId, UUID parentId, String name, Instant createdAt) {}
