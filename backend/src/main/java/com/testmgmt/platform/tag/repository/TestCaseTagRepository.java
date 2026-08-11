package com.testmgmt.platform.tag.repository;

import com.testmgmt.platform.tag.entity.TestCaseTag;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestCaseTagRepository extends JpaRepository<TestCaseTag, UUID> {

    List<TestCaseTag> findByTestCaseId(UUID testCaseId);

    Optional<TestCaseTag> findByTestCaseIdAndTagId(UUID testCaseId, UUID tagId);

    boolean existsByTestCaseIdAndTagId(UUID testCaseId, UUID tagId);
}
