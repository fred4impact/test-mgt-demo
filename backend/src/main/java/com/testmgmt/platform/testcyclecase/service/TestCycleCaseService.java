package com.testmgmt.platform.testcyclecase.service;

import com.testmgmt.platform.common.error.ConflictException;
import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.project.entity.Project;
import com.testmgmt.platform.project.repository.ProjectMemberRepository;
import com.testmgmt.platform.project.repository.ProjectRepository;
import com.testmgmt.platform.testcase.entity.TestCase;
import com.testmgmt.platform.testcase.repository.TestCaseRepository;
import com.testmgmt.platform.testcycle.entity.TestCycle;
import com.testmgmt.platform.testcycle.repository.TestCycleRepository;
import com.testmgmt.platform.testcyclecase.dto.AddTestCycleCaseRequest;
import com.testmgmt.platform.testcyclecase.dto.TestCycleCaseDto;
import com.testmgmt.platform.testcyclecase.entity.TestCycleCase;
import com.testmgmt.platform.testcyclecase.mapper.TestCycleCaseMapper;
import com.testmgmt.platform.testcyclecase.repository.TestCycleCaseRepository;
import com.testmgmt.platform.user.service.UserService;
import java.util.UUID;
import java.util.List;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class TestCycleCaseService {

    private final ProjectRepository projectRepository;
    private final TestCycleRepository testCycleRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestCycleCaseRepository testCycleCaseRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserService userService;

    public TestCycleCaseService(
            ProjectRepository projectRepository,
            TestCycleRepository testCycleRepository,
            TestCaseRepository testCaseRepository,
            TestCycleCaseRepository testCycleCaseRepository,
            ProjectMemberRepository projectMemberRepository,
            UserService userService) {
        this.projectRepository = projectRepository;
        this.testCycleRepository = testCycleRepository;
        this.testCaseRepository = testCaseRepository;
        this.testCycleCaseRepository = testCycleCaseRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userService = userService;
    }

    public TestCycleCaseDto addCase(Jwt jwt, UUID projectId, UUID cycleId, AddTestCycleCaseRequest request) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        TestCycle cycle = testCycleRepository
                .findByIdAndProjectId(cycleId, project.getId())
                .orElseThrow(() -> new NotFoundException("Test cycle not found: " + cycleId));

        TestCase testCase = testCaseRepository
                .findByIdAndProjectId(request.testCaseId(), project.getId())
                .orElseThrow(() -> new NotFoundException("Test case not found: " + request.testCaseId()));

        if (request.assigneeId() != null
                && !projectMemberRepository.existsByProjectIdAndUserId(project.getId(), request.assigneeId())) {
            throw new NotFoundException("Assignee is not a project member: " + request.assigneeId());
        }

        if (testCycleCaseRepository.existsByCycleIdAndTestCaseId(cycle.getId(), testCase.getId())) {
            throw new ConflictException("Test case is already in this cycle");
        }

        long nextSortOrder = testCycleCaseRepository.countByCycleId(cycle.getId()) + 1;

        TestCycleCase cycleCase = new TestCycleCase();
        cycleCase.setCycleId(cycle.getId());
        cycleCase.setTestCaseId(testCase.getId());
        cycleCase.setAssigneeId(request.assigneeId());
        cycleCase.setSortOrder((int) nextSortOrder);
        TestCycleCase saved = testCycleCaseRepository.save(cycleCase);

        return TestCycleCaseMapper.toDto(saved, testCase);
    }

    public void removeCase(Jwt jwt, UUID projectId, UUID cycleId, UUID testCaseId) {
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

        testCycleCaseRepository.delete(cycleCase);
    }

    public List<TestCycleCaseDto> listCases(Jwt jwt, UUID projectId, UUID cycleId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        TestCycle cycle = testCycleRepository
                .findByIdAndProjectId(cycleId, project.getId())
                .orElseThrow(() -> new NotFoundException("Test cycle not found: " + cycleId));

        return testCycleCaseRepository.findByCycleIdOrderBySortOrderAsc(cycle.getId()).stream()
                .map(cycleCase -> TestCycleCaseMapper.toDto(
                        cycleCase,
                        testCaseRepository
                                .findById(cycleCase.getTestCaseId())
                                .orElseThrow(() -> new NotFoundException(
                                        "Test case not found: " + cycleCase.getTestCaseId()))))
                .toList();
    }
}
