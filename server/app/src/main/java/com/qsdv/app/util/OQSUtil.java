package com.qsdv.app.util;

import org.openquantumsafe.Common;
import org.openquantumsafe.KEMs;
import org.openquantumsafe.Sigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for Open Quantum Safe operations
 */
public class OQSUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(OQSUtil.class);
    
    private static boolean initialized = false;
    
    /**
     * Initialize OQS library if not already initialized
     */
    public static synchronized void initializeOQS() {
        if (!initialized) {
            try {
                Common.enable_system_load();
                initialized = true;
                logger.info("OQS library initialized successfully");
            } catch (Exception e) {
                logger.error("Failed to initialize OQS library", e);
                throw new RuntimeException("OQS initialization failed", e);
            }
        }
    }
    
    /**
     * Get list of available KEM algorithms
     */
    public static List<String> getAvailableKEMAlgorithms() {
        initializeOQS();
        return KEMs.get_enabled_KEMs();
    }
    
    /**
     * Get list of available signature algorithms
     */
    public static List<String> getAvailableSignatureAlgorithms() {
        initializeOQS();
        return Sigs.get_enabled_sigs();
    }
    
    /**
     * Get list of NIST standardized KEM algorithms
     */
    public static List<String> getNISTStandardKEMs() {
        List<String> nistKEMs = new ArrayList<>();
        List<String> allKEMs = getAvailableKEMAlgorithms();
        
        // ML-KEM (formerly Kyber) is the NIST standard
        for (String kem : allKEMs) {
            if (kem.contains("Kyber") || kem.contains("ML-KEM")) {
                nistKEMs.add(kem);
            }
        }
        
        return nistKEMs;
    }
    
    /**
     * Get list of NIST standardized signature algorithms
     */
    public static List<String> getNISTStandardSignatures() {
        List<String> nistSigs = new ArrayList<>();
        List<String> allSigs = getAvailableSignatureAlgorithms();
        
        // ML-DSA (formerly Dilithium), SLH-DSA (formerly SPHINCS+), and FN-DSA (formerly Falcon) are NIST standards
        for (String sig : allSigs) {
            if (sig.contains("Dilithium") || sig.contains("ML-DSA") ||
                sig.contains("Falcon") || sig.contains("FN-DSA") ||
                sig.contains("SPHINCS") || sig.contains("SLH-DSA")) {
                nistSigs.add(sig);
            }
        }
        
        return nistSigs;
    }
    
    /**
     * Check if a KEM algorithm is available
     */
    public static boolean isKEMAvailable(String algorithm) {
        initializeOQS();
        return KEMs.is_KEM_enabled(algorithm);
    }
    
    /**
     * Check if a signature algorithm is available
     */
    public static boolean isSignatureAvailable(String algorithm) {
        initializeOQS();
        return Sigs.is_sig_enabled(algorithm);
    }
    
    /**
     * Get recommended KEM algorithm based on security level
     * @param securityLevel 1 (128-bit), 3 (192-bit), or 5 (256-bit)
     */
    public static String getRecommendedKEM(int securityLevel) {
        switch (securityLevel) {
            case 1:
                return isKEMAvailable("Kyber512") ? "Kyber512" : "ML-KEM-512";
            case 3:
                return isKEMAvailable("Kyber768") ? "Kyber768" : "ML-KEM-768";
            case 5:
                return isKEMAvailable("Kyber1024") ? "Kyber1024" : "ML-KEM-1024";
            default:
                return isKEMAvailable("Kyber768") ? "Kyber768" : "ML-KEM-768"; // Default to medium security
        }
    }
    
    /**
     * Get recommended signature algorithm based on security level
     * @param securityLevel 2 (128-bit), 3 (192-bit), or 5 (256-bit)
     */
    public static String getRecommendedSignature(int securityLevel) {
        switch (securityLevel) {
            case 2:
                return isSignatureAvailable("Dilithium2") ? "Dilithium2" : "ML-DSA-44";
            case 3:
                return isSignatureAvailable("Dilithium3") ? "Dilithium3" : "ML-DSA-65";
            case 5:
                return isSignatureAvailable("Dilithium5") ? "Dilithium5" : "ML-DSA-87";
            default:
                return isSignatureAvailable("Dilithium3") ? "Dilithium3" : "ML-DSA-65"; // Default to medium security
        }
    }
    
    /**
     * Format algorithm info for logging
     */
    public static String formatAlgorithmInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== Available Post-Quantum Algorithms ===\n");
        
        sb.append("\nKEM Algorithms:\n");
        List<String> kems = getAvailableKEMAlgorithms();
        for (String kem : kems) {
            boolean isNIST = getNISTStandardKEMs().contains(kem);
            sb.append("  - ").append(kem);
            if (isNIST) {
                sb.append(" [NIST Standard]");
            }
            sb.append("\n");
        }
        
        sb.append("\nSignature Algorithms:\n");
        List<String> sigs = getAvailableSignatureAlgorithms();
        for (String sig : sigs) {
            boolean isNIST = getNISTStandardSignatures().contains(sig);
            sb.append("  - ").append(sig);
            if (isNIST) {
                sb.append(" [NIST Standard]");
            }
            sb.append("\n");
        }
        
        sb.append("\n=== Recommended Algorithms ===\n");
        sb.append("Security Level 1 (128-bit): KEM=").append(getRecommendedKEM(1))
          .append(", Sig=").append(getRecommendedSignature(2)).append("\n");
        sb.append("Security Level 3 (192-bit): KEM=").append(getRecommendedKEM(3))
          .append(", Sig=").append(getRecommendedSignature(3)).append("\n");
        sb.append("Security Level 5 (256-bit): KEM=").append(getRecommendedKEM(5))
          .append(", Sig=").append(getRecommendedSignature(5)).append("\n");
        
        return sb.toString();
    }
}