package com.testmgmt.platform.requirement.service;

import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.project.entity.Project;
import com.testmgmt.platform.project.repository.ProjectRepository;
import com.testmgmt.platform.requirement.dto.CreateRequirementRequest;
import com.testmgmt.platform.requirement.dto.RequirementDto;
import com.testmgmt.platform.requirement.entity.Requirement;
import com.testmgmt.platform.requirement.mapper.RequirementMapper;
import com.testmgmt.platform.requirement.repository.RequirementRepository;
import com.testmgmt.platform.requirement.specification.RequirementSpecifications;
import com.testmgmt.platform.user.entity.User;
import com.testmgmt.platform.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class RequirementService {

    private final ProjectRepository projectRepository;
    private final RequirementRepository requirementRepository;
    private final UserService userService;

    public RequirementService(
            ProjectRepository projectRepository,
            RequirementRepository requirementRepository,
            UserService userService) {
        this.projectRepository = projectRepository;
        this.requirementRepository = requirementRepository;
        this.userService = userService;
    }

    public RequirementDto create(Jwt jwt, UUID projectId, CreateRequirementRequest request) {
        User user = userService.resolveOrProvisionUser(jwt);
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, user.getOrganizationId())
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        long nextNumber = requirementRepository.countByProjectId(project.getId()) + 1;

        Requirement requirement = new Requirement();
        requirement.setProjectId(project.getId());
        requirement.setKey(project.getKey() + "-" + nextNumber);
        requirement.setTitle(request.title());
        if (request.status() != null) {
            requirement.setStatus(request.status());
        }
        requirement.setPriority(request.priority());
        requirement.setOwnerId(user.getId());

        return RequirementMapper.toDto(requirementRepository.save(requirement));
    }

    public RequirementDto getById(Jwt jwt, UUID projectId, UUID id) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        Requirement requirement = requirementRepository
                .findByIdAndProjectId(id, project.getId())
                .orElseThrow(() -> new NotFoundException("Requirement not found: " + id));
        return RequirementMapper.toDto(requirement);
    }

    public List<RequirementDto> list(Jwt jwt, UUID projectId, String q, String status, String priority) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        return requirementRepository
                .findAll(
                        RequirementSpecifications.search(project.getId(), q, status, priority),
                        Sort.by("createdAt").ascending())
                .stream()
                .map(RequirementMapper::toDto)
                .toList();
    }
}
