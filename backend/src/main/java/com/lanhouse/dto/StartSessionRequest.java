package com.lanhouse.dto;

public record StartSessionRequest(
        Integer durationMinutes  // null = sem limite
) {}
