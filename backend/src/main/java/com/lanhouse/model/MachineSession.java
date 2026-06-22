package com.lanhouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "machine_sessions")
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
    private Integer durationMinutes;

    @Column(name = "notified")
    private boolean notified = false;

    public MachineSession() {}

    public MachineSession(Machine machine, Integer durationMinutes) {
        this.machine = machine;
        this.durationMinutes = durationMinutes;
    }

    public boolean isExpired() {
        if (durationMinutes == null) return false;
        return LocalDateTime.now().isAfter(startTime.plusMinutes(durationMinutes));
    }

    public long getSecondsRemaining() {
        if (durationMinutes == null) return -1L;
        long endEpoch = startTime.plusMinutes(durationMinutes).toEpochSecond(ZoneOffset.UTC);
        long nowEpoch = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        return Math.max(0L, endEpoch - nowEpoch);
    }

    public long getSecondsElapsed() {
        long startEpoch = startTime.toEpochSecond(ZoneOffset.UTC);
        long nowEpoch = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        return Math.max(0L, nowEpoch - startEpoch);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Machine getMachine() { return machine; }
    public void setMachine(Machine machine) { this.machine = machine; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public boolean isNotified() { return notified; }
    public void setNotified(boolean notified) { this.notified = notified; }
}
