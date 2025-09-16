package com.qsdv.app.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.RSAKeyGenParameterSpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Post-Quantum Cryptography Service
 * 
 * This service simulates post-quantum cryptographic operations using RSA-OAEP
 * as a placeholder. In production, this would use actual PQ algorithms like
 * Kyber for KEM and AES-GCM for symmetric encryption.
 * 
 * The structure is designed to be easily replaceable with real PQ implementations
 * when they become widely available in Java libraries.
 */
@Service
public class PostQuantumCryptoService {

    private static final Logger logger = LoggerFactory.getLogger(PostQuantumCryptoService.class);

    @Value("${qsdv.pq.algorithm:Kyber512}")
    private String algorithm;

    @Value("${qsdv.pq.storage-path:./pq-keys}")
    private String keyStoragePath;

    @Value("${qsdv.pq.key-rotation-days:30}")
    private int keyRotationDays;

    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentHashMap<String, KeyPairInfo> keyCache = new ConcurrentHashMap<>();

    private static class KeyPairInfo {
        public final PublicKey publicKey;
        public final PrivateKey privateKey;
        public final LocalDateTime createdAt;
        public final String version;

        public KeyPairInfo(PublicKey publicKey, PrivateKey privateKey, String version) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
            this.createdAt = LocalDateTime.now();
            this.version = version;
        }
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing Post-Quantum Cryptography Service (RSA-OAEP simulation)");
        
