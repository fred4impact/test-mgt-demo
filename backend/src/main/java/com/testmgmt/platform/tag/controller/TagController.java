package com.testmgmt.platform.tag.controller;

import com.testmgmt.platform.tag.dto.CreateTagRequest;
import com.testmgmt.platform.tag.dto.TagDto;
import com.testmgmt.platform.tag.service.TagService;
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
@RequestMapping("/api/v1/projects/{projectId}/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    public ResponseEntity<TagDto> create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTagRequest request) {
        TagDto dto = tagService.create(jwt, projectId, request);
        return ResponseEntity.created(URI.create("/api/v1/projects/" + projectId + "/tags/" + dto.id())).body(dto);
    }

    @GetMapping
    public List<TagDto> list(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId) {
        return tagService.list(jwt, projectId);
    }
}
