package com.testmgmt.platform.tag.service;

import com.testmgmt.platform.common.error.ConflictException;
import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.project.entity.Project;
import com.testmgmt.platform.project.repository.ProjectRepository;
import com.testmgmt.platform.tag.dto.CreateTagRequest;
import com.testmgmt.platform.tag.dto.TagDto;
import com.testmgmt.platform.tag.entity.Tag;
import com.testmgmt.platform.tag.mapper.TagMapper;
import com.testmgmt.platform.tag.repository.TagRepository;
import com.testmgmt.platform.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class TagService {

    private final ProjectRepository projectRepository;
    private final TagRepository tagRepository;
    private final UserService userService;

    public TagService(ProjectRepository projectRepository, TagRepository tagRepository, UserService userService) {
        this.projectRepository = projectRepository;
        this.tagRepository = tagRepository;
        this.userService = userService;
    }

    public TagDto create(Jwt jwt, UUID projectId, CreateTagRequest request) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        if (tagRepository.existsByProjectIdAndName(project.getId(), request.name())) {
            throw new ConflictException("A tag named '" + request.name() + "' already exists in this project");
        }

        Tag tag = new Tag();
        tag.setProjectId(project.getId());
        tag.setName(request.name());
        return TagMapper.toDto(tagRepository.save(tag));
    }

    public List<TagDto> list(Jwt jwt, UUID projectId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        return tagRepository.findByProjectIdOrderByCreatedAtAsc(project.getId()).stream()
                .map(TagMapper::toDto)
                .toList();
    }
}
