package com.qsdv.app.controller;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qsdv.app.service.PostQuantumCryptoService;

@RestController
@RequestMapping("/keys")
@CrossOrigin(origins = "*")
public class PostQuantumKeyController {

    private static final Logger logger = LoggerFactory.getLogger(PostQuantumKeyController.class);

    @Autowired
    private PostQuantumCryptoService pqCryptoService;

    /**
     * Get the current post-quantum public key
     */
    @GetMapping("/pq/v1")
    public ResponseEntity<Map<String, Object>> getCurrentPQKey() {
        try {
            byte[] publicKey = pqCryptoService.getCurrentPublicKey();
            String keyVersion = pqCryptoService.getCurrentKeyVersion();
            
            Map<String, Object> response = new HashMap<>();
            response.put("publicKey", Base64.getEncoder().encodeToString(publicKey));
            response.put("keyVersion", keyVersion);
            response.put("algorithm", "Kyber512-Simulated"); // In production: "Kyber512"
            response.put("format", "DER");
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("keyUsage", "Key Encapsulation Mechanism (KEM)");
            response.put("description", "Post-quantum public key for secure document encryption");
            
            logger.info("Served PQ public key version: {}", keyVersion);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to retrieve PQ public key", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to retrieve post-quantum public key", 
                           "message", e.getMessage()));
        }
    }

    /**
     * Test the PQ encryption/decryption round-trip
     */
    @PostMapping("/pq/test")
    public ResponseEntity<Map<String, Object>> testPQCrypto(@RequestBody Map<String, String> request) {
        try {
            String testData = request.getOrDefault("data", "Hello, Post-Quantum World!");
            byte[] data = testData.getBytes();
            
            // Encrypt data
            long encryptStart = System.currentTimeMillis();
            PostQuantumCryptoService.PQEncryptionResult encrypted = pqCryptoService.encryptData(data);
            long encryptTime = System.currentTimeMillis() - encryptStart;
            
            // Decrypt data
            long decryptStart = System.currentTimeMillis();
            byte[] decrypted = pqCryptoService.decryptData(encrypted);
            long decryptTime = System.currentTimeMillis() - decryptStart;
            
            String decryptedData = new String(decrypted);
            boolean success = testData.equals(decryptedData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("original", testData);
            response.put("decrypted", decryptedData);
            response.put("keyVersion", encrypted.getKeyVersion());
            response.put("encryptionTimeMs", encryptTime);
            response.put("decryptionTimeMs", decryptTime);
            response.put("totalTimeMs", encryptTime + decryptTime);
            response.put("encryptedSize", encrypted.getEncryptedData().length);
            response.put("originalSize", data.length);
            response.put("sizeOverhead", encrypted.getEncryptedData().length - data.length);
            response.put("encapsulatedKeySize", encrypted.getEncapsulatedKey().length);
            response.put("timestamp", LocalDateTime.now().toString());
            
            logger.info("PQ crypto test completed: success={}, encryptTime={}ms, decryptTime={}ms", 
                       success, encryptTime, decryptTime);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("PQ crypto test failed", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false,
                           "error", "Post-quantum crypto test failed",
                           "message", e.getMessage()));
        }
    }

    /**
     * Get PQ cryptography service status
     */
    @GetMapping("/pq/status")
    public ResponseEntity<Map<String, Object>> getPQStatus() {
        try {
            String keyVersion = pqCryptoService.getCurrentKeyVersion();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "operational");
            response.put("algorithm", "Kyber512-Simulated");
            response.put("currentKeyVersion", keyVersion);
            response.put("provider", "BouncyCastle");
            response.put("keyRotationDays", 30);
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("description", "Post-quantum cryptography service using simulated Kyber KEM");
            response.put("note", "This implementation uses RSA-OAEP to simulate PQ behavior. " +
                               "In production, this would use actual Kyber or other NIST-approved PQ algorithms.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get PQ status", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("status", "error",
                           "error", "Failed to retrieve PQ service status",
                           "message", e.getMessage()));
        }
    }
}
