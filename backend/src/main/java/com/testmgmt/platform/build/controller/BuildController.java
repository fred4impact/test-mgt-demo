package com.testmgmt.platform.build.controller;

import com.testmgmt.platform.build.dto.BuildDto;
import com.testmgmt.platform.build.dto.CreateBuildRequest;
import com.testmgmt.platform.build.service.BuildService;
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
@RequestMapping("/api/v1/projects/{projectId}/builds")
public class BuildController {

    private final BuildService buildService;

    public BuildController(BuildService buildService) {
        this.buildService = buildService;
    }

    @PostMapping
    public ResponseEntity<BuildDto> create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateBuildRequest request) {
        BuildDto dto = buildService.create(jwt, projectId, request);
        return ResponseEntity.created(URI.create("/api/v1/projects/" + projectId + "/builds/" + dto.id()))
                .body(dto);
    }

    @GetMapping("/{id}")
    public BuildDto getById(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId, @PathVariable UUID id) {
        return buildService.getById(jwt, projectId, id);
    }

    @GetMapping
    public List<BuildDto> list(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId) {
        return buildService.list(jwt, projectId);
    }
}
