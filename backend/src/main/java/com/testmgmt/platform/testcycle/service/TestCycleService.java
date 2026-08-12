package com.testmgmt.platform.testcycle.service;

import com.testmgmt.platform.build.repository.BuildRepository;
import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.environment.repository.EnvironmentRepository;
import com.testmgmt.platform.project.entity.Project;
import com.testmgmt.platform.project.repository.ProjectRepository;
import com.testmgmt.platform.release.repository.ReleaseRepository;
import com.testmgmt.platform.testcycle.dto.CreateTestCycleRequest;
import com.testmgmt.platform.testcycle.dto.TestCycleDto;
import com.testmgmt.platform.testcycle.entity.TestCycle;
import com.testmgmt.platform.testcycle.mapper.TestCycleMapper;
import com.testmgmt.platform.testcycle.repository.TestCycleRepository;
import com.testmgmt.platform.testplan.repository.TestPlanRepository;
import com.testmgmt.platform.user.entity.User;
import com.testmgmt.platform.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class TestCycleService {

    private final ProjectRepository projectRepository;
    private final TestCycleRepository testCycleRepository;
    private final TestPlanRepository testPlanRepository;
    private final ReleaseRepository releaseRepository;
    private final BuildRepository buildRepository;
    private final EnvironmentRepository environmentRepository;
    private final UserService userService;

    public TestCycleService(
            ProjectRepository projectRepository,
            TestCycleRepository testCycleRepository,
            TestPlanRepository testPlanRepository,
            ReleaseRepository releaseRepository,
            BuildRepository buildRepository,
            EnvironmentRepository environmentRepository,
            UserService userService) {
        this.projectRepository = projectRepository;
        this.testCycleRepository = testCycleRepository;
        this.testPlanRepository = testPlanRepository;
        this.releaseRepository = releaseRepository;
        this.buildRepository = buildRepository;
        this.environmentRepository = environmentRepository;
        this.userService = userService;
    }

    public TestCycleDto create(Jwt jwt, UUID projectId, CreateTestCycleRequest request) {
        User user = userService.resolveOrProvisionUser(jwt);
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, user.getOrganizationId())
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        testPlanRepository
                .findByIdAndProjectId(request.testPlanId(), project.getId())
                .orElseThrow(() -> new NotFoundException("Test plan not found: " + request.testPlanId()));
        releaseRepository
                .findByIdAndProjectId(request.releaseId(), project.getId())
                .orElseThrow(() -> new NotFoundException("Release not found: " + request.releaseId()));
        buildRepository
                .findByIdAndProjectId(request.buildId(), project.getId())
                .orElseThrow(() -> new NotFoundException("Build not found: " + request.buildId()));
        environmentRepository
                .findByIdAndProjectId(request.environmentId(), project.getId())
                .orElseThrow(() -> new NotFoundException("Environment not found: " + request.environmentId()));

        TestCycle testCycle = new TestCycle();
        testCycle.setProjectId(project.getId());
        testCycle.setTestPlanId(request.testPlanId());
        testCycle.setReleaseId(request.releaseId());
        testCycle.setBuildId(request.buildId());
        testCycle.setEnvironmentId(request.environmentId());
        testCycle.setName(request.name());
        if (request.status() != null) {
            testCycle.setStatus(request.status());
        }
        testCycle.setOwnerId(user.getId());
        testCycle.setStartDate(request.startDate());
        testCycle.setEndDate(request.endDate());

        return TestCycleMapper.toDto(testCycleRepository.save(testCycle));
    }

    public TestCycleDto getById(Jwt jwt, UUID projectId, UUID id) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        TestCycle testCycle = testCycleRepository
                .findByIdAndProjectId(id, project.getId())
                .orElseThrow(() -> new NotFoundException("Test cycle not found: " + id));
        return TestCycleMapper.toDto(testCycle);
    }

    public List<TestCycleDto> list(Jwt jwt, UUID projectId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        return testCycleRepository.findByProjectIdOrderByCreatedAtAsc(project.getId()).stream()
                .map(TestCycleMapper::toDto)
                .toList();
    }
}
