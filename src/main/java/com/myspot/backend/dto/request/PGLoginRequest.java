// src/main/java/com/myspot/backend/dto/request/PGLoginRequest.java
package com.myspot.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PG Management Login Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PGLoginRequest {

    @NotBlank(message = "Email address is required")
    @Email(message = "Provide a valid email address")
    private String emailAddress;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private Boolean rememberMe = false;
}
