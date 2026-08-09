package com.testmgmt.platform.organization.service;

import com.testmgmt.platform.common.error.ConflictException;
import com.testmgmt.platform.common.error.NotFoundException;
import com.testmgmt.platform.organization.dto.CreateOrganizationRequest;
import com.testmgmt.platform.organization.dto.OrganizationDto;
import com.testmgmt.platform.organization.entity.Organization;
import com.testmgmt.platform.organization.mapper.OrganizationMapper;
import com.testmgmt.platform.organization.repository.OrganizationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public OrganizationDto create(CreateOrganizationRequest request) {
        if (organizationRepository.existsBySlug(request.slug())) {
            throw new ConflictException("An organization with slug '" + request.slug() + "' already exists");
        }
        Organization organization = new Organization();
        organization.setName(request.name());
        organization.setSlug(request.slug());
        organization.setDescription(request.description());
        return OrganizationMapper.toDto(organizationRepository.save(organization));
    }

    public OrganizationDto getById(UUID id) {
        Organization organization = organizationRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Organization not found: " + id));
        return OrganizationMapper.toDto(organization);
    }

    public List<OrganizationDto> list() {
        return organizationRepository.findAll().stream().map(OrganizationMapper::toDto).toList();
    }
}
