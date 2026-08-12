package com.testmgmt.platform.environment.controller;

import com.testmgmt.platform.environment.dto.CreateEnvironmentRequest;
import com.testmgmt.platform.environment.dto.EnvironmentDto;
import com.testmgmt.platform.environment.service.EnvironmentService;
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
@RequestMapping("/api/v1/projects/{projectId}/environments")
public class EnvironmentController {

    private final EnvironmentService environmentService;

    public EnvironmentController(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @PostMapping
    public ResponseEntity<EnvironmentDto> create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateEnvironmentRequest request) {
        EnvironmentDto dto = environmentService.create(jwt, projectId, request);
        return ResponseEntity.created(URI.create("/api/v1/projects/" + projectId + "/environments/" + dto.id()))
                .body(dto);
    }

    @GetMapping("/{id}")
    public EnvironmentDto getById(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId, @PathVariable UUID id) {
        return environmentService.getById(jwt, projectId, id);
    }

    @GetMapping
    public List<EnvironmentDto> list(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId) {
        return environmentService.list(jwt, projectId);
    }
}
