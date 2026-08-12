package com.testmgmt.platform.environment.service;

import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.environment.dto.CreateEnvironmentRequest;
import com.testmgmt.platform.environment.dto.EnvironmentDto;
import com.testmgmt.platform.environment.entity.Environment;
import com.testmgmt.platform.environment.mapper.EnvironmentMapper;
import com.testmgmt.platform.environment.repository.EnvironmentRepository;
import com.testmgmt.platform.project.entity.Project;
import com.testmgmt.platform.project.repository.ProjectRepository;
import com.testmgmt.platform.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class EnvironmentService {

    private final ProjectRepository projectRepository;
    private final EnvironmentRepository environmentRepository;
    private final UserService userService;

    public EnvironmentService(
            ProjectRepository projectRepository,
            EnvironmentRepository environmentRepository,
            UserService userService) {
        this.projectRepository = projectRepository;
        this.environmentRepository = environmentRepository;
        this.userService = userService;
    }

    public EnvironmentDto create(Jwt jwt, UUID projectId, CreateEnvironmentRequest request) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        Environment environment = new Environment();
        environment.setProjectId(project.getId());
        environment.setName(request.name());
        environment.setType(request.type());
        environment.setUrl(request.url());

        return EnvironmentMapper.toDto(environmentRepository.save(environment));
    }

    public EnvironmentDto getById(Jwt jwt, UUID projectId, UUID id) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        Environment environment = environmentRepository
                .findByIdAndProjectId(id, project.getId())
                .orElseThrow(() -> new NotFoundException("Environment not found: " + id));
        return EnvironmentMapper.toDto(environment);
    }

    public List<EnvironmentDto> list(Jwt jwt, UUID projectId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        return environmentRepository.findByProjectIdOrderByCreatedAtAsc(project.getId()).stream()
                .map(EnvironmentMapper::toDto)
                .toList();
    }
}
