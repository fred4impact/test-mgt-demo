package com.testmgmt.platform.tag.service;

import com.testmgmt.platform.common.error.ConflictException;
import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.project.entity.Project;
import com.testmgmt.platform.project.repository.ProjectRepository;
import com.testmgmt.platform.tag.dto.AddTestCaseTagRequest;
import com.testmgmt.platform.tag.dto.TagDto;
import com.testmgmt.platform.tag.entity.Tag;
import com.testmgmt.platform.tag.entity.TestCaseTag;
import com.testmgmt.platform.tag.mapper.TagMapper;
import com.testmgmt.platform.tag.repository.TagRepository;
import com.testmgmt.platform.tag.repository.TestCaseTagRepository;
import com.testmgmt.platform.testcase.entity.TestCase;
import com.testmgmt.platform.testcase.repository.TestCaseRepository;
import com.testmgmt.platform.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class TestCaseTagService {

    private final ProjectRepository projectRepository;
    private final TestCaseRepository testCaseRepository;
    private final TagRepository tagRepository;
    private final TestCaseTagRepository testCaseTagRepository;
    private final UserService userService;

    public TestCaseTagService(
            ProjectRepository projectRepository,
            TestCaseRepository testCaseRepository,
            TagRepository tagRepository,
            TestCaseTagRepository testCaseTagRepository,
            UserService userService) {
        this.projectRepository = projectRepository;
        this.testCaseRepository = testCaseRepository;
        this.tagRepository = tagRepository;
        this.testCaseTagRepository = testCaseTagRepository;
        this.userService = userService;
    }

    public TagDto addTag(Jwt jwt, UUID projectId, UUID testCaseId, AddTestCaseTagRequest request) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        TestCase testCase = testCaseRepository
                .findByIdAndProjectId(testCaseId, project.getId())
                .orElseThrow(() -> new NotFoundException("Test case not found: " + testCaseId));

        Tag tag = tagRepository
                .findByIdAndProjectId(request.tagId(), project.getId())
                .orElseThrow(() -> new NotFoundException("Tag not found: " + request.tagId()));

        if (testCaseTagRepository.existsByTestCaseIdAndTagId(testCase.getId(), tag.getId())) {
            throw new ConflictException("Tag is already attached to this test case");
        }

        TestCaseTag testCaseTag = new TestCaseTag();
        testCaseTag.setTestCaseId(testCase.getId());
        testCaseTag.setTagId(tag.getId());
        testCaseTagRepository.save(testCaseTag);

        return TagMapper.toDto(tag);
    }

    public void removeTag(Jwt jwt, UUID projectId, UUID testCaseId, UUID tagId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        TestCase testCase = testCaseRepository
                .findByIdAndProjectId(testCaseId, project.getId())
                .orElseThrow(() -> new NotFoundException("Test case not found: " + testCaseId));

        TestCaseTag testCaseTag = testCaseTagRepository
                .findByTestCaseIdAndTagId(testCase.getId(), tagId)
                .orElseThrow(() -> new NotFoundException("Tag is not attached to this test case: " + tagId));

        testCaseTagRepository.delete(testCaseTag);
    }

    public List<TagDto> listTags(Jwt jwt, UUID projectId, UUID testCaseId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        TestCase testCase = testCaseRepository
                .findByIdAndProjectId(testCaseId, project.getId())
                .orElseThrow(() -> new NotFoundException("Test case not found: " + testCaseId));

        return testCaseTagRepository.findByTestCaseId(testCase.getId()).stream()
                .map(testCaseTag -> tagRepository
                        .findById(testCaseTag.getTagId())
                        .orElseThrow(() -> new NotFoundException("Tag not found: " + testCaseTag.getTagId())))
                .map(TagMapper::toDto)
                .toList();
    }
}
