package com.testmgmt.platform.user.repository;

import com.testmgmt.platform.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByExternalAuthId(String externalAuthId);
}
