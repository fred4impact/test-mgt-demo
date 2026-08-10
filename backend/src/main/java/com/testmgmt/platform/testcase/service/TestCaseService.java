package com.testmgmt.platform.testcase.service;

import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.project.entity.Project;
import com.testmgmt.platform.project.repository.ProjectRepository;
import com.testmgmt.platform.testcase.dto.CreateTestCaseRequest;
import com.testmgmt.platform.testcase.dto.CreateTestStepRequest;
import com.testmgmt.platform.testcase.dto.TestCaseDto;
import com.testmgmt.platform.testcase.dto.TestStepDto;
import com.testmgmt.platform.testcase.entity.TestCase;
import com.testmgmt.platform.testcase.entity.TestStep;
import com.testmgmt.platform.testcase.mapper.TestCaseMapper;
import com.testmgmt.platform.testcase.repository.TestCaseRepository;
import com.testmgmt.platform.testcase.repository.TestStepRepository;
import com.testmgmt.platform.testfolder.repository.TestFolderRepository;
import com.testmgmt.platform.user.entity.User;
import com.testmgmt.platform.user.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestCaseService {

    private final ProjectRepository projectRepository;
    private final TestFolderRepository testFolderRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestStepRepository testStepRepository;
    private final UserService userService;

    public TestCaseService(
            ProjectRepository projectRepository,
            TestFolderRepository testFolderRepository,
            TestCaseRepository testCaseRepository,
            TestStepRepository testStepRepository,
            UserService userService) {
        this.projectRepository = projectRepository;
        this.testFolderRepository = testFolderRepository;
        this.testCaseRepository = testCaseRepository;
        this.testStepRepository = testStepRepository;
        this.userService = userService;
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
        TestCase saved = testCaseRepository.save(testCase);

        List<CreateTestStepRequest> stepRequests = request.steps() != null ? request.steps() : List.of();
        List<TestStepDto> stepDtos = new ArrayList<>();
        for (int i = 0; i < stepRequests.size(); i++) {
            CreateTestStepRequest stepRequest = stepRequests.get(i);
            TestStep step = new TestStep();
            step.setTestCaseId(saved.getId());
            step.setStepNumber(i + 1);
            step.setAction(stepRequest.action());
            step.setTestData(stepRequest.testData());
            step.setExpectedResult(stepRequest.expectedResult());
            stepDtos.add(TestCaseMapper.toDto(testStepRepository.save(step)));
        }

        return TestCaseMapper.toDto(saved, stepDtos);
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

    public List<TestCaseDto> list(Jwt jwt, UUID projectId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        return testCaseRepository.findByProjectIdOrderByCreatedAtAsc(project.getId()).stream()
                .map(testCase -> TestCaseMapper.toDto(testCase, loadSteps(testCase.getId())))
                .toList();
    }

    private List<TestStepDto> loadSteps(UUID testCaseId) {
        return testStepRepository.findByTestCaseIdOrderByStepNumberAsc(testCaseId).stream()
                .map(TestCaseMapper::toDto)
                .toList();
    }
}
