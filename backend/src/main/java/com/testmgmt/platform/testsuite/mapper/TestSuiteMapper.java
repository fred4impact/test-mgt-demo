package com.testmgmt.platform.testsuite.mapper;

import com.testmgmt.platform.testcase.entity.TestCase;
import com.testmgmt.platform.testsuite.dto.TestSuiteCaseDto;
import com.testmgmt.platform.testsuite.dto.TestSuiteDto;
import com.testmgmt.platform.testsuite.entity.TestSuite;
import com.testmgmt.platform.testsuite.entity.TestSuiteCase;

public final class TestSuiteMapper {

    private TestSuiteMapper() {}

    public static TestSuiteDto toDto(TestSuite suite) {
        return new TestSuiteDto(
                suite.getId(), suite.getProjectId(), suite.getParentId(), suite.getName(), suite.getCreatedAt());
    }

    public static TestSuiteCaseDto toDto(TestSuiteCase suiteCase, TestCase testCase) {
        return new TestSuiteCaseDto(
                testCase.getId(), testCase.getKey(), testCase.getTitle(), suiteCase.getSortOrder(), suiteCase.getCreatedAt());
    }
}
