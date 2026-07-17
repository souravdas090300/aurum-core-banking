package com.aurum.corebanking.interfaces.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Loan officer review decision request.
 */
public record LoanReviewRequest(
    boolean approved,

    @NotBlank
    @Size(max = 500)
    String notes
) {}
