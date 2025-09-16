package com.qsdv.app.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.openquantumsafe.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Post-Quantum Cryptography Service using Open Quantum Safe (liboqs)
 * 
 * This service implements post-quantum cryptographic operations using
 * actual PQ algorithms from the Open Quantum Safe project.
 * It uses Kyber for Key Encapsulation Mechanism (KEM) and 
 * Dilithium for digital signatures, combined with AES-GCM for symmetric encryption.
 */
@Service
public class PostQuantumCryptoService {

    private static final Logger logger = LoggerFactory.getLogger(PostQuantumCryptoService.class);

    @Value("${qsdv.pq.kem.algorithm:Kyber512}")
    private String kemAlgorithm;
    
    @Value("${qsdv.pq.sig.algorithm:Dilithium2}")
    private String sigAlgorithm;

    @Value("${qsdv.pq.storage-path:./pq-keys}")
    private String keyStoragePath;

    @Value("${qsdv.pq.key-rotation-days:30}")
    private int keyRotationDays;

    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentHashMap<String, KEMKeyPairInfo> kemKeyCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SigKeyPairInfo> sigKeyCache = new ConcurrentHashMap<>();

    private static class KEMKeyPairInfo {
        public final byte[] publicKey;
        public final byte[] secretKey;
        public final LocalDateTime createdAt;
        public final String version;
        public final String algorithm;

        public KEMKeyPairInfo(byte[] publicKey, byte[] secretKey, String version, String algorithm) {
            this.publicKey = publicKey;
            this.secretKey = secretKey;
            this.createdAt = LocalDateTime.now();
            this.version = version;
            this.algorithm = algorithm;
        }
    }
    
    private static class SigKeyPairInfo {
        public final byte[] publicKey;
        public final byte[] secretKey;
        public final LocalDateTime createdAt;
        public final String version;
        public final String algorithm;

        public SigKeyPairInfo(byte[] publicKey, byte[] secretKey, String version, String algorithm) {
            this.publicKey = publicKey;
            this.secretKey = secretKey;
            this.createdAt = LocalDateTime.now();
            this.version = version;
            this.algorithm = algorithm;
        }
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing Post-Quantum Cryptography Service with Open Quantum Safe");
        
        try {
            // Enable OQS native library
            Common.enable_system_load();
            
            // Log available algorithms
            logger.info("Available KEM algorithms: {}", String.join(", ", KEMs.get_enabled_KEMs()));
            logger.info("Available Signature algorithms: {}", String.join(", ", Sigs.get_enabled_sigs()));
            
            // Validate selected algorithms
            if (!KEMs.is_KEM_enabled(kemAlgorithm)) {
                logger.warn("KEM algorithm {} not available, falling back to Kyber512", kemAlgorithm);
                kemAlgorithm = "Kyber512";
            }
            
            if (!Sigs.is_sig_enabled(sigAlgorithm)) {
                logger.warn("Signature algorithm {} not available, falling back to Dilithium2", sigAlgorithm);
                sigAlgorithm = "Dilithium2";
            }
            
            // Create key storage directory
            Path keyDir = Paths.get(keyStoragePath);
            if (!Files.exists(keyDir)) {
                Files.createDirectories(keyDir);
                logger.info("Created key storage directory: {}", keyStoragePath);
            }
            
            // Generate initial key pairs
            generateCurrentKEMKeyPair();
            generateCurrentSigKeyPair();
            
            logger.info("Post-Quantum Cryptography Service initialized with KEM: {} and Sig: {}", 
                       kemAlgorithm, sigAlgorithm);
            
        } catch (Exception e) {
            logger.error("Failed to initialize Post-Quantum Cryptography Service", e);
            throw new RuntimeException("PQ Service initialization failed", e);
        }
    }
    
    @PreDestroy
    public void cleanup() {
        // Clean up sensitive key material
        kemKeyCache.clear();
        sigKeyCache.clear();
        logger.info("Post-Quantum Cryptography Service cleaned up");
    }

    /**
     * Get the current KEM public key for encryption
     */
    public byte[] getCurrentKEMPublicKey() {
        KEMKeyPairInfo currentKey = getCurrentKEMKeyPair();
        return currentKey.publicKey;
    }

    /**
     * Get the current signature public key for verification
     */
    public byte[] getCurrentSigPublicKey() {
        SigKeyPairInfo currentKey = getCurrentSigKeyPair();
        return currentKey.publicKey;
    }

