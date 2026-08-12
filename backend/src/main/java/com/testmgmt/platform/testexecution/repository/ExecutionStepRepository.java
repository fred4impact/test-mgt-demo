package com.testmgmt.platform.testexecution.repository;

import com.testmgmt.platform.testexecution.entity.ExecutionStep;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionStepRepository extends JpaRepository<ExecutionStep, UUID> {

    List<ExecutionStep> findByExecutionIdOrderByStepNumberAsc(UUID executionId);

    Optional<ExecutionStep> findByExecutionIdAndTestStepId(UUID executionId, UUID testStepId);
}
