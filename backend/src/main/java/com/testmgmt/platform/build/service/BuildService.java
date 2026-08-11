package com.testmgmt.platform.build.service;

import com.testmgmt.platform.build.dto.BuildDto;
import com.testmgmt.platform.build.dto.CreateBuildRequest;
import com.testmgmt.platform.build.entity.Build;
import com.testmgmt.platform.build.mapper.BuildMapper;
import com.testmgmt.platform.build.repository.BuildRepository;
import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.project.entity.Project;
import com.testmgmt.platform.project.repository.ProjectRepository;
import com.testmgmt.platform.release.repository.ReleaseRepository;
import com.testmgmt.platform.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class BuildService {

    private final ProjectRepository projectRepository;
    private final BuildRepository buildRepository;
    private final ReleaseRepository releaseRepository;
    private final UserService userService;

    public BuildService(
            ProjectRepository projectRepository,
            BuildRepository buildRepository,
            ReleaseRepository releaseRepository,
            UserService userService) {
        this.projectRepository = projectRepository;
        this.buildRepository = buildRepository;
        this.releaseRepository = releaseRepository;
        this.userService = userService;
    }

    public BuildDto create(Jwt jwt, UUID projectId, CreateBuildRequest request) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        releaseRepository
                .findByIdAndProjectId(request.releaseId(), project.getId())
                .orElseThrow(() -> new NotFoundException("Release not found: " + request.releaseId()));

        Build build = new Build();
        build.setProjectId(project.getId());
        build.setReleaseId(request.releaseId());
        build.setName(request.name());
        build.setVersion(request.version());
        build.setBranch(request.branch());
        build.setCommitSha(request.commitSha());
        if (request.status() != null) {
            build.setStatus(request.status());
        }

        return BuildMapper.toDto(buildRepository.save(build));
    }

    public BuildDto getById(Jwt jwt, UUID projectId, UUID id) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        Build build = buildRepository
                .findByIdAndProjectId(id, project.getId())
                .orElseThrow(() -> new NotFoundException("Build not found: " + id));
        return BuildMapper.toDto(build);
    }

    public List<BuildDto> list(Jwt jwt, UUID projectId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        return buildRepository.findByProjectIdOrderByCreatedAtAsc(project.getId()).stream()
                .map(BuildMapper::toDto)
                .toList();
    }
}
