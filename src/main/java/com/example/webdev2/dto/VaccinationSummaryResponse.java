package com.example.webdev2.dto;

import java.time.LocalDate;

public record VaccinationSummaryResponse(String vaccineName, LocalDate vaccinationDate) {}
