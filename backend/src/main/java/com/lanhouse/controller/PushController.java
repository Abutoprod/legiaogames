package com.lanhouse.controller;

import com.lanhouse.dto.PushSubscriptionDto;
import com.lanhouse.service.PushService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/push")
@CrossOrigin(origins = "*")
public class PushController {

    private final PushService pushService;

    public PushController(PushService pushService) {
        this.pushService = pushService;
    }

    @GetMapping("/vapid-public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        String key = pushService.getVapidPublicKey();
        if (key == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(Map.of("publicKey", key));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@RequestBody PushSubscriptionDto dto) {
        if (dto == null || dto.endpoint() == null || dto.keys() == null) {
            return ResponseEntity.badRequest().build();
        }
        pushService.saveSubscription(dto.endpoint(), dto.keys().p256dh(), dto.keys().auth());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/subscribe")
    public ResponseEntity<Void> unsubscribe(@RequestBody Map<String, String> body) {
        String endpoint = body.get("endpoint");
        if (endpoint != null) {
            pushService.removeSubscription(endpoint);
        }
        return ResponseEntity.ok().build();
    }
}
