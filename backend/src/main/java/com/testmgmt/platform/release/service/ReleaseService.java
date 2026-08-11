package com.testmgmt.platform.release.service;

import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.project.entity.Project;
import com.testmgmt.platform.project.repository.ProjectRepository;
import com.testmgmt.platform.release.dto.CreateReleaseRequest;
import com.testmgmt.platform.release.dto.ReleaseDto;
import com.testmgmt.platform.release.entity.Release;
import com.testmgmt.platform.release.mapper.ReleaseMapper;
import com.testmgmt.platform.release.repository.ReleaseRepository;
import com.testmgmt.platform.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class ReleaseService {

    private final ProjectRepository projectRepository;
    private final ReleaseRepository releaseRepository;
    private final UserService userService;

    public ReleaseService(
            ProjectRepository projectRepository, ReleaseRepository releaseRepository, UserService userService) {
        this.projectRepository = projectRepository;
        this.releaseRepository = releaseRepository;
        this.userService = userService;
    }

    public ReleaseDto create(Jwt jwt, UUID projectId, CreateReleaseRequest request) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        Release release = new Release();
        release.setProjectId(project.getId());
        release.setName(request.name());
        release.setVersion(request.version());
        if (request.status() != null) {
            release.setStatus(request.status());
        }
        release.setStartDate(request.startDate());
        release.setReleaseDate(request.releaseDate());

        return ReleaseMapper.toDto(releaseRepository.save(release));
    }

    public ReleaseDto getById(Jwt jwt, UUID projectId, UUID id) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        Release release = releaseRepository
                .findByIdAndProjectId(id, project.getId())
                .orElseThrow(() -> new NotFoundException("Release not found: " + id));
        return ReleaseMapper.toDto(release);
    }

    public List<ReleaseDto> list(Jwt jwt, UUID projectId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        return releaseRepository.findByProjectIdOrderByCreatedAtAsc(project.getId()).stream()
                .map(ReleaseMapper::toDto)
                .toList();
    }
}
