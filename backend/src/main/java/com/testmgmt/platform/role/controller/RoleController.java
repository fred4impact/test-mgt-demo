package com.testmgmt.platform.role.controller;

import com.testmgmt.platform.permission.dto.PermissionDto;
import com.testmgmt.platform.role.dto.AddRolePermissionRequest;
import com.testmgmt.platform.role.dto.CreateRoleRequest;
import com.testmgmt.platform.role.dto.RoleDto;
import com.testmgmt.platform.role.service.RolePermissionService;
import com.testmgmt.platform.role.service.RoleService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;
    private final RolePermissionService rolePermissionService;

    public RoleController(RoleService roleService, RolePermissionService rolePermissionService) {
        this.roleService = roleService;
        this.rolePermissionService = rolePermissionService;
    }

    @PostMapping
    public ResponseEntity<RoleDto> create(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateRoleRequest request) {
        RoleDto dto = roleService.create(jwt, request);
        return ResponseEntity.created(URI.create("/api/v1/roles/" + dto.id())).body(dto);
    }

    @GetMapping("/{id}")
    public RoleDto getById(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return roleService.getById(jwt, id);
    }

    @GetMapping
    public List<RoleDto> list(@AuthenticationPrincipal Jwt jwt) {
        return roleService.list(jwt);
    }

    @PostMapping("/{roleId}/permissions")
    public ResponseEntity<PermissionDto> addPermission(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID roleId,
            @Valid @RequestBody AddRolePermissionRequest request) {
        PermissionDto dto = rolePermissionService.addPermission(jwt, roleId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<Void> removePermission(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID roleId, @PathVariable UUID permissionId) {
        rolePermissionService.removePermission(jwt, roleId, permissionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{roleId}/permissions")
    public List<PermissionDto> listPermissions(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID roleId) {
        return rolePermissionService.listPermissions(jwt, roleId);
    }
}
