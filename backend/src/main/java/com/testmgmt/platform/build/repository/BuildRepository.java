package com.testmgmt.platform.build.repository;

import com.testmgmt.platform.build.entity.Build;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildRepository extends JpaRepository<Build, UUID> {

    List<Build> findByProjectIdOrderByCreatedAtAsc(UUID projectId);

    Optional<Build> findByIdAndProjectId(UUID id, UUID projectId);
}
