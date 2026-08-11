package com.testmgmt.platform.release.mapper;

import com.testmgmt.platform.release.dto.ReleaseDto;
import com.testmgmt.platform.release.entity.Release;

public final class ReleaseMapper {

    private ReleaseMapper() {}

    public static ReleaseDto toDto(Release release) {
        return new ReleaseDto(
                release.getId(),
                release.getProjectId(),
                release.getName(),
                release.getVersion(),
                release.getStatus(),
                release.getStartDate(),
                release.getReleaseDate(),
                release.getCreatedAt());
    }
}
