package com.aurum.corebanking.application.service;

import lombok.*;
import java.util.List;

@Getter @Builder
public class FraudResult {
    private final boolean      blocked;
    private final String       blockReason;
    private final List<String> fraudFlags;
    private final boolean      fiauReportRequired;
    private final boolean      amlAlert;
}
