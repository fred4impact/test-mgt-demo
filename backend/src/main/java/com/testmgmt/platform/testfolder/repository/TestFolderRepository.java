package com.testmgmt.platform.testfolder.repository;

import com.testmgmt.platform.testfolder.entity.TestFolder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestFolderRepository extends JpaRepository<TestFolder, UUID> {

    List<TestFolder> findByProjectIdOrderByCreatedAtAsc(UUID projectId);

    Optional<TestFolder> findByIdAndProjectId(UUID id, UUID projectId);
}
