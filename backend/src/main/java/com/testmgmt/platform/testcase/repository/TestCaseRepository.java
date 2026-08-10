package com.testmgmt.platform.testcase.repository;

import com.testmgmt.platform.testcase.entity.TestCase;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestCaseRepository extends JpaRepository<TestCase, UUID> {

    List<TestCase> findByProjectIdOrderByCreatedAtAsc(UUID projectId);

    Optional<TestCase> findByIdAndProjectId(UUID id, UUID projectId);

    long countByProjectId(UUID projectId);
}
