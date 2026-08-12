package com.testmgmt.platform.testexecution.mapper;

import com.testmgmt.platform.testexecution.dto.TestExecutionDto;
import com.testmgmt.platform.testexecution.entity.TestExecution;

public final class TestExecutionMapper {

    private TestExecutionMapper() {}

    public static TestExecutionDto toDto(TestExecution execution) {
        return new TestExecutionDto(
                execution.getId(),
                execution.getProjectId(),
                execution.getCycleId(),
                execution.getTestCaseId(),
                execution.getAssigneeId(),
                execution.getEnvironmentId(),
                execution.getBuildId(),
                execution.getStatus(),
                execution.getStartedAt(),
                execution.getCompletedAt(),
                execution.getDurationMs(),
                execution.getActualResult(),
                execution.getComment(),
                execution.getCreatedAt(),
                execution.getUpdatedAt());
    }
}
