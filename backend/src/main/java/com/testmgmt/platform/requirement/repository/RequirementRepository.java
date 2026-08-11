package com.testmgmt.platform.requirement.repository;

import com.testmgmt.platform.requirement.entity.Requirement;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RequirementRepository
        extends JpaRepository<Requirement, UUID>, JpaSpecificationExecutor<Requirement> {

    Optional<Requirement> findByIdAndProjectId(UUID id, UUID projectId);

    long countByProjectId(UUID projectId);
}
