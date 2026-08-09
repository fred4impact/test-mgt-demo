package com.testmgmt.platform.organization.mapper;

import com.testmgmt.platform.organization.dto.OrganizationDto;
import com.testmgmt.platform.organization.entity.Organization;

public final class OrganizationMapper {

    private OrganizationMapper() {}

    public static OrganizationDto toDto(Organization organization) {
        return new OrganizationDto(
                organization.getId(),
                organization.getName(),
                organization.getSlug(),
                organization.getStatus(),
                organization.getCreatedAt());
    }
}
