package com.xhs.ai.content.analysis.dto;

import java.time.LocalDate;

public record DashboardRequest(LocalDate startDate, LocalDate endDate) {
}
