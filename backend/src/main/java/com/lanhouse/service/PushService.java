package com.lanhouse.service;

import com.lanhouse.model.PushSubscription;
import com.lanhouse.model.VapidKey;
import com.lanhouse.repository.PushSubscriptionRepository;
import com.lanhouse.repository.VapidKeyRepository;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Utils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.security.KeyPair;
import java.security.Security;
import java.util.Base64;
import java.util.List;

@Service
public class PushService {

    private static final Logger log = LoggerFactory.getLogger(PushService.class);

    private final PushSubscriptionRepository subscriptionRepo;
    private final VapidKeyRepository vapidKeyRepo;

    private String vapidPublicKey;
    private String vapidPrivateKey;

    public PushService(PushSubscriptionRepository subscriptionRepo, VapidKeyRepository vapidKeyRepo) {
        this.subscriptionRepo = subscriptionRepo;
        this.vapidKeyRepo = vapidKeyRepo;
    }

    @PostConstruct
    public void init() {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        VapidKey stored = vapidKeyRepo.findById(1L).orElse(null);
        if (stored == null) {
            try {
                KeyPair keyPair = Utils.generateVapidKeyPair();
                String pub = Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(keyPair.getPublic().getEncoded());
                String priv = Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(keyPair.getPrivate().getEncoded());
                stored = vapidKeyRepo.save(new VapidKey(pub, priv));
                log.info("VAPID keys generated. Public key: {}", pub);
            } catch (Exception e) {
                log.error("Failed to generate VAPID keys", e);
                return;
            }
        }

        this.vapidPublicKey = stored.getPublicKey();
        this.vapidPrivateKey = stored.getPrivateKey();
    }

    public String getVapidPublicKey() {
        return vapidPublicKey;
    }

    @Transactional
    public void saveSubscription(String endpoint, String p256dh, String auth) {
        subscriptionRepo.findByEndpoint(endpoint).ifPresentOrElse(
                existing -> {
                    existing.setP256dh(p256dh);
                    existing.setAuth(auth);
                    subscriptionRepo.save(existing);
                },
                () -> subscriptionRepo.save(new PushSubscription(endpoint, p256dh, auth))
        );
    }

    @Transactional
    public void removeSubscription(String endpoint) {
        subscriptionRepo.deleteByEndpoint(endpoint);
    }

    public void sendNotificationToAll(String title, String body) {
        if (vapidPublicKey == null || vapidPrivateKey == null) {
            log.warn("VAPID keys not available, skipping push notification");
            return;
        }

        List<PushSubscription> subscriptions = subscriptionRepo.findAll();
        if (subscriptions.isEmpty()) return;

        String payload = String.format("{\"title\":\"%s\",\"body\":\"%s\"}", title, body);

        try {
            PushService pushService = new PushService(vapidPublicKey, vapidPrivateKey, "mailto:admin@lanhouse.local");

            for (PushSubscription sub : subscriptions) {
                try {
                    Notification notification = new Notification(sub.getEndpoint(), sub.getP256dh(), sub.getAuth(), payload);
                    pushService.send(notification);
                } catch (Exception e) {
                    log.warn("Failed to send push to {}: {}", sub.getEndpoint(), e.getMessage());
                    // Remove subscription inválida
                    subscriptionRepo.delete(sub);
                }
            }
        } catch (Exception e) {
            log.error("Error creating push service", e);
        }
    }
}
