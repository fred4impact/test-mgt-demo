package com.testmgmt.platform.testsuite.service;

import com.testmgmt.platform.common.error.ConflictException;
import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.project.entity.Project;
import com.testmgmt.platform.project.repository.ProjectRepository;
import com.testmgmt.platform.testcase.entity.TestCase;
import com.testmgmt.platform.testcase.repository.TestCaseRepository;
import com.testmgmt.platform.testsuite.dto.AddTestSuiteCaseRequest;
import com.testmgmt.platform.testsuite.dto.TestSuiteCaseDto;
import com.testmgmt.platform.testsuite.entity.TestSuite;
import com.testmgmt.platform.testsuite.entity.TestSuiteCase;
import com.testmgmt.platform.testsuite.mapper.TestSuiteMapper;
import com.testmgmt.platform.testsuite.repository.TestSuiteCaseRepository;
import com.testmgmt.platform.testsuite.repository.TestSuiteRepository;
import com.testmgmt.platform.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class TestSuiteCaseService {

    private final ProjectRepository projectRepository;
    private final TestSuiteRepository testSuiteRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestSuiteCaseRepository testSuiteCaseRepository;
    private final UserService userService;

    public TestSuiteCaseService(
            ProjectRepository projectRepository,
            TestSuiteRepository testSuiteRepository,
            TestCaseRepository testCaseRepository,
            TestSuiteCaseRepository testSuiteCaseRepository,
            UserService userService) {
        this.projectRepository = projectRepository;
        this.testSuiteRepository = testSuiteRepository;
        this.testCaseRepository = testCaseRepository;
        this.testSuiteCaseRepository = testSuiteCaseRepository;
        this.userService = userService;
    }

    public TestSuiteCaseDto addCase(Jwt jwt, UUID projectId, UUID suiteId, AddTestSuiteCaseRequest request) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        TestSuite suite = testSuiteRepository
                .findByIdAndProjectId(suiteId, project.getId())
                .orElseThrow(() -> new NotFoundException("Test suite not found: " + suiteId));

        TestCase testCase = testCaseRepository
                .findByIdAndProjectId(request.testCaseId(), project.getId())
                .orElseThrow(() -> new NotFoundException("Test case not found: " + request.testCaseId()));

        if (testSuiteCaseRepository.existsBySuiteIdAndTestCaseId(suite.getId(), testCase.getId())) {
            throw new ConflictException("Test case is already in this suite");
        }

        long nextSortOrder = testSuiteCaseRepository.countBySuiteId(suite.getId()) + 1;

        TestSuiteCase suiteCase = new TestSuiteCase();
        suiteCase.setSuiteId(suite.getId());
        suiteCase.setTestCaseId(testCase.getId());
        suiteCase.setSortOrder((int) nextSortOrder);
        TestSuiteCase saved = testSuiteCaseRepository.save(suiteCase);

        return TestSuiteMapper.toDto(saved, testCase);
    }

    public void removeCase(Jwt jwt, UUID projectId, UUID suiteId, UUID testCaseId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        TestSuite suite = testSuiteRepository
                .findByIdAndProjectId(suiteId, project.getId())
                .orElseThrow(() -> new NotFoundException("Test suite not found: " + suiteId));

        TestSuiteCase suiteCase = testSuiteCaseRepository
                .findBySuiteIdAndTestCaseId(suite.getId(), testCaseId)
                .orElseThrow(() -> new NotFoundException("Test case is not in this suite: " + testCaseId));

        testSuiteCaseRepository.delete(suiteCase);
    }

    public List<TestSuiteCaseDto> listCases(Jwt jwt, UUID projectId, UUID suiteId) {
        UUID organizationId = userService.resolveOrProvisionUser(jwt).getOrganizationId();
        Project project = projectRepository
                .findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        TestSuite suite = testSuiteRepository
                .findByIdAndProjectId(suiteId, project.getId())
                .orElseThrow(() -> new NotFoundException("Test suite not found: " + suiteId));

        return testSuiteCaseRepository.findBySuiteIdOrderBySortOrderAsc(suite.getId()).stream()
                .map(suiteCase -> TestSuiteMapper.toDto(
                        suiteCase,
                        testCaseRepository
                                .findById(suiteCase.getTestCaseId())
                                .orElseThrow(() -> new NotFoundException(
                                        "Test case not found: " + suiteCase.getTestCaseId()))))
                .toList();
    }
}
