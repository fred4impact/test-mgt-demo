package com.testmgmt.platform.requirement.mapper;

import com.testmgmt.platform.requirement.dto.RequirementDto;
import com.testmgmt.platform.requirement.entity.Requirement;

public final class RequirementMapper {

    private RequirementMapper() {}

    public static RequirementDto toDto(Requirement requirement) {
        return new RequirementDto(
                requirement.getId(),
                requirement.getProjectId(),
                requirement.getKey(),
                requirement.getTitle(),
                requirement.getStatus(),
                requirement.getPriority(),
                requirement.getOwnerId(),
                requirement.getCreatedAt());
    }
}
