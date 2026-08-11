package com.testmgmt.platform.testsuite.repository;

import com.testmgmt.platform.testsuite.entity.TestSuite;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestSuiteRepository extends JpaRepository<TestSuite, UUID> {

    List<TestSuite> findByProjectIdOrderByCreatedAtAsc(UUID projectId);

    Optional<TestSuite> findByIdAndProjectId(UUID id, UUID projectId);
}
