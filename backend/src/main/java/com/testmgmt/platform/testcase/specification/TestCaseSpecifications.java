package com.testmgmt.platform.testcase.specification;

import com.testmgmt.platform.testcase.entity.TestCase;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class TestCaseSpecifications {

    private TestCaseSpecifications() {}

    public static Specification<TestCase> search(
            UUID projectId,
            String q,
            String status,
            String priority,
            String severity,
            String testType,
            String automationStatus,
            UUID folderId) {
        List<Specification<TestCase>> predicates = new ArrayList<>();
        predicates.add((root, query, cb) -> cb.equal(root.get("projectId"), projectId));

        if (q != null && !q.isBlank()) {
            String pattern = "%" + q.toLowerCase() + "%";
            predicates.add((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), pattern), cb.like(cb.lower(root.get("key")), pattern)));
        }
        if (status != null && !status.isBlank()) {
            predicates.add((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (priority != null && !priority.isBlank()) {
            predicates.add((root, query, cb) -> cb.equal(root.get("priority"), priority));
        }
        if (severity != null && !severity.isBlank()) {
            predicates.add((root, query, cb) -> cb.equal(root.get("severity"), severity));
        }
        if (testType != null && !testType.isBlank()) {
            predicates.add((root, query, cb) -> cb.equal(root.get("testType"), testType));
        }
        if (automationStatus != null && !automationStatus.isBlank()) {
            predicates.add((root, query, cb) -> cb.equal(root.get("automationStatus"), automationStatus));
        }
        if (folderId != null) {
            predicates.add((root, query, cb) -> cb.equal(root.get("folderId"), folderId));
        }

        return Specification.allOf(predicates);
    }
}
