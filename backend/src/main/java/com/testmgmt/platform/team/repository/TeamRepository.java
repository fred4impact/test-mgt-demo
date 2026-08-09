package com.testmgmt.platform.team.repository;

import com.testmgmt.platform.team.entity.Team;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, UUID> {

    List<Team> findByOrganizationId(UUID organizationId);

    Optional<Team> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
