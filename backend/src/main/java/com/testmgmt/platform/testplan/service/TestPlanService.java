package com.testmgmt.platform.testplan.service;

import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.project.entity.Project;
import com.testmgmt.platform.project.repository.ProjectRepository;
import com.testmgmt.platform.release.repository.ReleaseRepository;
import com.testmgmt.platform.testplan.dto.CreateTestPlanRequest;
import com.testmgmt.platform.testplan.dto.TestPlanDto;
import com.testmgmt.platform.testplan.entity.TestPlan;
import com.testmgmt.platform.testplan.mapper.TestPlanMapper;
import com.testmgmt.platform.testplan.repository.TestPlanRepository;
import com.testmgmt.platform.user.entity.User;
import com.testmgmt.platform.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class TestPlanService {

    private final ProjectRepository projectRepository;
    private final TestPlanRepository testPlanRepository;
    private final ReleaseRepository releaseRepository;
    private final UserService userService;

    public TestPlanService(
            ProjectRepository projectRepository,
            TestPlanRepository testPlanRepository,
            ReleaseRepository releaseRepository,
            UserService userService) {
        this.projectRepository = projectRepository;
        this.testPlanRepository = testPlanRepository;
        this.releaseRepository = releaseRepository;
        this.userService = userService;
    }

    public TestPlanDto create(Jwt jwt, UUID projectId, CreateTestPlanRequest request) {
        User user = userService.resolveOrProvisionUser(jwt);
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, user.getOrganizationId())
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        releaseRepository
                .findByIdAndProjectId(request.releaseId(), project.getId())
                .orElseThrow(() -> new NotFoundException("Release not found: " + request.releaseId()));

        TestPlan testPlan = new TestPlan();
        testPlan.setProjectId(project.getId());
        testPlan.setReleaseId(request.releaseId());
        testPlan.setName(request.name());
        if (request.status() != null) {
            testPlan.setStatus(request.status());
        }
        testPlan.setOwnerId(user.getId());
        testPlan.setStartDate(request.startDate());
        testPlan.setEndDate(request.endDate());

        return TestPlanMapper.toDto(testPlanRepository.save(testPlan));
    }

    public TestPlanDto getById(Jwt jwt, UUID projectId, UUID id) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        TestPlan testPlan = testPlanRepository
                .findByIdAndProjectId(id, project.getId())
                .orElseThrow(() -> new NotFoundException("Test plan not found: " + id));
        return TestPlanMapper.toDto(testPlan);
    }

    public List<TestPlanDto> list(Jwt jwt, UUID projectId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        return testPlanRepository.findByProjectIdOrderByCreatedAtAsc(project.getId()).stream()
                .map(TestPlanMapper::toDto)
                .toList();
    }
}
