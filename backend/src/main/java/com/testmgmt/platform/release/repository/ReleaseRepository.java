package com.testmgmt.platform.release.repository;

import com.testmgmt.platform.release.entity.Release;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReleaseRepository extends JpaRepository<Release, UUID> {

    List<Release> findByProjectIdOrderByCreatedAtAsc(UUID projectId);

    Optional<Release> findByIdAndProjectId(UUID id, UUID projectId);
}
