package com.myspot.backend.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myspot.backend.security.CustomUserPrincipal;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
@SecurityRequirement(name = "bearerAuth") 
@RestController
public class WelcomeController {

    /**
     * A protected route that returns a welcome message.
     * JWT must be provided in the Authorization header.
     */
    @GetMapping("/welcome")
    public String welcome(@AuthenticationPrincipal CustomUserPrincipal user) {
        return "Welcome, " + user.getEmail() + " (PG ID: " + user.getId() + ")!";
    }
}
