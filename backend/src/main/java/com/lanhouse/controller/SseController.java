package com.lanhouse.controller;

import com.lanhouse.service.SseService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class SseController {

    private final SseService sseService;

    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping(produces = "text/event-stream")
    public SseEmitter subscribe() {
        return sseService.addEmitter();
    }
}
