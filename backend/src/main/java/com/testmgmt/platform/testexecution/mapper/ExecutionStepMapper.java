package com.testmgmt.platform.testexecution.mapper;

import com.testmgmt.platform.testexecution.dto.ExecutionStepDto;
import com.testmgmt.platform.testexecution.entity.ExecutionStep;

public final class ExecutionStepMapper {

    private ExecutionStepMapper() {}

    public static ExecutionStepDto toDto(ExecutionStep step) {
        return new ExecutionStepDto(
                step.getId(),
                step.getExecutionId(),
                step.getTestStepId(),
                step.getStepNumber(),
                step.getStatus(),
                step.getActualResult(),
                step.getComment(),
                step.getCreatedAt(),
                step.getUpdatedAt());
    }
}
