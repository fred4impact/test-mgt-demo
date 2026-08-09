package com.testmgmt.platform.team.mapper;

import com.testmgmt.platform.team.dto.TeamDto;
import com.testmgmt.platform.team.entity.Team;

public final class TeamMapper {

    private TeamMapper() {}

    public static TeamDto toDto(Team team) {
        return new TeamDto(
                team.getId(), team.getOrganizationId(), team.getName(), team.getDescription(), team.getCreatedAt());
    }
}