    /**
     * Get the current KEM key version
     */
    public String getCurrentKEMKeyVersion() {
        KEMKeyPairInfo currentKey = getCurrentKEMKeyPair();
        return currentKey.version;
    }

    /**
     * Get the current signature key version
     */
    public String getCurrentSigKeyVersion() {
        SigKeyPairInfo currentKey = getCurrentSigKeyPair();
        return currentKey.version;
    }

    /**
     * Encapsulate a secret using the current KEM public key
     */
    public PQEncapsulationResult encapsulateSecret() {
        try {
            KEMKeyPairInfo currentKey = getCurrentKEMKeyPair();
            
            // Create KEM instance
            KEM kem = new KEM(kemAlgorithm);
            
            // Generate shared secret and ciphertext
            Pair<byte[], byte[]> encapResult = kem.encap_secret(currentKey.publicKey);
            byte[] sharedSecret = encapResult.getLeft();
            byte[] ciphertext = encapResult.getRight();
            
            // Derive AES key from shared secret using KDF
            byte[] aesKey = deriveAESKey(sharedSecret);
            
            logger.debug("Successfully encapsulated secret with {} key version: {}", 
                        kemAlgorithm, currentKey.version);
            
            return new PQEncapsulationResult(
                ciphertext,
                aesKey,
                currentKey.version,
                kemAlgorithm
            );
            
        } catch (Exception e) {
            logger.error("Failed to encapsulate secret", e);
            throw new RuntimeException("PQ encapsulation failed", e);
        }
    }

    /**
     * Decapsulate a ciphertext to recover the shared secret
     */
    public byte[] decapsulateSecret(byte[] ciphertext, String keyVersion) {
        try {
            KEMKeyPairInfo keyPair = getKEMKeyPairByVersion(keyVersion);
            if (keyPair == null) {
                throw new IllegalArgumentException("KEM key version not found: " + keyVersion);
            }
            
            // Create KEM instance with the same algorithm
            KEM kem = new KEM(keyPair.algorithm);
            
            // Decapsulate to get shared secret
            byte[] sharedSecret = kem.decap_secret(ciphertext, keyPair.secretKey);
            
            // Derive AES key from shared secret
            byte[] aesKey = deriveAESKey(sharedSecret);
            
            logger.debug("Successfully decapsulated secret with {} key version: {}", 
                        keyPair.algorithm, keyVersion);
            
            return aesKey;
            
        } catch (Exception e) {
            logger.error("Failed to decapsulate secret for version: {}", keyVersion, e);
            throw new RuntimeException("PQ decapsulation failed", e);
        }
    }

    /**
     * Sign data using the current signature key
     */
    public PQSignatureResult signData(byte[] data) {
        try {
            SigKeyPairInfo currentKey = getCurrentSigKeyPair();
            
            // Create signature instance
            Signature sig = new Signature(sigAlgorithm);
            
            // Sign the data
            byte[] signature = sig.sign(data, currentKey.secretKey);
            
            logger.debug("Successfully signed data with {} key version: {}", 
                        sigAlgorithm, currentKey.version);
            
            return new PQSignatureResult(
                signature,
                currentKey.version,
                sigAlgorithm
            );
            
        } catch (Exception e) {
            logger.error("Failed to sign data", e);
            throw new RuntimeException("PQ signing failed", e);
        }
    }

    /**
     * Verify a signature using the appropriate public key
     */
    public boolean verifySignature(byte[] data, byte[] signature, String keyVersion) {
        try {
            SigKeyPairInfo keyPair = getSigKeyPairByVersion(keyVersion);
            if (keyPair == null) {
                throw new IllegalArgumentException("Signature key version not found: " + keyVersion);
            }
            
            // Create signature instance with the same algorithm
            Signature sig = new Signature(keyPair.algorithm);
            
            // Verify the signature
            boolean isValid = sig.verify(data, signature, keyPair.publicKey);
            
            logger.debug("Signature verification {} with {} key version: {}", 
                        isValid ? "succeeded" : "failed", keyPair.algorithm, keyVersion);
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("Failed to verify signature for version: {}", keyVersion, e);
            return false;
        }
    }

