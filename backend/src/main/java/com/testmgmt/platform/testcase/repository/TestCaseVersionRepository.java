package com.testmgmt.platform.testcase.repository;

import com.testmgmt.platform.testcase.entity.TestCaseVersion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestCaseVersionRepository extends JpaRepository<TestCaseVersion, UUID> {

    List<TestCaseVersion> findByTestCaseIdOrderByVersionNumberAsc(UUID testCaseId);

    long countByTestCaseId(UUID testCaseId);
}
