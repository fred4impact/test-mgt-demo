package com.testmgmt.platform.testcase.mapper;

import com.testmgmt.platform.testcase.dto.TestCaseDto;
import com.testmgmt.platform.testcase.dto.TestCaseVersionDto;
import com.testmgmt.platform.testcase.dto.TestStepDto;
import com.testmgmt.platform.testcase.entity.TestCase;
import com.testmgmt.platform.testcase.entity.TestCaseVersion;
import com.testmgmt.platform.testcase.entity.TestStep;
import java.util.List;

public final class TestCaseMapper {

    private TestCaseMapper() {}

    public static TestCaseVersionDto toDto(TestCaseVersion version) {
        return new TestCaseVersionDto(
                version.getId(),
                version.getVersionNumber(),
                version.getSnapshot(),
                version.getChangeSummary(),
                version.getCreatedAt());
    }

    public static TestStepDto toDto(TestStep step) {
        return new TestStepDto(
                step.getId(), step.getStepNumber(), step.getAction(), step.getTestData(), step.getExpectedResult());
    }

    public static TestCaseDto toDto(TestCase testCase, List<TestStepDto> steps) {
        return new TestCaseDto(
                testCase.getId(),
                testCase.getProjectId(),
                testCase.getFolderId(),
                testCase.getKey(),
                testCase.getTitle(),
                testCase.getPriority(),
                testCase.getSeverity(),
                testCase.getStatus(),
                testCase.getTestType(),
                testCase.getAutomationStatus(),
                testCase.getOwnerId(),
                testCase.getCreatedAt(),
                steps);
    }
}
