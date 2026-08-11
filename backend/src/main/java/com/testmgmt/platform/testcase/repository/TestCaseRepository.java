package com.testmgmt.platform.testcase.repository;

import com.testmgmt.platform.testcase.entity.TestCase;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TestCaseRepository extends JpaRepository<TestCase, UUID>, JpaSpecificationExecutor<TestCase> {

    Optional<TestCase> findByIdAndProjectId(UUID id, UUID projectId);

    long countByProjectId(UUID projectId);
}
