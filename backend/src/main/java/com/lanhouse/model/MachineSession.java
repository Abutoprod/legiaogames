package com.lanhouse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "machine_sessions")
@Getter
@Setter
@NoArgsConstructor
public class MachineSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "machine_id", unique = true, nullable = false)
    private Machine machine;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime = LocalDateTime.now();

    @Column(name = "duration_minutes")
    private Integer durationMinutes; // null = sem limite (manual stop)

    @Column(name = "notified")
    private boolean notified = false;

    public MachineSession(Machine machine, Integer durationMinutes) {
        this.machine = machine;
        this.durationMinutes = durationMinutes;
    }

    /** Retorna true se a sessão tem tempo definido e o tempo acabou */
    public boolean isExpired() {
        if (durationMinutes == null) return false;
        return LocalDateTime.now().isAfter(startTime.plusMinutes(durationMinutes));
    }

    /** Segundos restantes. -1 = sem limite. 0 = expirado */
    public long getSecondsRemaining() {
        if (durationMinutes == null) return -1L;
        long endEpoch = startTime.plusMinutes(durationMinutes).toEpochSecond(ZoneOffset.UTC);
        long nowEpoch = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        return Math.max(0L, endEpoch - nowEpoch);
    }
}
