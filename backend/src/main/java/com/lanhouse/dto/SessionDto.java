package com.lanhouse.dto;

public record SessionDto(
        Long id,
        String startTime,
        Integer durationMinutes,
        long secondsRemaining  // -1 = sem limite
) {}
