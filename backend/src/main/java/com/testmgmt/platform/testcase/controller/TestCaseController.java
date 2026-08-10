package com.testmgmt.platform.testcase.controller;

import com.testmgmt.platform.testcase.dto.CreateTestCaseRequest;
import com.testmgmt.platform.testcase.dto.TestCaseDto;
import com.testmgmt.platform.testcase.service.TestCaseService;
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
@RequestMapping("/api/v1/projects/{projectId}/test-cases")
public class TestCaseController {

    private final TestCaseService testCaseService;

    public TestCaseController(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
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
}
