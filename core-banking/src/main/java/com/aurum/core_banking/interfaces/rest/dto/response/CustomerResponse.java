package com.aurum.core_banking.interfaces.rest.dto.response;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private UUID id;
    private String keycloakId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private String kycStatus;
    private Instant createdAt;
    private Instant updatedAt;
}
