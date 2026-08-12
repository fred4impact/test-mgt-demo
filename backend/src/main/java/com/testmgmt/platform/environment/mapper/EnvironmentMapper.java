package com.testmgmt.platform.environment.mapper;

import com.testmgmt.platform.environment.dto.EnvironmentDto;
import com.testmgmt.platform.environment.entity.Environment;

public final class EnvironmentMapper {

    private EnvironmentMapper() {}

    public static EnvironmentDto toDto(Environment environment) {
        return new EnvironmentDto(
                environment.getId(),
                environment.getProjectId(),
                environment.getName(),
                environment.getType(),
                environment.getUrl(),
                environment.getCreatedAt());
    }
}
