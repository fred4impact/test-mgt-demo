package com.testmgmt.platform.testsuite.repository;

import com.testmgmt.platform.testsuite.entity.TestSuiteCase;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestSuiteCaseRepository extends JpaRepository<TestSuiteCase, UUID> {

    List<TestSuiteCase> findBySuiteIdOrderBySortOrderAsc(UUID suiteId);

    Optional<TestSuiteCase> findBySuiteIdAndTestCaseId(UUID suiteId, UUID testCaseId);

    boolean existsBySuiteIdAndTestCaseId(UUID suiteId, UUID testCaseId);

    long countBySuiteId(UUID suiteId);
}
