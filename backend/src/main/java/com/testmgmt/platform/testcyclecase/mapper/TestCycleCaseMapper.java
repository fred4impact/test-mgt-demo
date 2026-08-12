package com.testmgmt.platform.testcyclecase.mapper;

import com.testmgmt.platform.testcase.entity.TestCase;
import com.testmgmt.platform.testcyclecase.dto.TestCycleCaseDto;
import com.testmgmt.platform.testcyclecase.entity.TestCycleCase;

public final class TestCycleCaseMapper {

    private TestCycleCaseMapper() {}

    public static TestCycleCaseDto toDto(TestCycleCase cycleCase, TestCase testCase) {
        return new TestCycleCaseDto(
                testCase.getId(),
                testCase.getKey(),
                testCase.getTitle(),
                cycleCase.getAssigneeId(),
                cycleCase.getSortOrder(),
                cycleCase.getCreatedAt());
    }
}
