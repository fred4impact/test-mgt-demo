package com.testmgmt.platform.project.repository;

import com.testmgmt.platform.project.entity.Project;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByOrganizationId(UUID organizationId);

    Optional<Project> findByIdAndOrganizationId(UUID id, UUID organizationId);

    boolean existsByOrganizationIdAndKey(UUID organizationId, String key);
}
