package com.testmgmt.platform.testcyclecase.repository;

import com.testmgmt.platform.testcyclecase.entity.TestCycleCase;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestCycleCaseRepository extends JpaRepository<TestCycleCase, UUID> {

    List<TestCycleCase> findByCycleIdOrderBySortOrderAsc(UUID cycleId);

    Optional<TestCycleCase> findByCycleIdAndTestCaseId(UUID cycleId, UUID testCaseId);

    boolean existsByCycleIdAndTestCaseId(UUID cycleId, UUID testCaseId);

    long countByCycleId(UUID cycleId);
}
