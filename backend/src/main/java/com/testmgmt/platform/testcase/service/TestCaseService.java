package com.testmgmt.platform.testcase.service;

import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.project.entity.Project;
import com.testmgmt.platform.project.repository.ProjectRepository;
import com.testmgmt.platform.testcase.dto.CreateTestCaseRequest;
import com.testmgmt.platform.testcase.dto.CreateTestStepRequest;
import com.testmgmt.platform.testcase.dto.TestCaseDto;
import com.testmgmt.platform.testcase.dto.TestCaseVersionDto;
import com.testmgmt.platform.testcase.dto.TestStepDto;
import com.testmgmt.platform.testcase.dto.UpdateTestCaseRequest;
import com.testmgmt.platform.testcase.entity.TestCase;
import com.testmgmt.platform.testcase.entity.TestCaseVersion;
import com.testmgmt.platform.testcase.entity.TestStep;
import com.testmgmt.platform.testcase.mapper.TestCaseMapper;
import com.testmgmt.platform.testcase.repository.TestCaseRepository;
import com.testmgmt.platform.testcase.repository.TestCaseVersionRepository;
import com.testmgmt.platform.testcase.repository.TestStepRepository;
import com.testmgmt.platform.testcase.specification.TestCaseSpecifications;
import com.testmgmt.platform.testfolder.repository.TestFolderRepository;
import com.testmgmt.platform.release.repository.ReleaseRepository;
import com.testmgmt.platform.user.entity.User;
import com.testmgmt.platform.user.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class TestCaseService {

    private final ProjectRepository projectRepository;
    private final TestFolderRepository testFolderRepository;
    private final ReleaseRepository releaseRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestStepRepository testStepRepository;
    private final TestCaseVersionRepository testCaseVersionRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public TestCaseService(
            ProjectRepository projectRepository,
            TestFolderRepository testFolderRepository,
            ReleaseRepository releaseRepository,
            TestCaseRepository testCaseRepository,
            TestStepRepository testStepRepository,
            TestCaseVersionRepository testCaseVersionRepository,
            UserService userService,
            ObjectMapper objectMapper) {
        this.projectRepository = projectRepository;
        this.testFolderRepository = testFolderRepository;
        this.releaseRepository = releaseRepository;
        this.testCaseRepository = testCaseRepository;
        this.testStepRepository = testStepRepository;
        this.testCaseVersionRepository = testCaseVersionRepository;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public TestCaseDto create(Jwt jwt, UUID projectId, CreateTestCaseRequest request) {
        User user = userService.resolveOrProvisionUser(jwt);
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, user.getOrganizationId())
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        testFolderRepository
                .findByIdAndProjectId(request.folderId(), project.getId())
                .orElseThrow(() -> new NotFoundException("Test folder not found: " + request.folderId()));

        if (request.releaseId() != null) {
            releaseRepository
                    .findByIdAndProjectId(request.releaseId(), project.getId())
                    .orElseThrow(() -> new NotFoundException("Release not found: " + request.releaseId()));
        }

        long nextNumber = testCaseRepository.countByProjectId(project.getId()) + 1;

        TestCase testCase = new TestCase();
        testCase.setProjectId(project.getId());
        testCase.setFolderId(request.folderId());
        testCase.setKey(project.getKey() + "-" + nextNumber);
        testCase.setTitle(request.title());
        testCase.setPriority(request.priority());
        testCase.setSeverity(request.severity());
        testCase.setTestType(request.testType());
        testCase.setAutomationStatus(request.automationStatus());
        testCase.setOwnerId(user.getId());
        testCase.setReleaseId(request.releaseId());
        TestCase saved = testCaseRepository.save(testCase);

        List<TestStepDto> stepDtos = createSteps(saved.getId(), request.steps());

        return TestCaseMapper.toDto(saved, stepDtos);
    }

    @Transactional
    public TestCaseDto update(Jwt jwt, UUID projectId, UUID id, UpdateTestCaseRequest request) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        TestCase testCase = testCaseRepository
                .findByIdAndProjectId(id, project.getId())
                .orElseThrow(() -> new NotFoundException("Test case not found: " + id));

        if (request.folderId() != null) {
            testFolderRepository
                    .findByIdAndProjectId(request.folderId(), project.getId())
                    .orElseThrow(() -> new NotFoundException("Test folder not found: " + request.folderId()));
        }
        if (request.releaseId() != null) {
            releaseRepository
                    .findByIdAndProjectId(request.releaseId(), project.getId())
                    .orElseThrow(() -> new NotFoundException("Release not found: " + request.releaseId()));
        }

        TestCaseDto preEditState = TestCaseMapper.toDto(testCase, loadSteps(testCase.getId()));
        long nextVersionNumber = testCaseVersionRepository.countByTestCaseId(testCase.getId()) + 1;

        TestCaseVersion version = new TestCaseVersion();
        version.setTestCaseId(testCase.getId());
        version.setVersionNumber((int) nextVersionNumber);
        version.setSnapshot(objectMapper.writeValueAsString(preEditState));
        version.setChangeSummary(request.changeSummary());
        testCaseVersionRepository.save(version);

        if (request.folderId() != null) {
            testCase.setFolderId(request.folderId());
        }
        if (request.title() != null) {
            testCase.setTitle(request.title());
        }
        if (request.priority() != null) {
            testCase.setPriority(request.priority());
        }
        if (request.severity() != null) {
            testCase.setSeverity(request.severity());
        }
        if (request.status() != null) {
            testCase.setStatus(request.status());
        }
        if (request.testType() != null) {
            testCase.setTestType(request.testType());
        }
        if (request.automationStatus() != null) {
            testCase.setAutomationStatus(request.automationStatus());
        }
        if (request.releaseId() != null) {
            testCase.setReleaseId(request.releaseId());
        }
        TestCase saved = testCaseRepository.save(testCase);

        List<TestStepDto> stepDtos;
        if (request.steps() != null) {
            testStepRepository.deleteByTestCaseId(saved.getId());
            testStepRepository.flush();
            stepDtos = createSteps(saved.getId(), request.steps());
        } else {
            stepDtos = loadSteps(saved.getId());
        }

        return TestCaseMapper.toDto(saved, stepDtos);
    }

    public List<TestCaseVersionDto> listVersions(Jwt jwt, UUID projectId, UUID id) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        testCaseRepository
                .findByIdAndProjectId(id, project.getId())
                .orElseThrow(() -> new NotFoundException("Test case not found: " + id));

        return testCaseVersionRepository.findByTestCaseIdOrderByVersionNumberAsc(id).stream()
                .map(TestCaseMapper::toDto)
                .toList();
    }

    public TestCaseDto getById(Jwt jwt, UUID projectId, UUID id) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        TestCase testCase = testCaseRepository
                .findByIdAndProjectId(id, project.getId())
                .orElseThrow(() -> new NotFoundException("Test case not found: " + id));

        return TestCaseMapper.toDto(testCase, loadSteps(testCase.getId()));
    }

    public List<TestCaseDto> list(
            Jwt jwt,
            UUID projectId,
            String q,
            String status,
            String priority,
            String severity,
            String testType,
            String automationStatus,
            UUID folderId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        return testCaseRepository
                .findAll(
                        TestCaseSpecifications.search(
                                project.getId(), q, status, priority, severity, testType, automationStatus, folderId),
                        Sort.by("createdAt").ascending())
                .stream()
                .map(testCase -> TestCaseMapper.toDto(testCase, loadSteps(testCase.getId())))
                .toList();
    }

    private List<TestStepDto> loadSteps(UUID testCaseId) {
        return testStepRepository.findByTestCaseIdOrderByStepNumberAsc(testCaseId).stream()
                .map(TestCaseMapper::toDto)
                .toList();
    }

    private List<TestStepDto> createSteps(UUID testCaseId, List<CreateTestStepRequest> requestedSteps) {
        List<CreateTestStepRequest> stepRequests = requestedSteps != null ? requestedSteps : List.of();
        List<TestStepDto> stepDtos = new ArrayList<>();
        for (int i = 0; i < stepRequests.size(); i++) {
            CreateTestStepRequest stepRequest = stepRequests.get(i);
            TestStep step = new TestStep();
            step.setTestCaseId(testCaseId);
            step.setStepNumber(i + 1);
            step.setAction(stepRequest.action());
            step.setTestData(stepRequest.testData());
            step.setExpectedResult(stepRequest.expectedResult());
            stepDtos.add(TestCaseMapper.toDto(testStepRepository.save(step)));
        }
        return stepDtos;
    }
}
