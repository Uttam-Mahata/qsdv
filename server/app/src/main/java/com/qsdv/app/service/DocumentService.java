package com.qsdv.app.service;

import com.qsdv.app.entity.AuditLog;
import com.qsdv.app.entity.Document;
import com.qsdv.app.entity.User;
import com.qsdv.app.repository.AuditLogRepository;
import com.qsdv.app.repository.DocumentRepository;
import com.qsdv.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DocumentService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private PostQuantumCryptoService cryptoService;
    
    /**
     * Upload and encrypt a document
     */
    public Document uploadDocument(MultipartFile file, String username, String encryptionMetadata) throws Exception {
        long startTime = System.currentTimeMillis();
        
        try {
            // Get or create user
            User user = userRepository.findByUsername(username)
                    .orElseGet(() -> {
                        User newUser = new User(username, username + "@qsdv.local");
                        return userRepository.save(newUser);
                    });
            
            // Generate unique filename
            String uniqueFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            
            // Encrypt the file data
            PostQuantumCryptoService.PQEncryptionResult encryptionResult = cryptoService.encryptData(file.getBytes());
            String keyVersion = cryptoService.getCurrentKeyVersion();
            
            // Create document entity
            Document document = new Document(
                    uniqueFilename,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    encryptionResult.getEncryptedData(),
                    encryptionResult.getEncapsulatedKey(),
                    encryptionResult.getIv(),
                    "RSA-OAEP+AES-GCM", // Our PQ simulation algorithm
                    keyVersion,
                    user
            );
            
            if (encryptionMetadata != null) {
                document.setEncryptionMetadata(encryptionMetadata);
            }
            
            // Save document
            document = documentRepository.save(document);
            
            // Create audit log
            long executionTime = System.currentTimeMillis() - startTime;
            AuditLog auditLog = AuditLog.documentUpload(document, user, executionTime);
            auditLogRepository.save(auditLog);
            
            logger.info("Document uploaded successfully: {} by user: {} in {}ms", 
                       document.getOriginalFilename(), username, executionTime);
            
            return document;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to upload document: {}", e.getMessage(), e);
            
            // Log failed upload attempt
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                AuditLog errorLog = AuditLog.encryptionOperation("UPLOAD_FAILED", user, false, e.getMessage(), executionTime);
                auditLogRepository.save(errorLog);
            }
            
            throw e;
        }
    }
    
    /**
     * Download and decrypt a document
     */
    public DocumentDownloadResult downloadDocument(Long documentId, String username) throws Exception {
        long startTime = System.currentTimeMillis();
        
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
            
            Document document = documentRepository.findByIdAndOwner(documentId, user)
                    .orElseThrow(() -> new IllegalArgumentException("Document not found or access denied"));
            
            // Decrypt the document data
            PostQuantumCryptoService.PQEncryptionResult encryptionResult = 
                new PostQuantumCryptoService.PQEncryptionResult(
                    document.getEncryptedData(),
                    document.getEncapsulatedKey(),
                    document.getEncryptionIv(),
                    document.getKeyVersion()
                );
            byte[] decryptedData = cryptoService.decryptData(encryptionResult);
            
            // Update last accessed time
            document.setLastAccessed(LocalDateTime.now());
            documentRepository.save(document);
            
            // Create audit log
            long executionTime = System.currentTimeMillis() - startTime;
            AuditLog auditLog = AuditLog.documentDownload(document, user, executionTime);
            auditLogRepository.save(auditLog);
            
            logger.info("Document downloaded successfully: {} by user: {} in {}ms", 
                       document.getOriginalFilename(), username, executionTime);
            
            return new DocumentDownloadResult(document, decryptedData);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to download document: {}", e.getMessage(), e);
            
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                AuditLog errorLog = AuditLog.encryptionOperation("DOWNLOAD_FAILED", user, false, e.getMessage(), executionTime);
                auditLogRepository.save(errorLog);
            }
            
            throw e;
        }
    }
    
    /**
     * List user's documents
     */
    public List<Document> getUserDocuments(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        return documentRepository.findByOwnerOrderByCreatedAtDesc(user);
    }
    
    /**
     * Get document metadata (without decrypting)
     */
    public Optional<Document> getDocumentMetadata(Long documentId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        return documentRepository.findByIdAndOwner(documentId, user);
    }
    
    /**
     * Delete a document
     */
    public boolean deleteDocument(Long documentId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        Optional<Document> documentOpt = documentRepository.findByIdAndOwner(documentId, user);
        if (documentOpt.isPresent()) {
            documentRepository.delete(documentOpt.get());
            
            // Create audit log
            AuditLog auditLog = new AuditLog("DELETE", "DOCUMENT", documentId, true, user);
            auditLog.setDetails("Document deleted: " + documentOpt.get().getOriginalFilename());
            auditLogRepository.save(auditLog);
            
            logger.info("Document deleted: {} by user: {}", documentOpt.get().getOriginalFilename(), username);
            return true;
        }
        
        return false;
    }
    
    /**
     * Get user statistics
     */
    public UserDocumentStats getUserStats(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        Long documentCount = documentRepository.getDocumentCountByOwner(user);
        Long totalEncryptedSize = documentRepository.getTotalEncryptedSizeByOwner(user);
        
        return new UserDocumentStats(documentCount != null ? documentCount : 0, 
                                   totalEncryptedSize != null ? totalEncryptedSize : 0);
    }
    
    // Inner classes for return types
    public static class DocumentDownloadResult {
        private final Document document;
        private final byte[] decryptedData;
        
        public DocumentDownloadResult(Document document, byte[] decryptedData) {
            this.document = document;
            this.decryptedData = decryptedData;
        }
        
        public Document getDocument() { return document; }
        public byte[] getDecryptedData() { return decryptedData; }
    }
    
    public static class UserDocumentStats {
        private final long documentCount;
        private final long totalEncryptedSize;
        
        public UserDocumentStats(long documentCount, long totalEncryptedSize) {
            this.documentCount = documentCount;
            this.totalEncryptedSize = totalEncryptedSize;
        }
        
        public long getDocumentCount() { return documentCount; }
        public long getTotalEncryptedSize() { return totalEncryptedSize; }
    }
}
