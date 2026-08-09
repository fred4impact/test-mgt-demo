package com.testmgmt.platform.project.service;

import com.testmgmt.platform.common.error.ConflictException;
import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.project.dto.CreateProjectRequest;
import com.testmgmt.platform.project.dto.ProjectDto;
import com.testmgmt.platform.project.entity.Project;
import com.testmgmt.platform.project.mapper.ProjectMapper;
import com.testmgmt.platform.project.repository.ProjectRepository;
import com.testmgmt.platform.user.entity.User;
import com.testmgmt.platform.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService;

    public ProjectService(ProjectRepository projectRepository, UserService userService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    public ProjectDto create(Jwt jwt, CreateProjectRequest request) {
        User user = userService.resolveOrProvisionUser(jwt);
        UUID organizationId = user.getOrganizationId();

        if (projectRepository.existsByOrganizationIdAndKey(organizationId, request.key())) {
            throw new ConflictException("A project with key '" + request.key() + "' already exists");
        }

        Project project = new Project();
        project.setOrganizationId(organizationId);
        project.setKey(request.key());
        project.setName(request.name());
        project.setOwnerId(user.getId());
        return ProjectMapper.toDto(projectRepository.save(project));
    }

    public ProjectDto getById(Jwt jwt, UUID id) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + id));
        return ProjectMapper.toDto(project);
    }

    public List<ProjectDto> list(Jwt jwt) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        return projectRepository.findByOrganizationId(organizationId).stream()
                .map(ProjectMapper::toDto)
                .toList();
    }
}
