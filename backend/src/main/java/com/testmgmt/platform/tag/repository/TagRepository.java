package com.testmgmt.platform.tag.repository;

import com.testmgmt.platform.tag.entity.Tag;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    List<Tag> findByProjectIdOrderByCreatedAtAsc(UUID projectId);

    Optional<Tag> findByIdAndProjectId(UUID id, UUID projectId);

    boolean existsByProjectIdAndName(UUID projectId, String name);
}
