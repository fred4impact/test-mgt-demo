package com.testmgmt.platform.testexecution.service;

import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.project.entity.Project;
import com.testmgmt.platform.project.repository.ProjectRepository;
import com.testmgmt.platform.testcycle.entity.TestCycle;
import com.testmgmt.platform.testcycle.repository.TestCycleRepository;
import com.testmgmt.platform.testcyclecase.entity.TestCycleCase;
import com.testmgmt.platform.testcyclecase.repository.TestCycleCaseRepository;
import com.testmgmt.platform.testexecution.dto.TestExecutionDto;
import com.testmgmt.platform.testexecution.dto.UpdateTestExecutionRequest;
import com.testmgmt.platform.testexecution.entity.TestExecution;
import com.testmgmt.platform.testexecution.mapper.TestExecutionMapper;
import com.testmgmt.platform.testexecution.repository.TestExecutionRepository;
import com.testmgmt.platform.user.service.UserService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class TestExecutionService {

    private final ProjectRepository projectRepository;
    private final TestCycleRepository testCycleRepository;
    private final TestCycleCaseRepository testCycleCaseRepository;
    private final TestExecutionRepository testExecutionRepository;
    private final UserService userService;

    public TestExecutionService(
            ProjectRepository projectRepository,
            TestCycleRepository testCycleRepository,
            TestCycleCaseRepository testCycleCaseRepository,
            TestExecutionRepository testExecutionRepository,
            UserService userService) {
        this.projectRepository = projectRepository;
        this.testCycleRepository = testCycleRepository;
        this.testCycleCaseRepository = testCycleCaseRepository;
        this.testExecutionRepository = testExecutionRepository;
        this.userService = userService;
    }

    public List<TestExecutionDto> list(Jwt jwt, UUID projectId, UUID cycleId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));
        TestCycle cycle = testCycleRepository
                .findByIdAndProjectId(cycleId, project.getId())
                .orElseThrow(() -> new NotFoundException("Test cycle not found: " + cycleId));

        List<TestCycleCase> cycleCases = testCycleCaseRepository.findByCycleIdOrderBySortOrderAsc(cycle.getId());
        Map<UUID, TestExecution> existingByTestCaseId = testExecutionRepository.findByCycleId(cycle.getId()).stream()
                .collect(Collectors.toMap(TestExecution::getTestCaseId, Function.identity()));

        return cycleCases.stream()
                .map(cycleCase -> {
                    TestExecution existing = existingByTestCaseId.get(cycleCase.getTestCaseId());
                    return existing != null ? existing : createExecution(project.getId(), cycle, cycleCase);
                })
                .map(TestExecutionMapper::toDto)
                .toList();
    }

    public TestExecutionDto getById(Jwt jwt, UUID projectId, UUID cycleId, UUID testCaseId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));
        TestCycle cycle = testCycleRepository
                .findByIdAndProjectId(cycleId, project.getId())
                .orElseThrow(() -> new NotFoundException("Test cycle not found: " + cycleId));
        TestCycleCase cycleCase = testCycleCaseRepository
                .findByCycleIdAndTestCaseId(cycle.getId(), testCaseId)
                .orElseThrow(() -> new NotFoundException("Test case is not in this cycle: " + testCaseId));

        TestExecution execution = testExecutionRepository
                .findByCycleIdAndTestCaseId(cycle.getId(), testCaseId)
                .orElseGet(() -> createExecution(project.getId(), cycle, cycleCase));

        return TestExecutionMapper.toDto(execution);
    }

    public TestExecutionDto update(
            Jwt jwt, UUID projectId, UUID cycleId, UUID testCaseId, UpdateTestExecutionRequest request) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));
        TestCycle cycle = testCycleRepository
                .findByIdAndProjectId(cycleId, project.getId())
                .orElseThrow(() -> new NotFoundException("Test cycle not found: " + cycleId));
        TestCycleCase cycleCase = testCycleCaseRepository
                .findByCycleIdAndTestCaseId(cycle.getId(), testCaseId)
                .orElseThrow(() -> new NotFoundException("Test case is not in this cycle: " + testCaseId));

        TestExecution execution = testExecutionRepository
                .findByCycleIdAndTestCaseId(cycle.getId(), testCaseId)
                .orElseGet(() -> createExecution(project.getId(), cycle, cycleCase));

        if (execution.getStartedAt() == null) {
            execution.setStartedAt(Instant.now());
        }
        execution.setStatus(request.status());
        if (request.status().isTerminal()) {
            Instant now = Instant.now();
            execution.setCompletedAt(now);
            execution.setDurationMs(Duration.between(execution.getStartedAt(), now).toMillis());
        }
        if (request.actualResult() != null) {
            execution.setActualResult(request.actualResult());
        }
        if (request.comment() != null) {
            execution.setComment(request.comment());
        }

        return TestExecutionMapper.toDto(testExecutionRepository.save(execution));
    }

    private TestExecution createExecution(UUID projectId, TestCycle cycle, TestCycleCase cycleCase) {
        TestExecution execution = new TestExecution();
        execution.setProjectId(projectId);
        execution.setCycleId(cycle.getId());
        execution.setTestCaseId(cycleCase.getTestCaseId());
        execution.setAssigneeId(cycleCase.getAssigneeId());
        execution.setEnvironmentId(cycle.getEnvironmentId());
        execution.setBuildId(cycle.getBuildId());
        return testExecutionRepository.save(execution);
    }
}
