package com.testmgmt.platform.permission.controller;

import com.testmgmt.platform.permission.dto.PermissionDto;
import com.testmgmt.platform.permission.service.PermissionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public List<PermissionDto> list() {
        return permissionService.list();
    }
}
