package com.qsdv.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "encryption_keys")
public class EncryptionKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "key_version", unique = true, nullable = false)
    private String keyVersion;
    
    @Column(name = "algorithm", nullable = false)
    private String algorithm;
    
    @Lob
    @Column(name = "public_key", nullable = false)
    private byte[] publicKey;
    
    @Lob
    @Column(name = "private_key", nullable = false)
    private byte[] privateKey;
    
    @Column(name = "key_size")
    private Integer keySize;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "is_active")
    private boolean isActive;
    
    @Column(name = "usage_count")
    private Long usageCount;
    
    @Column(name = "last_used")
    private LocalDateTime lastUsed;
    
    // Constructors
    public EncryptionKey() {
        this.createdAt = LocalDateTime.now();
        this.usageCount = 0L;
        this.isActive = true;
    }
    
    public EncryptionKey(String keyVersion, String algorithm, byte[] publicKey, 
                        byte[] privateKey, Integer keySize) {
        this();
        this.keyVersion = keyVersion;
        this.algorithm = algorithm;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.keySize = keySize;
        // Keys expire after 1 year by default
        this.expiresAt = LocalDateTime.now().plusYears(1);
    }
    
    // Helper methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public void incrementUsage() {
        this.usageCount++;
        this.lastUsed = LocalDateTime.now();
    }
    
    public boolean isValid() {
        return isActive && !isExpired();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getKeyVersion() { return keyVersion; }
    public void setKeyVersion(String keyVersion) { this.keyVersion = keyVersion; }
    
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    
    public byte[] getPublicKey() { return publicKey; }
    public void setPublicKey(byte[] publicKey) { this.publicKey = publicKey; }
    
    public byte[] getPrivateKey() { return privateKey; }
    public void setPrivateKey(byte[] privateKey) { this.privateKey = privateKey; }
    
    public Integer getKeySize() { return keySize; }
    public void setKeySize(Integer keySize) { this.keySize = keySize; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public Long getUsageCount() { return usageCount; }
    public void setUsageCount(Long usageCount) { this.usageCount = usageCount; }
    
    public LocalDateTime getLastUsed() { return lastUsed; }
    public void setLastUsed(LocalDateTime lastUsed) { this.lastUsed = lastUsed; }
}
