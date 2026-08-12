package com.testmgmt.platform.testexecution.controller;

import com.testmgmt.platform.testexecution.dto.TestExecutionDto;
import com.testmgmt.platform.testexecution.dto.UpdateTestExecutionRequest;
import com.testmgmt.platform.testexecution.service.TestExecutionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/test-cycles/{cycleId}/executions")
public class TestExecutionController {

    private final TestExecutionService testExecutionService;

    public TestExecutionController(TestExecutionService testExecutionService) {
        this.testExecutionService = testExecutionService;
    }

    @GetMapping
    public List<TestExecutionDto> list(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId, @PathVariable UUID cycleId) {
        return testExecutionService.list(jwt, projectId, cycleId);
    }

    @GetMapping("/{testCaseId}")
    public TestExecutionDto getById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId,
            @PathVariable UUID cycleId,
            @PathVariable UUID testCaseId) {
        return testExecutionService.getById(jwt, projectId, cycleId, testCaseId);
    }

    @PutMapping("/{testCaseId}")
    public TestExecutionDto update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId,
            @PathVariable UUID cycleId,
            @PathVariable UUID testCaseId,
            @Valid @RequestBody UpdateTestExecutionRequest request) {
        return testExecutionService.update(jwt, projectId, cycleId, testCaseId, request);
    }
}
