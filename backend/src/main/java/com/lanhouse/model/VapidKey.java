package com.lanhouse.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vapid_keys")
public class VapidKey {

    @Id
    private Long id = 1L;

    @Column(name = "public_key", nullable = false, length = 1024)
    private String publicKey;

    @Column(name = "private_key", nullable = false, length = 1024)
    private String privateKey;

    public VapidKey() {}

    public VapidKey(String publicKey, String privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public Long getId() { return id; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
}
