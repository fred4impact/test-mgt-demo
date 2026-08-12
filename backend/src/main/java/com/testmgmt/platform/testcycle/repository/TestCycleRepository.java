package com.testmgmt.platform.testcycle.repository;

import com.testmgmt.platform.testcycle.entity.TestCycle;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestCycleRepository extends JpaRepository<TestCycle, UUID> {

    List<TestCycle> findByProjectIdOrderByCreatedAtAsc(UUID projectId);

    Optional<TestCycle> findByIdAndProjectId(UUID id, UUID projectId);
}
