package com.testmgmt.platform.testcycle.controller;

import com.testmgmt.platform.testcycle.dto.CreateTestCycleRequest;
import com.testmgmt.platform.testcycle.dto.TestCycleDto;
import com.testmgmt.platform.testcycle.service.TestCycleService;
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
@RequestMapping("/api/v1/projects/{projectId}/test-cycles")
public class TestCycleController {

    private final TestCycleService testCycleService;

    public TestCycleController(TestCycleService testCycleService) {
        this.testCycleService = testCycleService;
    }

    @PostMapping
    public ResponseEntity<TestCycleDto> create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTestCycleRequest request) {
        TestCycleDto dto = testCycleService.create(jwt, projectId, request);
        return ResponseEntity.created(URI.create("/api/v1/projects/" + projectId + "/test-cycles/" + dto.id()))
                .body(dto);
    }

    @GetMapping("/{id}")
    public TestCycleDto getById(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId, @PathVariable UUID id) {
        return testCycleService.getById(jwt, projectId, id);
    }

    @GetMapping
    public List<TestCycleDto> list(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId) {
        return testCycleService.list(jwt, projectId);
    }
}
