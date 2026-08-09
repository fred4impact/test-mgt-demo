package com.testmgmt.platform.project.mapper;

import com.testmgmt.platform.project.dto.ProjectDto;
import com.testmgmt.platform.project.entity.Project;

public final class ProjectMapper {

    private ProjectMapper() {}

    public static ProjectDto toDto(Project project) {
        return new ProjectDto(
                project.getId(),
                project.getOrganizationId(),
                project.getKey(),
                project.getName(),
                project.getStatus(),
                project.getOwnerId(),
                project.getCreatedAt());
    }
}
