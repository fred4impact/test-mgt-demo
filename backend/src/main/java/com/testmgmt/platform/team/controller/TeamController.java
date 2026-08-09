package com.testmgmt.platform.team.controller;

import com.testmgmt.platform.team.dto.AddTeamMemberRequest;
import com.testmgmt.platform.team.dto.CreateTeamRequest;
import com.testmgmt.platform.team.dto.TeamDto;
import com.testmgmt.platform.team.dto.TeamMemberDto;
import com.testmgmt.platform.team.service.TeamMemberService;
import com.testmgmt.platform.team.service.TeamService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamController {

    private final TeamService teamService;
    private final TeamMemberService teamMemberService;

    public TeamController(TeamService teamService, TeamMemberService teamMemberService) {
        this.teamService = teamService;
        this.teamMemberService = teamMemberService;
    }

    @PostMapping
    public ResponseEntity<TeamDto> create(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateTeamRequest request) {
        TeamDto dto = teamService.create(jwt, request);
        return ResponseEntity.created(URI.create("/api/v1/teams/" + dto.id())).body(dto);
    }

    @GetMapping("/{id}")
    public TeamDto getById(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return teamService.getById(jwt, id);
    }

    @GetMapping
    public List<TeamDto> list(@AuthenticationPrincipal Jwt jwt) {
        return teamService.list(jwt);
    }

    @PostMapping("/{teamId}/members")
    public ResponseEntity<TeamMemberDto> addMember(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID teamId,
            @Valid @RequestBody AddTeamMemberRequest request) {
        TeamMemberDto dto = teamMemberService.addMember(jwt, teamId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID teamId, @PathVariable UUID userId) {
        teamMemberService.removeMember(jwt, teamId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{teamId}/members")
    public List<TeamMemberDto> listMembers(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID teamId) {
        return teamMemberService.listMembers(jwt, teamId);
    }
}
