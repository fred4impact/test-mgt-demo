package com.testmgmt.platform.testfolder.controller;

import com.testmgmt.platform.testfolder.dto.CreateTestFolderRequest;
import com.testmgmt.platform.testfolder.dto.TestFolderDto;
import com.testmgmt.platform.testfolder.service.TestFolderService;
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
@RequestMapping("/api/v1/projects/{projectId}/test-folders")
public class TestFolderController {

    private final TestFolderService testFolderService;

    public TestFolderController(TestFolderService testFolderService) {
        this.testFolderService = testFolderService;
    }

    @PostMapping
    public ResponseEntity<TestFolderDto> create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTestFolderRequest request) {
        TestFolderDto dto = testFolderService.create(jwt, projectId, request);
        return ResponseEntity.created(URI.create("/api/v1/projects/" + projectId + "/test-folders/" + dto.id()))
                .body(dto);
    }

    @GetMapping("/{id}")
    public TestFolderDto getById(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId, @PathVariable UUID id) {
        return testFolderService.getById(jwt, projectId, id);
    }

    @GetMapping
    public List<TestFolderDto> list(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId) {
        return testFolderService.list(jwt, projectId);
    }
}
