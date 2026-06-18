package com.lanhouse.repository;

import com.lanhouse.model.MachineSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MachineSessionRepository extends JpaRepository<MachineSession, Long> {

    Optional<MachineSession> findByMachineId(Long machineId);

    /** Sessões com tempo definido que já expiraram e ainda não foram notificadas */
    @Query("SELECT s FROM MachineSession s WHERE s.durationMinutes IS NOT NULL AND s.notified = false AND s.startTime < :cutoff")
    List<MachineSession> findExpiredUnnotified(LocalDateTime cutoff);
}
