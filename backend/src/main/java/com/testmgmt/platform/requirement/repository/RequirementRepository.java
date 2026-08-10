package com.testmgmt.platform.requirement.repository;

import com.testmgmt.platform.requirement.entity.Requirement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementRepository extends JpaRepository<Requirement, UUID> {

    List<Requirement> findByProjectIdOrderByCreatedAtAsc(UUID projectId);

    Optional<Requirement> findByIdAndProjectId(UUID id, UUID projectId);

    long countByProjectId(UUID projectId);
}
