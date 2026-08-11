package com.testmgmt.platform.testcase.controller;

import com.testmgmt.platform.tag.dto.AddTestCaseTagRequest;
import com.testmgmt.platform.tag.dto.TagDto;
import com.testmgmt.platform.tag.service.TestCaseTagService;
import com.testmgmt.platform.testcase.dto.CreateTestCaseRequest;
import com.testmgmt.platform.testcase.dto.TestCaseDto;
import com.testmgmt.platform.testcase.dto.TestCaseVersionDto;
import com.testmgmt.platform.testcase.dto.UpdateTestCaseRequest;
import com.testmgmt.platform.testcase.service.TestCaseService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/test-cases")
public class TestCaseController {

    private final TestCaseService testCaseService;
    private final TestCaseTagService testCaseTagService;

    public TestCaseController(TestCaseService testCaseService, TestCaseTagService testCaseTagService) {
        this.testCaseService = testCaseService;
        this.testCaseTagService = testCaseTagService;
    }

    @PostMapping
    public ResponseEntity<TestCaseDto> create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTestCaseRequest request) {
        TestCaseDto dto = testCaseService.create(jwt, projectId, request);
        return ResponseEntity.created(URI.create("/api/v1/projects/" + projectId + "/test-cases/" + dto.id()))
                .body(dto);
    }

    @GetMapping("/{id}")
    public TestCaseDto getById(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId, @PathVariable UUID id) {
        return testCaseService.getById(jwt, projectId, id);
    }

    @GetMapping
    public List<TestCaseDto> list(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId) {
        return testCaseService.list(jwt, projectId);
    }

    @PutMapping("/{id}")
    public TestCaseDto update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTestCaseRequest request) {
        return testCaseService.update(jwt, projectId, id, request);
    }

    @GetMapping("/{id}/versions")
    public List<TestCaseVersionDto> listVersions(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId, @PathVariable UUID id) {
        return testCaseService.listVersions(jwt, projectId, id);
    }

    @PostMapping("/{testCaseId}/tags")
    public ResponseEntity<TagDto> addTag(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId,
            @PathVariable UUID testCaseId,
            @Valid @RequestBody AddTestCaseTagRequest request) {
        TagDto dto = testCaseTagService.addTag(jwt, projectId, testCaseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{testCaseId}/tags/{tagId}")
    public ResponseEntity<Void> removeTag(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId,
            @PathVariable UUID testCaseId,
            @PathVariable UUID tagId) {
        testCaseTagService.removeTag(jwt, projectId, testCaseId, tagId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{testCaseId}/tags")
    public List<TagDto> listTags(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId, @PathVariable UUID testCaseId) {
        return testCaseTagService.listTags(jwt, projectId, testCaseId);
    }
}
