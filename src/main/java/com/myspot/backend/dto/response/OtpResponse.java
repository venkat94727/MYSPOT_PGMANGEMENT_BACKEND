package com.myspot.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpResponse {

    private Boolean success;
    private String message;
    private String emailAddress;
    private LocalDateTime expiryTime;
    private Integer attemptsRemaining;
}