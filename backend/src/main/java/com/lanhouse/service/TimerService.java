package com.lanhouse.service;

import com.lanhouse.dto.MachineDto;
import com.lanhouse.model.Machine;
import com.lanhouse.model.MachineSession;
import com.lanhouse.model.MachineStatus;
import com.lanhouse.repository.MachineRepository;
import com.lanhouse.repository.MachineSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TimerService {

    private static final Logger log = LoggerFactory.getLogger(TimerService.class);

    private final MachineSessionRepository sessionRepo;
    private final MachineRepository machineRepo;
    private final PushService pushService;
    private final SseService sseService;

    public TimerService(MachineSessionRepository sessionRepo, MachineRepository machineRepo,
                        PushService pushService, SseService sseService) {
        this.sessionRepo = sessionRepo;
        this.machineRepo = machineRepo;
        this.pushService = pushService;
        this.sseService = sseService;
    }

    /** Roda a cada 30 segundos: verifica sessões expiradas e envia notificações */
    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void checkExpiredSessions() {
        // Busca sessões com tempo definido onde startTime + duration já passou
        LocalDateTime now = LocalDateTime.now();

        List<MachineSession> sessions = sessionRepo.findAll().stream()
                .filter(s -> s.getDurationMinutes() != null && !s.isNotified() && s.isExpired())
                .toList();

        for (MachineSession session : sessions) {
            Machine machine = session.getMachine();
            log.info("Sessão expirada: máquina {} ({})", machine.getName(), machine.getId());

            // Marca como notificada
            session.setNotified(true);
            sessionRepo.save(session);

            // Envia push notification
            pushService.sendNotificationToAll(
                    "Tempo esgotado!",
                    "A máquina " + machine.getName() + " terminou o tempo."
            );

            // Emite evento SSE para atualizar a interface
            sseService.broadcast("expired", MachineDto.from(machine));
        }
    }

    /** Roda a cada 10 segundos: faz broadcast do estado atual para manter contagens atualizadas */
    @Scheduled(fixedDelay = 10_000)
    @Transactional(readOnly = true)
    public void broadcastTick() {
        List<Machine> busy = machineRepo.findAll().stream()
                .filter(m -> m.getStatus() == MachineStatus.BUSY)
                .toList();

        for (Machine m : busy) {
            if (m.getCurrentSession() != null && m.getCurrentSession().getDurationMinutes() != null) {
                sseService.broadcast("tick", MachineDto.from(m));
            }
        }
    }
}
