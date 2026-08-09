package com.testmgmt.platform.permission.service;

import com.testmgmt.platform.permission.dto.PermissionDto;
import com.testmgmt.platform.permission.mapper.PermissionMapper;
import com.testmgmt.platform.permission.repository.PermissionRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public List<PermissionDto> list() {
        return permissionRepository.findAll().stream()
                .map(PermissionMapper::toDto)
                .toList();
    }
}