    /**
     * Encrypt data using AES-GCM with a PQ-KEM derived key
     */
    public PQEncryptionResult encryptData(byte[] data) {
        try {
            // Encapsulate a shared secret using PQ-KEM
            PQEncapsulationResult encapsulation = encapsulateSecret();
            
            // Use the derived AES key
            SecretKeySpec keySpec = new SecretKeySpec(encapsulation.getSharedSecret(), "AES");
            
            // Generate random IV
            byte[] iv = new byte[12]; // 96 bits for GCM
            secureRandom.nextBytes(iv);
            
            // Encrypt data with AES-GCM
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            byte[] encryptedData = cipher.doFinal(data);
            
            // Sign the encrypted data for authenticity
            PQSignatureResult signature = signData(encryptedData);
            
            logger.debug("Successfully encrypted data using PQ-KEM derived AES key");
            
            return new PQEncryptionResult(
                encryptedData,
                encapsulation.getCiphertext(),
                iv,
                encapsulation.getKeyVersion(),
                encapsulation.getAlgorithm(),
                signature.getSignature(),
                signature.getKeyVersion(),
                signature.getAlgorithm()
            );
            
        } catch (Exception e) {
            logger.error("Failed to encrypt data", e);
            throw new RuntimeException("PQ encryption failed", e);
        }
    }

