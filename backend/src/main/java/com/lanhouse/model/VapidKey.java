package com.lanhouse.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vapid_keys")
@Getter
@Setter
@NoArgsConstructor
public class VapidKey {

    @Id
    private Long id = 1L; // sempre um único registro

    @Column(name = "public_key", nullable = false, length = 1024)
    private String publicKey;

    @Column(name = "private_key", nullable = false, length = 1024)
    private String privateKey;

    public VapidKey(String publicKey, String privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }
}
