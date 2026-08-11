package com.testmgmt.platform.release.controller;

import com.testmgmt.platform.release.dto.CreateReleaseRequest;
import com.testmgmt.platform.release.dto.ReleaseDto;
import com.testmgmt.platform.release.service.ReleaseService;
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
@RequestMapping("/api/v1/projects/{projectId}/releases")
public class ReleaseController {

    private final ReleaseService releaseService;

    public ReleaseController(ReleaseService releaseService) {
        this.releaseService = releaseService;
    }

    @PostMapping
    public ResponseEntity<ReleaseDto> create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateReleaseRequest request) {
        ReleaseDto dto = releaseService.create(jwt, projectId, request);
        return ResponseEntity.created(URI.create("/api/v1/projects/" + projectId + "/releases/" + dto.id()))
                .body(dto);
    }

    @GetMapping("/{id}")
    public ReleaseDto getById(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId, @PathVariable UUID id) {
        return releaseService.getById(jwt, projectId, id);
    }

    @GetMapping
    public List<ReleaseDto> list(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId) {
        return releaseService.list(jwt, projectId);
    }
}
