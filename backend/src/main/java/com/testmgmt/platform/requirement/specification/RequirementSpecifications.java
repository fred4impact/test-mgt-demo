package com.testmgmt.platform.requirement.specification;

import com.testmgmt.platform.requirement.entity.Requirement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class RequirementSpecifications {

    private RequirementSpecifications() {}

    public static Specification<Requirement> search(UUID projectId, String q, String status, String priority) {
        List<Specification<Requirement>> predicates = new ArrayList<>();
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

        return Specification.allOf(predicates);
    }
}
