package com.testmgmt.platform.testsuite.controller;

import com.testmgmt.platform.testsuite.dto.AddTestSuiteCaseRequest;
import com.testmgmt.platform.testsuite.dto.CreateTestSuiteRequest;
import com.testmgmt.platform.testsuite.dto.TestSuiteCaseDto;
import com.testmgmt.platform.testsuite.dto.TestSuiteDto;
import com.testmgmt.platform.testsuite.service.TestSuiteCaseService;
import com.testmgmt.platform.testsuite.service.TestSuiteService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/test-suites")
public class TestSuiteController {

    private final TestSuiteService testSuiteService;
    private final TestSuiteCaseService testSuiteCaseService;

    public TestSuiteController(TestSuiteService testSuiteService, TestSuiteCaseService testSuiteCaseService) {
        this.testSuiteService = testSuiteService;
        this.testSuiteCaseService = testSuiteCaseService;
    }

    @PostMapping
    public ResponseEntity<TestSuiteDto> create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTestSuiteRequest request) {
        TestSuiteDto dto = testSuiteService.create(jwt, projectId, request);
        return ResponseEntity.created(URI.create("/api/v1/projects/" + projectId + "/test-suites/" + dto.id()))
                .body(dto);
    }

    @GetMapping("/{id}")
    public TestSuiteDto getById(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId, @PathVariable UUID id) {
        return testSuiteService.getById(jwt, projectId, id);
    }

    @GetMapping
    public List<TestSuiteDto> list(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId) {
        return testSuiteService.list(jwt, projectId);
    }

    @PostMapping("/{suiteId}/cases")
    public ResponseEntity<TestSuiteCaseDto> addCase(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId,
            @PathVariable UUID suiteId,
            @Valid @RequestBody AddTestSuiteCaseRequest request) {
        TestSuiteCaseDto dto = testSuiteCaseService.addCase(jwt, projectId, suiteId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{suiteId}/cases/{testCaseId}")
    public ResponseEntity<Void> removeCase(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID projectId,
            @PathVariable UUID suiteId,
            @PathVariable UUID testCaseId) {
        testSuiteCaseService.removeCase(jwt, projectId, suiteId, testCaseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{suiteId}/cases")
    public List<TestSuiteCaseDto> listCases(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId, @PathVariable UUID suiteId) {
        return testSuiteCaseService.listCases(jwt, projectId, suiteId);
    }
}
