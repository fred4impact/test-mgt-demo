package com.testmgmt.platform.testexecution.repository;

import com.testmgmt.platform.testexecution.entity.TestExecution;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestExecutionRepository extends JpaRepository<TestExecution, UUID> {

    List<TestExecution> findByCycleId(UUID cycleId);

    Optional<TestExecution> findByCycleIdAndTestCaseId(UUID cycleId, UUID testCaseId);
}
