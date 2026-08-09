package com.testmgmt.platform.team.service;

import com.testmgmt.platform.common.error.ConflictException;
import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.team.dto.AddTeamMemberRequest;
import com.testmgmt.platform.team.dto.TeamMemberDto;
import com.testmgmt.platform.team.entity.Team;
import com.testmgmt.platform.team.entity.TeamMember;
import com.testmgmt.platform.team.repository.TeamMemberRepository;
import com.testmgmt.platform.team.repository.TeamRepository;
import com.testmgmt.platform.user.entity.User;
import com.testmgmt.platform.user.repository.UserRepository;
import com.testmgmt.platform.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class TeamMemberService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public TeamMemberService(
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            UserRepository userRepository,
            UserService userService) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public TeamMemberDto addMember(Jwt jwt, UUID teamId, AddTeamMemberRequest request) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Team team = teamRepository
                .findByIdAndOrganizationId(teamId, organizationId)
                .orElseThrow(() -> new NotFoundException("Team not found: " + teamId));

        User target = userRepository
                .findById(request.userId())
                .filter(u -> u.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new NotFoundException("User not found: " + request.userId()));

        if (teamMemberRepository.existsByTeamIdAndUserId(team.getId(), target.getId())) {
            throw new ConflictException("User is already a member of this team");
        }

        TeamMember member = new TeamMember();
        member.setTeamId(team.getId());
        member.setUserId(target.getId());
        TeamMember saved = teamMemberRepository.save(member);

        return toDto(saved, target);
    }

    public void removeMember(Jwt jwt, UUID teamId, UUID userId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Team team = teamRepository
                .findByIdAndOrganizationId(teamId, organizationId)
                .orElseThrow(() -> new NotFoundException("Team not found: " + teamId));

        TeamMember member = teamMemberRepository
                .findByTeamIdAndUserId(team.getId(), userId)
                .orElseThrow(() -> new NotFoundException("User is not a member of this team: " + userId));

        teamMemberRepository.delete(member);
    }

    public List<TeamMemberDto> listMembers(Jwt jwt, UUID teamId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Team team = teamRepository
                .findByIdAndOrganizationId(teamId, organizationId)
                .orElseThrow(() -> new NotFoundException("Team not found: " + teamId));

        return teamMemberRepository.findByTeamId(team.getId()).stream()
                .map(member -> toDto(
                        member,
                        userRepository
                                .findById(member.getUserId())
                                .orElseThrow(() -> new NotFoundException(
                                        "User not found: " + member.getUserId()))))
                .toList();
    }

    private TeamMemberDto toDto(TeamMember member, User user) {
        return new TeamMemberDto(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), member.getCreatedAt());
    }
}
