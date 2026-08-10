package com.testmgmt.platform.project.service;

import com.testmgmt.platform.common.error.ConflictException;
import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.project.dto.AddProjectMemberRequest;
import com.testmgmt.platform.project.dto.ProjectMemberDto;
import com.testmgmt.platform.project.entity.Project;
import com.testmgmt.platform.project.entity.ProjectMember;
import com.testmgmt.platform.project.repository.ProjectMemberRepository;
import com.testmgmt.platform.project.repository.ProjectRepository;
import com.testmgmt.platform.role.entity.Role;
import com.testmgmt.platform.role.repository.RoleRepository;
import com.testmgmt.platform.user.entity.User;
import com.testmgmt.platform.user.repository.UserRepository;
import com.testmgmt.platform.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserService userService;

    public ProjectMemberService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserService userService) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userService = userService;
    }

    public ProjectMemberDto addMember(Jwt jwt, UUID projectId, AddProjectMemberRequest request) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        User targetUser = userRepository
                .findById(request.userId())
                .filter(u -> u.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new NotFoundException("User not found: " + request.userId()));

        Role role = roleRepository
                .findByIdAndOrganizationId(request.roleId(), organizationId)
                .orElseThrow(() -> new NotFoundException("Role not found: " + request.roleId()));

        if (projectMemberRepository.existsByProjectIdAndUserId(project.getId(), targetUser.getId())) {
            throw new ConflictException("User is already a member of this project");
        }

        ProjectMember member = new ProjectMember();
        member.setProjectId(project.getId());
        member.setUserId(targetUser.getId());
        member.setRoleId(role.getId());
        ProjectMember saved = projectMemberRepository.save(member);

        return toDto(saved, targetUser, role);
    }

    public void removeMember(Jwt jwt, UUID projectId, UUID userId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        ProjectMember member = projectMemberRepository
                .findByProjectIdAndUserId(project.getId(), userId)
                .orElseThrow(() -> new NotFoundException("User is not a member of this project: " + userId));

        projectMemberRepository.delete(member);
    }

    public List<ProjectMemberDto> listMembers(Jwt jwt, UUID projectId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        return projectMemberRepository.findByProjectId(project.getId()).stream()
                .map(member -> toDto(
                        member,
                        userRepository
                                .findById(member.getUserId())
                                .orElseThrow(() -> new NotFoundException(
                                        "User not found: " + member.getUserId())),
                        roleRepository
                                .findById(member.getRoleId())
                                .orElseThrow(() -> new NotFoundException(
                                        "Role not found: " + member.getRoleId()))))
                .toList();
    }

    private ProjectMemberDto toDto(ProjectMember member, User user, Role role) {
        return new ProjectMemberDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                role.getId(),
                role.getName(),
                member.getCreatedAt());
    }
}
