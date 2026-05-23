package com.aurum.core_banking.interfaces.rest.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdateRequest {
    
    @NotBlank
    @Size(max = 100)
    private String firstName;
    
    @NotBlank
    @Size(max = 100)
    private String lastName;
    
    @NotBlank
    @Email
    @Size(max = 255)
    private String email;
    
    @Size(max = 20)
    private String phone;
    
    @Size(max = 500)
    private String address;
    
    private LocalDate dateOfBirth;
}
