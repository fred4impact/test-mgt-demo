package com.testmgmt.platform.user.controller;

import com.testmgmt.platform.user.dto.MeDto;
import com.testmgmt.platform.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MeController {

    private final UserService userService;

    public MeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public MeDto me(@AuthenticationPrincipal Jwt jwt) {
        return userService.resolveCurrentUser(jwt);
    }
}
