package com.testmgmt.platform.testplan.controller;

import com.testmgmt.platform.testplan.dto.CreateTestPlanRequest;
import com.testmgmt.platform.testplan.dto.TestPlanDto;
import com.testmgmt.platform.testplan.service.TestPlanService;
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
@RequestMapping("/api/v1/projects/{projectId}/test-plans")
public class TestPlanController {

    private final TestPlanService testPlanService;

    public TestPlanController(TestPlanService testPlanService) {
        this.testPlanService = testPlanService;
    }

    @PostMapping
    public ResponseEntity<TestPlanDto> create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTestPlanRequest request) {
        TestPlanDto dto = testPlanService.create(jwt, projectId, request);
        return ResponseEntity.created(URI.create("/api/v1/projects/" + projectId + "/test-plans/" + dto.id()))
                .body(dto);
    }

    @GetMapping("/{id}")
    public TestPlanDto getById(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId, @PathVariable UUID id) {
        return testPlanService.getById(jwt, projectId, id);
    }

    @GetMapping
    public List<TestPlanDto> list(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId) {
        return testPlanService.list(jwt, projectId);
    }
}
