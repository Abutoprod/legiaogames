package com.lanhouse.dto;

import com.lanhouse.model.Machine;
import com.lanhouse.model.MachineStatus;

public record MachineDto(
        Long id,
        String name,
        MachineStatus status,
        SessionDto currentSession
) {
    public static MachineDto from(Machine m) {
        SessionDto session = null;
        if (m.getCurrentSession() != null) {
            var s = m.getCurrentSession();
            session = new SessionDto(
                    s.getId(),
                    s.getStartTime().toString(),
                    s.getDurationMinutes(),
                    s.getSecondsRemaining()
            );
        }
        return new MachineDto(m.getId(), m.getName(), m.getStatus(), session);
    }
}
