package com.lanhouse.controller;

import com.lanhouse.dto.MachineDto;
import com.lanhouse.dto.StartSessionRequest;
import com.lanhouse.model.MachineStatus;
import com.lanhouse.service.MachineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/machines")
@CrossOrigin(origins = "*")
public class MachineController {

    private final MachineService machineService;

    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    @GetMapping
    public List<MachineDto> getAll() {
        return machineService.getAllMachines();
    }

    @PostMapping
    public ResponseEntity<MachineDto> create(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "").trim();
        if (name.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(machineService.createMachine(name));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MachineDto> updateName(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "").trim();
        if (name.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.ok(machineService.updateName(id, name));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            machineService.deleteMachine(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<MachineDto> startSession(@PathVariable Long id,
                                                    @RequestBody(required = false) StartSessionRequest req) {
        try {
            Integer duration = req != null ? req.durationMinutes() : null;
            return ResponseEntity.ok(machineService.startSession(id, duration));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<MachineDto> stopSession(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(machineService.stopSession(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<MachineDto> setStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            MachineStatus status = MachineStatus.valueOf(body.get("status").toUpperCase());
            return ResponseEntity.ok(machineService.setStatus(id, status));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
