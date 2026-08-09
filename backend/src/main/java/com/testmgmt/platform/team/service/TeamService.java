package com.testmgmt.platform.team.service;

import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.team.dto.CreateTeamRequest;
import com.testmgmt.platform.team.dto.TeamDto;
import com.testmgmt.platform.team.entity.Team;
import com.testmgmt.platform.team.mapper.TeamMapper;
import com.testmgmt.platform.team.repository.TeamRepository;
import com.testmgmt.platform.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserService userService;

    public TeamService(TeamRepository teamRepository, UserService userService) {
        this.teamRepository = teamRepository;
        this.userService = userService;
    }

    public TeamDto create(Jwt jwt, CreateTeamRequest request) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();

        Team team = new Team();
        team.setOrganizationId(organizationId);
        team.setName(request.name());
        team.setDescription(request.description());
        return TeamMapper.toDto(teamRepository.save(team));
    }

    public TeamDto getById(Jwt jwt, UUID id) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Team team = teamRepository
                .findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new NotFoundException("Team not found: " + id));
        return TeamMapper.toDto(team);
    }

    public List<TeamDto> list(Jwt jwt) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        return teamRepository.findByOrganizationId(organizationId).stream()
                .map(TeamMapper::toDto)
                .toList();
    }
}
