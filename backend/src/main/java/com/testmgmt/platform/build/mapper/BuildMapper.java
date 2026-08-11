package com.testmgmt.platform.build.mapper;

import com.testmgmt.platform.build.dto.BuildDto;
import com.testmgmt.platform.build.entity.Build;

public final class BuildMapper {

    private BuildMapper() {}

    public static BuildDto toDto(Build build) {
        return new BuildDto(
                build.getId(),
                build.getProjectId(),
                build.getReleaseId(),
                build.getName(),
                build.getVersion(),
                build.getBranch(),
                build.getCommitSha(),
                build.getStatus(),
                build.getCreatedAt());
    }
}
