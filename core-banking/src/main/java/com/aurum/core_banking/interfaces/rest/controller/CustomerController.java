package com.aurum.core_banking.interfaces.rest.controller;

import com.aurum.core_banking.infrastructure.persistence.entity.CustomerEntity;
import com.aurum.core_banking.infrastructure.persistence.repository.CustomerRepository;
import com.aurum.core_banking.interfaces.rest.dto.request.CustomerUpdateRequest;
import com.aurum.core_banking.interfaces.rest.dto.response.CustomerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for customer management operations.
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class CustomerController {

    private final CustomerRepository customerRepository;

    /**
     * Get all customers.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('LOAN_OFFICER','COMPLIANCE_OFFICER')")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<CustomerResponse> customers = customerRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customers);
    }

    /**
     * Get customer profile (current user).
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('BANKING_USER')")
    public ResponseEntity<CustomerResponse> getProfile() {
        // In a real implementation, get customer ID from security context
        // For now, return first customer as demo
        CustomerEntity customer = customerRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> createDemoCustomer());
        return ResponseEntity.ok(toResponse(customer));
    }

    /**
     * Get customer by ID.
     */
    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('BANKING_USER','LOAN_OFFICER','COMPLIANCE_OFFICER')")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable @NonNull UUID customerId) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return ResponseEntity.ok(toResponse(customer));
    }

    /**
     * Update customer profile.
     */
    @PutMapping("/{customerId}")
    @PreAuthorize("hasRole('BANKING_USER')")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable @NonNull UUID customerId,
            @Valid @RequestBody CustomerUpdateRequest request) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setDateOfBirth(request.getDateOfBirth());

        CustomerEntity updated = customerRepository.save(customer);
        return ResponseEntity.ok(toResponse(updated));
    }

    private CustomerEntity createDemoCustomer() {
        CustomerEntity customer = CustomerEntity.builder()
                .firstName("Demo")
                .lastName("User")
                .email("demo@aurum.bank")
                .phone("+356 1234 5678")
                .address("123 Banking Street, Valletta, Malta")
                .kycStatus(CustomerEntity.KycStatus.VERIFIED)
                .build();
        return customerRepository.save(customer);
    }

    private CustomerResponse toResponse(CustomerEntity entity) {
        return CustomerResponse.builder()
                .id(entity.getId())
                .keycloakId(entity.getKeycloakId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .dateOfBirth(entity.getDateOfBirth())
                .kycStatus(entity.getKycStatus().name())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
