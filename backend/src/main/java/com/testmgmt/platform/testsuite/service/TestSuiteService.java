package com.testmgmt.platform.testsuite.service;

import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.project.entity.Project;
import com.testmgmt.platform.project.repository.ProjectRepository;
import com.testmgmt.platform.testsuite.dto.CreateTestSuiteRequest;
import com.testmgmt.platform.testsuite.dto.TestSuiteDto;
import com.testmgmt.platform.testsuite.entity.TestSuite;
import com.testmgmt.platform.testsuite.mapper.TestSuiteMapper;
import com.testmgmt.platform.testsuite.repository.TestSuiteRepository;
import com.testmgmt.platform.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class TestSuiteService {

    private final ProjectRepository projectRepository;
    private final TestSuiteRepository testSuiteRepository;
    private final UserService userService;

    public TestSuiteService(
            ProjectRepository projectRepository, TestSuiteRepository testSuiteRepository, UserService userService) {
        this.projectRepository = projectRepository;
        this.testSuiteRepository = testSuiteRepository;
        this.userService = userService;
    }

    public TestSuiteDto create(Jwt jwt, UUID projectId, CreateTestSuiteRequest request) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        if (request.parentId() != null) {
            testSuiteRepository
                    .findByIdAndProjectId(request.parentId(), project.getId())
                    .orElseThrow(() -> new NotFoundException("Parent suite not found: " + request.parentId()));
        }

        TestSuite suite = new TestSuite();
        suite.setProjectId(project.getId());
        suite.setParentId(request.parentId());
        suite.setName(request.name());

        return TestSuiteMapper.toDto(testSuiteRepository.save(suite));
    }

    public TestSuiteDto getById(Jwt jwt, UUID projectId, UUID id) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        TestSuite suite = testSuiteRepository
                .findByIdAndProjectId(id, project.getId())
                .orElseThrow(() -> new NotFoundException("Test suite not found: " + id));
        return TestSuiteMapper.toDto(suite);
    }

    public List<TestSuiteDto> list(Jwt jwt, UUID projectId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        return testSuiteRepository.findByProjectIdOrderByCreatedAtAsc(project.getId()).stream()
                .map(TestSuiteMapper::toDto)
                .toList();
    }
}
