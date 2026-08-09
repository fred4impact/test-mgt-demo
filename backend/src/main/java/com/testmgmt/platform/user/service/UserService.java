package com.testmgmt.platform.user.service;

import com.testmgmt.platform.common.error.ConflictException;
import com.testmgmt.platform.organization.entity.Organization;
import com.testmgmt.platform.organization.repository.OrganizationRepository;
import com.testmgmt.platform.user.dto.MeDto;
import com.testmgmt.platform.user.entity.User;
import com.testmgmt.platform.user.repository.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public UserService(UserRepository userRepository, OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    public MeDto resolveCurrentUser(Jwt jwt) {
        User user = userRepository.findByExternalAuthId(jwt.getSubject()).orElseGet(() -> provision(jwt));

        Organization organization = organizationRepository
                .findById(user.getOrganizationId())
                .orElseThrow(() -> new ConflictException("User's organization no longer exists"));

        return new MeDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                organization.getId(),
                organization.getName());
    }

    private User provision(Jwt jwt) {
        Organization organization = organizationRepository
                .findFirstByOrderByCreatedAtAsc()
                .orElseThrow(() -> new ConflictException("No organization exists yet to assign this user to"));

        User user = new User();
        user.setExternalAuthId(jwt.getSubject());
        user.setEmail(jwt.getClaimAsString("email"));
        user.setFirstName(jwt.getClaimAsString("given_name"));
        user.setLastName(jwt.getClaimAsString("family_name"));
        user.setOrganizationId(organization.getId());
        return userRepository.save(user);
    }
}
