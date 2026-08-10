package com.testmgmt.platform.testfolder.service;

import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.project.entity.Project;
import com.testmgmt.platform.project.repository.ProjectRepository;
import com.testmgmt.platform.testfolder.dto.CreateTestFolderRequest;
import com.testmgmt.platform.testfolder.dto.TestFolderDto;
import com.testmgmt.platform.testfolder.entity.TestFolder;
import com.testmgmt.platform.testfolder.mapper.TestFolderMapper;
import com.testmgmt.platform.testfolder.repository.TestFolderRepository;
import com.testmgmt.platform.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class TestFolderService {

    private final ProjectRepository projectRepository;
    private final TestFolderRepository testFolderRepository;
    private final UserService userService;

    public TestFolderService(
            ProjectRepository projectRepository,
            TestFolderRepository testFolderRepository,
            UserService userService) {
        this.projectRepository = projectRepository;
        this.testFolderRepository = testFolderRepository;
        this.userService = userService;
    }

    public TestFolderDto create(Jwt jwt, UUID projectId, CreateTestFolderRequest request) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        if (request.parentId() != null) {
            testFolderRepository
                    .findByIdAndProjectId(request.parentId(), project.getId())
                    .orElseThrow(() -> new NotFoundException("Parent folder not found: " + request.parentId()));
        }

        TestFolder folder = new TestFolder();
        folder.setProjectId(project.getId());
        folder.setParentId(request.parentId());
        folder.setName(request.name());

        return TestFolderMapper.toDto(testFolderRepository.save(folder));
    }

    public TestFolderDto getById(Jwt jwt, UUID projectId, UUID id) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        TestFolder folder = testFolderRepository
                .findByIdAndProjectId(id, project.getId())
                .orElseThrow(() -> new NotFoundException("Test folder not found: " + id));
        return TestFolderMapper.toDto(folder);
    }

    public List<TestFolderDto> list(Jwt jwt, UUID projectId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        return testFolderRepository.findByProjectIdOrderByCreatedAtAsc(project.getId()).stream()
                .map(TestFolderMapper::toDto)
                .toList();
    }
}
