package com.testmgmt.platform.organization.controller;

import com.testmgmt.platform.organization.dto.CreateOrganizationRequest;
import com.testmgmt.platform.organization.dto.OrganizationDto;
import com.testmgmt.platform.organization.service.OrganizationService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    public ResponseEntity<OrganizationDto> create(@Valid @RequestBody CreateOrganizationRequest request) {
        OrganizationDto dto = organizationService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/organizations/" + dto.id())).body(dto);
    }

    @GetMapping("/{id}")
    public OrganizationDto getById(@PathVariable UUID id) {
        return organizationService.getById(id);
    }

    @GetMapping
    public List<OrganizationDto> list() {
        return organizationService.list();
    }
}