    /**
     * Decrypt data using AES-GCM with a PQ-KEM derived key
     */
    public byte[] decryptData(PQEncryptionResult encryptionResult) {
        try {
            // Verify signature first
            boolean signatureValid = verifySignature(
                encryptionResult.getEncryptedData(),
                encryptionResult.getSignature(),
                encryptionResult.getSigKeyVersion()
            );
            
            if (!signatureValid) {
                throw new SecurityException("Invalid signature on encrypted data");
            }
            
            // Decapsulate the shared secret
            byte[] aesKey = decapsulateSecret(
                encryptionResult.getCiphertext(),
                encryptionResult.getKemKeyVersion()
            );
            
            // Use the derived AES key
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            
            // Decrypt data
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, encryptionResult.getIv());
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            byte[] decryptedData = cipher.doFinal(encryptionResult.getEncryptedData());
            
            logger.debug("Successfully decrypted data using PQ-KEM derived AES key");
            
            return decryptedData;
            
        } catch (Exception e) {
            logger.error("Failed to decrypt data", e);
            throw new RuntimeException("PQ decryption failed", e);
        }
    }

    /**
     * Derive AES key from shared secret using SHA-256
     */
    private byte[] deriveAESKey(byte[] sharedSecret) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(sharedSecret);
    }

    private KEMKeyPairInfo getCurrentKEMKeyPair() {
        String currentVersion = "kem_v1";
        return kemKeyCache.computeIfAbsent(currentVersion, k -> generateCurrentKEMKeyPair());
    }

    private SigKeyPairInfo getCurrentSigKeyPair() {
        String currentVersion = "sig_v1";
        return sigKeyCache.computeIfAbsent(currentVersion, k -> generateCurrentSigKeyPair());
    }

    private KEMKeyPairInfo getKEMKeyPairByVersion(String version) {
        return kemKeyCache.get(version);
    }

    private SigKeyPairInfo getSigKeyPairByVersion(String version) {
        return sigKeyCache.get(version);
    }

    private KEMKeyPairInfo generateCurrentKEMKeyPair() {
        try {
            // Generate KEM key pair using OQS
            KEM kem = new KEM(kemAlgorithm);
            byte[] publicKey = kem.generate_keypair();
            byte[] secretKey = kem.export_secret_key();
            
            String version = "kem_v1_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            
            KEMKeyPairInfo keyPairInfo = new KEMKeyPairInfo(publicKey, secretKey, version, kemAlgorithm);
            kemKeyCache.put("kem_v1", keyPairInfo); // Current version
            kemKeyCache.put(version, keyPairInfo); // Versioned access
            
            logger.info("Generated new {} KEM key pair with version: {}", kemAlgorithm, version);
            
            // Optionally save to file
            saveKEMKeyPair(keyPairInfo);
            
            return keyPairInfo;
            
        } catch (Exception e) {
            logger.error("Failed to generate KEM key pair", e);
            throw new RuntimeException("KEM key generation failed", e);
        }
    }

    private SigKeyPairInfo generateCurrentSigKeyPair() {
        try {
            // Generate signature key pair using OQS
            Signature sig = new Signature(sigAlgorithm);
            byte[] publicKey = sig.generate_keypair();
            byte[] secretKey = sig.export_secret_key();
            
            String version = "sig_v1_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            
            SigKeyPairInfo keyPairInfo = new SigKeyPairInfo(publicKey, secretKey, version, sigAlgorithm);
            sigKeyCache.put("sig_v1", keyPairInfo); // Current version
            sigKeyCache.put(version, keyPairInfo); // Versioned access
            
            logger.info("Generated new {} signature key pair with version: {}", sigAlgorithm, version);
            
            // Optionally save to file
            saveSigKeyPair(keyPairInfo);
            
            return keyPairInfo;
            
        } catch (Exception e) {
            logger.error("Failed to generate signature key pair", e);
            throw new RuntimeException("Signature key generation failed", e);
        }
    }

    private void saveKEMKeyPair(KEMKeyPairInfo keyPair) {
        try {
            Path keyDir = Paths.get(keyStoragePath);
            Path publicKeyPath = keyDir.resolve(keyPair.version + "_kem_public.key");
            Path secretKeyPath = keyDir.resolve(keyPair.version + "_kem_secret.key");
            
            Files.write(publicKeyPath, Base64.getEncoder().encode(keyPair.publicKey));
            Files.write(secretKeyPath, Base64.getEncoder().encode(keyPair.secretKey));
            
            logger.debug("Saved KEM key pair to files: {}", keyPair.version);
        } catch (Exception e) {
            logger.error("Failed to save KEM key pair to files", e);
        }
    }

    private void saveSigKeyPair(SigKeyPairInfo keyPair) {
        try {
            Path keyDir = Paths.get(keyStoragePath);
            Path publicKeyPath = keyDir.resolve(keyPair.version + "_sig_public.key");
            Path secretKeyPath = keyDir.resolve(keyPair.version + "_sig_secret.key");
            
            Files.write(publicKeyPath, Base64.getEncoder().encode(keyPair.publicKey));
            Files.write(secretKeyPath, Base64.getEncoder().encode(keyPair.secretKey));
            
            logger.debug("Saved signature key pair to files: {}", keyPair.version);
        } catch (Exception e) {
            logger.error("Failed to save signature key pair to files", e);
        }
    }

    // Result classes
    public static class PQEncapsulationResult {
        private final byte[] ciphertext;
        private final byte[] sharedSecret;
        private final String keyVersion;
        private final String algorithm;

        public PQEncapsulationResult(byte[] ciphertext, byte[] sharedSecret, String keyVersion, String algorithm) {
            this.ciphertext = ciphertext;
            this.sharedSecret = sharedSecret;
            this.keyVersion = keyVersion;
            this.algorithm = algorithm;
        }

        public byte[] getCiphertext() { return ciphertext; }
        public byte[] getSharedSecret() { return sharedSecret; }
        public String getKeyVersion() { return keyVersion; }
        public String getAlgorithm() { return algorithm; }
    }

    public static class PQSignatureResult {
        private final byte[] signature;
        private final String keyVersion;
        private final String algorithm;

        public PQSignatureResult(byte[] signature, String keyVersion, String algorithm) {
            this.signature = signature;
            this.keyVersion = keyVersion;
            this.algorithm = algorithm;
        }

        public byte[] getSignature() { return signature; }
        public String getKeyVersion() { return keyVersion; }
        public String getAlgorithm() { return algorithm; }
    }

    public static class PQEncryptionResult {
        private final byte[] encryptedData;
        private final byte[] ciphertext;
        private final byte[] iv;
        private final String kemKeyVersion;
        private final String kemAlgorithm;
        private final byte[] signature;
        private final String sigKeyVersion;
        private final String sigAlgorithm;

        public PQEncryptionResult(byte[] encryptedData, byte[] ciphertext, byte[] iv, 
                                 String kemKeyVersion, String kemAlgorithm,
                                 byte[] signature, String sigKeyVersion, String sigAlgorithm) {
            this.encryptedData = encryptedData;
            this.ciphertext = ciphertext;
            this.iv = iv;
            this.kemKeyVersion = kemKeyVersion;
            this.kemAlgorithm = kemAlgorithm;
            this.signature = signature;
            this.sigKeyVersion = sigKeyVersion;
            this.sigAlgorithm = sigAlgorithm;
        }

        public byte[] getEncryptedData() { return encryptedData; }
        public byte[] getCiphertext() { return ciphertext; }
        public byte[] getIv() { return iv; }
        public String getKemKeyVersion() { return kemKeyVersion; }
        public String getKemAlgorithm() { return kemAlgorithm; }
        public byte[] getSignature() { return signature; }
        public String getSigKeyVersion() { return sigKeyVersion; }
        public String getSigAlgorithm() { return sigAlgorithm; }
    }
}