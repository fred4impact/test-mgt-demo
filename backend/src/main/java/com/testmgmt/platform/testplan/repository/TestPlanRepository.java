package com.testmgmt.platform.testplan.repository;

import com.testmgmt.platform.testplan.entity.TestPlan;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestPlanRepository extends JpaRepository<TestPlan, UUID> {

    List<TestPlan> findByProjectIdOrderByCreatedAtAsc(UUID projectId);

    Optional<TestPlan> findByIdAndProjectId(UUID id, UUID projectId);
}
