package com.testmgmt.platform.permission.repository;

import com.testmgmt.platform.permission.entity.Permission;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {}
