package com.testmgmt.platform.testcase.repository;

import com.testmgmt.platform.testcase.entity.TestStep;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestStepRepository extends JpaRepository<TestStep, UUID> {

    List<TestStep> findByTestCaseIdOrderByStepNumberAsc(UUID testCaseId);

    Optional<TestStep> findByIdAndTestCaseId(UUID id, UUID testCaseId);

    void deleteByTestCaseId(UUID testCaseId);
}
