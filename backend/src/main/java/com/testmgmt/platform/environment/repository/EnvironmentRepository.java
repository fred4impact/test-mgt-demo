package com.testmgmt.platform.environment.repository;

import com.testmgmt.platform.environment.entity.Environment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvironmentRepository extends JpaRepository<Environment, UUID> {

    List<Environment> findByProjectIdOrderByCreatedAtAsc(UUID projectId);

    Optional<Environment> findByIdAndProjectId(UUID id, UUID projectId);
}