        // Add BouncyCastle provider
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
            logger.info("BouncyCastle provider added");
        }

        // Create key storage directory
        try {
            Path keyDir = Paths.get(keyStoragePath);
            if (!Files.exists(keyDir)) {
                Files.createDirectories(keyDir);
                logger.info("Created key storage directory: {}", keyStoragePath);
            }
        } catch (Exception e) {
            logger.error("Failed to create key storage directory", e);
        }

        // Generate initial key pair
        generateCurrentKeyPair();
        
        logger.info("Post-Quantum Cryptography Service initialized with algorithm: {} (simulated)", algorithm);
    }

    /**
     * Get the current public key for encryption
     */
    public byte[] getCurrentPublicKey() {
        KeyPairInfo currentKey = getCurrentKeyPair();
        return currentKey.publicKey.getEncoded();
    }

    /**
     * Get the current public key version
     */
    public String getCurrentKeyVersion() {
        KeyPairInfo currentKey = getCurrentKeyPair();
        return currentKey.version;
    }

    /**
     * Encapsulate a secret using the current public key (simulated KEM)
     */
    public PQEncapsulationResult encapsulateSecret() {
        try {
            KeyPairInfo currentKey = getCurrentKeyPair();
            
            // Generate a random shared secret (32 bytes for AES-256)
            byte[] sharedSecret = new byte[32];
            secureRandom.nextBytes(sharedSecret);
            
            // Encrypt the shared secret with RSA-OAEP (simulating KEM encapsulation)
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, currentKey.publicKey);
            byte[] encapsulatedKey = cipher.doFinal(sharedSecret);
            
            logger.debug("Successfully encapsulated secret with key version: {}", currentKey.version);
            
            return new PQEncapsulationResult(
                encapsulatedKey,
                sharedSecret,
                currentKey.version
            );
            
        } catch (Exception e) {
            logger.error("Failed to encapsulate secret", e);
            throw new RuntimeException("PQ encapsulation failed", e);
        }
    }

    /**
     * Decapsulate an encapsulated key to recover the shared secret (simulated KEM)
     */
    public byte[] decapsulateSecret(byte[] encapsulatedKey, String keyVersion) {
        try {
            KeyPairInfo keyPair = getKeyPairByVersion(keyVersion);
            if (keyPair == null) {
                throw new IllegalArgumentException("Key version not found: " + keyVersion);
            }
            
            // Decrypt the shared secret with RSA-OAEP (simulating KEM decapsulation)
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, keyPair.privateKey);
            byte[] sharedSecret = cipher.doFinal(encapsulatedKey);
            
            logger.debug("Successfully decapsulated secret with key version: {}", keyVersion);
            
            return sharedSecret;
            
        } catch (Exception e) {
            logger.error("Failed to decapsulate secret for version: {}", keyVersion, e);
            throw new RuntimeException("PQ decapsulation failed", e);
        }
    }

    /**
     * Encrypt data using AES-GCM with a PQ-derived key
     */
    public PQEncryptionResult encryptData(byte[] data) {
        try {
            // Encapsulate a shared secret using PQ crypto (simulated)
            PQEncapsulationResult encapsulation = encapsulateSecret();
            
            // Use the shared secret as AES key
            SecretKeySpec keySpec = new SecretKeySpec(encapsulation.getSharedSecret(), "AES");
            
            // Encrypt data with AES-GCM
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedData = cipher.doFinal(data);
            byte[] iv = cipher.getIV();
            
            logger.debug("Successfully encrypted data using PQ-derived AES key");
            
            return new PQEncryptionResult(
                encryptedData,
                encapsulation.getEncapsulatedKey(),
                iv,
                encapsulation.getKeyVersion()
            );
            
        } catch (Exception e) {
            logger.error("Failed to encrypt data", e);
            throw new RuntimeException("PQ encryption failed", e);
        }
    }

    /**
     * Decrypt data using AES-GCM with a PQ-derived key
     */
    public byte[] decryptData(PQEncryptionResult encryptionResult) {
        try {
            // Decapsulate the shared secret
            byte[] sharedSecret = decapsulateSecret(
                encryptionResult.getEncapsulatedKey(),
                encryptionResult.getKeyVersion()
            );
            
            // Use the shared secret as AES key
            SecretKeySpec keySpec = new SecretKeySpec(sharedSecret, "AES");
            
            // Decrypt data
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, encryptionResult.getIv());
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            byte[] decryptedData = cipher.doFinal(encryptionResult.getEncryptedData());
            
            logger.debug("Successfully decrypted data using PQ-derived AES key");
            
            return decryptedData;
            
        } catch (Exception e) {
            logger.error("Failed to decrypt data", e);
            throw new RuntimeException("PQ decryption failed", e);
        }
    }

    private KeyPairInfo getCurrentKeyPair() {
        String currentVersion = "v1"; // For now, we'll use a simple versioning scheme
        return keyCache.computeIfAbsent(currentVersion, k -> generateCurrentKeyPair());
    }

    private KeyPairInfo getKeyPairByVersion(String version) {
        return keyCache.get(version);
    }

    private KeyPairInfo generateCurrentKeyPair() {
        try {
            // Generate RSA key pair (simulating PQ key generation)
            // In production, this would generate Kyber or other PQ key pairs
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
            RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4);
            keyGen.initialize(spec, secureRandom);
            
            KeyPair keyPair = keyGen.generateKeyPair();
            
            String version = "v1_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            
            KeyPairInfo keyPairInfo = new KeyPairInfo(keyPair.getPublic(), keyPair.getPrivate(), version);
            keyCache.put("v1", keyPairInfo); // Current version
            keyCache.put(version, keyPairInfo); // Versioned access
            
            logger.info("Generated new RSA key pair (simulating PQ) with version: {}", version);
            
            return keyPairInfo;
            
        } catch (Exception e) {
            logger.error("Failed to generate key pair", e);
            throw new RuntimeException("Key generation failed", e);
        }
    }

    // Result classes
    public static class PQEncapsulationResult {
        private final byte[] encapsulatedKey;
        private final byte[] sharedSecret;
        private final String keyVersion;

        public PQEncapsulationResult(byte[] encapsulatedKey, byte[] sharedSecret, String keyVersion) {
            this.encapsulatedKey = encapsulatedKey;
            this.sharedSecret = sharedSecret;
            this.keyVersion = keyVersion;
        }

        public byte[] getEncapsulatedKey() { return encapsulatedKey; }
        public byte[] getSharedSecret() { return sharedSecret; }
        public String getKeyVersion() { return keyVersion; }
    }

    public static class PQEncryptionResult {
        private final byte[] encryptedData;
        private final byte[] encapsulatedKey;
        private final byte[] iv;
        private final String keyVersion;

        public PQEncryptionResult(byte[] encryptedData, byte[] encapsulatedKey, byte[] iv, String keyVersion) {
            this.encryptedData = encryptedData;
            this.encapsulatedKey = encapsulatedKey;
            this.iv = iv;
            this.keyVersion = keyVersion;
        }

        public byte[] getEncryptedData() { return encryptedData; }
        public byte[] getEncapsulatedKey() { return encapsulatedKey; }
        public byte[] getIv() { return iv; }
        public String getKeyVersion() { return keyVersion; }
    }
}
