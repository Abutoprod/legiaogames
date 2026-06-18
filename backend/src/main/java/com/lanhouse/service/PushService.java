package com.lanhouse.service;

import com.lanhouse.model.PushSubscription;
import com.lanhouse.model.VapidKey;
import com.lanhouse.repository.PushSubscriptionRepository;
import com.lanhouse.repository.VapidKeyRepository;
import jakarta.annotation.PostConstruct;
import nl.martijndwars.webpush.Notification;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.*;
import java.util.Arrays;
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
                ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("prime256v1");
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
                keyGen.initialize(spec, new SecureRandom());
                KeyPair keyPair = keyGen.generateKeyPair();

                // Public key: uncompressed EC point (65 bytes, starts with 0x04)
                org.bouncycastle.jce.interfaces.ECPublicKey bcPub =
                        (org.bouncycastle.jce.interfaces.ECPublicKey) keyPair.getPublic();
                byte[] pubBytes = bcPub.getQ().getEncoded(false);
                String pub = Base64.getUrlEncoder().withoutPadding().encodeToString(pubBytes);

                // Private key: raw 32-byte scalar (strip sign byte if present)
                org.bouncycastle.jce.interfaces.ECPrivateKey bcPriv =
                        (org.bouncycastle.jce.interfaces.ECPrivateKey) keyPair.getPrivate();
                byte[] privRaw = bcPriv.getD().toByteArray();
                // BigInteger.toByteArray() may add a leading 0x00 sign byte
                if (privRaw.length == 33 && privRaw[0] == 0) {
                    privRaw = java.util.Arrays.copyOfRange(privRaw, 1, 33);
                }
                String priv = Base64.getUrlEncoder().withoutPadding().encodeToString(privRaw);

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
            log.warn("VAPID keys not ready, skipping push");
            return;
        }

        List<PushSubscription> subscriptions = subscriptionRepo.findAll();
        if (subscriptions.isEmpty()) return;

        String payload = "{\"title\":\"" + title + "\",\"body\":\"" + body + "\"}";

        try {
            // Usa nome completo para evitar conflito com o nome desta classe
            nl.martijndwars.webpush.PushService pushSvc =
                    new nl.martijndwars.webpush.PushService(vapidPublicKey, vapidPrivateKey, "mailto:admin@lanhouse.local");

            for (PushSubscription sub : subscriptions) {
                try {
                    Notification notification = new Notification(sub.getEndpoint(), sub.getP256dh(), sub.getAuth(), payload);
                    pushSvc.send(notification);
                } catch (Exception e) {
                    log.warn("Push failed for {}: {}", sub.getEndpoint(), e.getMessage());
                    subscriptionRepo.delete(sub);
                }
            }
        } catch (Exception e) {
            log.error("Error creating push service", e);
        }
    }
}
