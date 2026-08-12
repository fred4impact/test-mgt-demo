package com.testmgmt.platform.testcycle.mapper;

import com.testmgmt.platform.testcycle.dto.TestCycleDto;
import com.testmgmt.platform.testcycle.entity.TestCycle;

public final class TestCycleMapper {

    private TestCycleMapper() {}

    public static TestCycleDto toDto(TestCycle testCycle) {
        return new TestCycleDto(
                testCycle.getId(),
                testCycle.getProjectId(),
                testCycle.getTestPlanId(),
                testCycle.getReleaseId(),
                testCycle.getBuildId(),
                testCycle.getEnvironmentId(),
                testCycle.getName(),
                testCycle.getStatus(),
                testCycle.getOwnerId(),
                testCycle.getStartDate(),
                testCycle.getEndDate(),
                testCycle.getCreatedAt());
    }
}
