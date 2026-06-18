package com.lanhouse.service;

import com.lanhouse.dto.MachineDto;
import com.lanhouse.model.Machine;
import com.lanhouse.model.MachineSession;
import com.lanhouse.model.MachineStatus;
import com.lanhouse.repository.MachineRepository;
import com.lanhouse.repository.MachineSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MachineService {

    private final MachineRepository machineRepo;
    private final MachineSessionRepository sessionRepo;
    private final SseService sseService;

    public MachineService(MachineRepository machineRepo, MachineSessionRepository sessionRepo, SseService sseService) {
        this.machineRepo = machineRepo;
        this.sessionRepo = sessionRepo;
        this.sseService = sseService;
    }

    public List<MachineDto> getAllMachines() {
        return machineRepo.findAll().stream().map(MachineDto::from).toList();
    }

    @Transactional
    public MachineDto createMachine(String name) {
        Machine machine = machineRepo.save(new Machine(name));
        MachineDto dto = MachineDto.from(machine);
        sseService.broadcast("update", dto);
        return dto;
    }

    @Transactional
    public MachineDto updateName(Long id, String name) {
        Machine machine = findOrThrow(id);
        machine.setName(name);
        machineRepo.save(machine);
        MachineDto dto = MachineDto.from(machine);
        sseService.broadcast("update", dto);
        return dto;
    }

    @Transactional
    public void deleteMachine(Long id) {
        machineRepo.deleteById(id);
        sseService.broadcast("delete", id);
    }

    @Transactional
    public MachineDto startSession(Long id, Integer durationMinutes) {
        Machine machine = findOrThrow(id);

        if (machine.getStatus() == MachineStatus.BUSY) {
            throw new IllegalStateException("Máquina já está em uso");
        }
        if (machine.getStatus() != MachineStatus.AVAILABLE) {
            throw new IllegalStateException("Máquina não está disponível");
        }

        // Remove sessão anterior se existir
        if (machine.getCurrentSession() != null) {
            sessionRepo.delete(machine.getCurrentSession());
            machine.setCurrentSession(null);
        }

        MachineSession session = new MachineSession(machine, durationMinutes);
        session = sessionRepo.save(session);
        machine.setCurrentSession(session);
        machine.setStatus(MachineStatus.BUSY);
        machineRepo.save(machine);

        MachineDto dto = MachineDto.from(machine);
        sseService.broadcast("update", dto);
        return dto;
    }

    @Transactional
    public MachineDto stopSession(Long id) {
        Machine machine = findOrThrow(id);

        if (machine.getCurrentSession() != null) {
            sessionRepo.delete(machine.getCurrentSession());
            machine.setCurrentSession(null);
        }

        machine.setStatus(MachineStatus.AVAILABLE);
        machineRepo.save(machine);

        MachineDto dto = MachineDto.from(machine);
        sseService.broadcast("update", dto);
        return dto;
    }

    @Transactional
    public MachineDto setStatus(Long id, MachineStatus status) {
        Machine machine = findOrThrow(id);

        if (machine.getStatus() == MachineStatus.BUSY) {
            throw new IllegalStateException("Finalize a sessão antes de alterar o status");
        }

        machine.setStatus(status);
        machineRepo.save(machine);

        MachineDto dto = MachineDto.from(machine);
        sseService.broadcast("update", dto);
        return dto;
    }

    private Machine findOrThrow(Long id) {
        return machineRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Máquina não encontrada: " + id));
    }
}
