package com.lanhouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "machines")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MachineStatus status = MachineStatus.AVAILABLE;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(mappedBy = "machine", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private MachineSession currentSession;

    public Machine() {}

    public Machine(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public MachineStatus getStatus() { return status; }
    public void setStatus(MachineStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public MachineSession getCurrentSession() { return currentSession; }
    public void setCurrentSession(MachineSession currentSession) { this.currentSession = currentSession; }
}
