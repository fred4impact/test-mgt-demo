package com.testmgmt.platform.role.repository;

import com.testmgmt.platform.role.entity.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    List<Role> findByOrganizationId(UUID organizationId);

    Optional<Role> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
