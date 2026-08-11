package com.testmgmt.platform.testplan.mapper;

import com.testmgmt.platform.testplan.dto.TestPlanDto;
import com.testmgmt.platform.testplan.entity.TestPlan;

public final class TestPlanMapper {

    private TestPlanMapper() {}

    public static TestPlanDto toDto(TestPlan testPlan) {
        return new TestPlanDto(
                testPlan.getId(),
                testPlan.getProjectId(),
                testPlan.getReleaseId(),
                testPlan.getName(),
                testPlan.getStatus(),
                testPlan.getOwnerId(),
                testPlan.getStartDate(),
                testPlan.getEndDate(),
                testPlan.getCreatedAt());
    }
}
