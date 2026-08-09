package com.testmgmt.platform.project.controller;

import com.testmgmt.platform.project.dto.CreateProjectRequest;
import com.testmgmt.platform.project.dto.ProjectDto;
import com.testmgmt.platform.project.service.ProjectService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectDto> create(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateProjectRequest request) {
        ProjectDto dto = projectService.create(jwt, request);
        return ResponseEntity.created(URI.create("/api/v1/projects/" + dto.id())).body(dto);
    }

    @GetMapping("/{id}")
    public ProjectDto getById(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return projectService.getById(jwt, id);
    }

    @GetMapping
    public List<ProjectDto> list(@AuthenticationPrincipal Jwt jwt) {
        return projectService.list(jwt);
    }
}
