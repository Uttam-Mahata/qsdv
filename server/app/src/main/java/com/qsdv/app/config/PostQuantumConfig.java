package com.qsdv.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Post-Quantum Cryptography
 */
@Configuration
@ConfigurationProperties(prefix = "qsdv.pq")
public class PostQuantumConfig {
    
    private Kem kem = new Kem();
    private Sig sig = new Sig();
    private String storagePath = "./pq-keys";
    private int keyRotationDays = 30;
    private int securityLevel = 3; // Default to NIST security level 3 (192-bit)
    private boolean enableKeyPersistence = true;
    private boolean enableHybridMode = false; // For hybrid classical/PQ crypto
    
    public static class Kem {
        private String algorithm = "Kyber768"; // Default to Kyber768 (Level 3)
        private boolean cachingEnabled = true;
        private int maxCacheSize = 100;
        
        public String getAlgorithm() {
            return algorithm;
        }
        
        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }
        
        public boolean isCachingEnabled() {
            return cachingEnabled;
        }
        
        public void setCachingEnabled(boolean cachingEnabled) {
            this.cachingEnabled = cachingEnabled;
        }
        
        public int getMaxCacheSize() {
            return maxCacheSize;
        }
        
        public void setMaxCacheSize(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
        }
    }
    
    public static class Sig {
        private String algorithm = "Dilithium3"; // Default to Dilithium3 (Level 3)
        private boolean cachingEnabled = true;
        private int maxCacheSize = 100;
        
        public String getAlgorithm() {
            return algorithm;
        }
        
        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }
        
        public boolean isCachingEnabled() {
            return cachingEnabled;
        }
        
        public void setCachingEnabled(boolean cachingEnabled) {
            this.cachingEnabled = cachingEnabled;
        }
        
        public int getMaxCacheSize() {
            return maxCacheSize;
        }
        
        public void setMaxCacheSize(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
        }
    }
    
    public Kem getKem() {
        return kem;
    }
    
    public void setKem(Kem kem) {
        this.kem = kem;
    }
    
    public Sig getSig() {
        return sig;
    }
    
    public void setSig(Sig sig) {
        this.sig = sig;
    }
    
    public String getStoragePath() {
        return storagePath;
    }
    
    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }
    
    public int getKeyRotationDays() {
        return keyRotationDays;
    }
    
    public void setKeyRotationDays(int keyRotationDays) {
        this.keyRotationDays = keyRotationDays;
    }
    
    public int getSecurityLevel() {
        return securityLevel;
    }
    
    public void setSecurityLevel(int securityLevel) {
        this.securityLevel = securityLevel;
    }
    
    public boolean isEnableKeyPersistence() {
        return enableKeyPersistence;
    }
    
    public void setEnableKeyPersistence(boolean enableKeyPersistence) {
        this.enableKeyPersistence = enableKeyPersistence;
    }
    
    public boolean isEnableHybridMode() {
        return enableHybridMode;
    }
    
    public void setEnableHybridMode(boolean enableHybridMode) {
        this.enableHybridMode = enableHybridMode;
    }
}