package com.qsdv.app.controller;

import com.qsdv.app.entity.Document;
import com.qsdv.app.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:5173")
public class DocumentController {
    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);
    
    @Autowired
    private DocumentService documentService;
    
    /**
     * Upload a new document
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "username", defaultValue = "demo-user") String username,
            @RequestParam(value = "metadata", required = false) String metadata) {
        
        long startTime = System.currentTimeMillis();
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (file.getSize() > 50 * 1024 * 1024) { // 50MB limit
                response.put("success", false);
                response.put("error", "File size exceeds 50MB limit");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Upload and encrypt document
            Document document = documentService.uploadDocument(file, username, metadata);
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            response.put("success", true);
            response.put("documentId", document.getId());
            response.put("filename", document.getOriginalFilename());
            response.put("originalSize", document.getFileSize());
            response.put("encryptedSize", document.getEncryptedSize());
            response.put("encryptionAlgorithm", document.getEncryptionAlgorithm());
            response.put("keyVersion", document.getKeyVersion());
            response.put("uploadTimeMs", executionTime);
            response.put("createdAt", document.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            logger.info("Document uploaded successfully: {} in {}ms", document.getOriginalFilename(), executionTime);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to upload document: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("executionTimeMs", executionTime);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Download a document
     */
    @GetMapping("/download/{documentId}")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long documentId,
            @RequestParam(value = "username", defaultValue = "demo-user") String username) {
        
        try {
            DocumentService.DocumentDownloadResult result = documentService.downloadDocument(documentId, username);
            Document document = result.getDocument();
            byte[] decryptedData = result.getDecryptedData();
            
            ByteArrayResource resource = new ByteArrayResource(decryptedData);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + document.getOriginalFilename() + "\"")
                    .contentType(MediaType.parseMediaType(document.getContentType()))
                    .contentLength(decryptedData.length)
                    .body(resource);
                    
        } catch (Exception e) {
            logger.error("Failed to download document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * List user's documents
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listDocuments(
            @RequestParam(value = "username", defaultValue = "demo-user") String username) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Document> documents = documentService.getUserDocuments(username);
            DocumentService.UserDocumentStats stats = documentService.getUserStats(username);
            
            response.put("success", true);
            response.put("documents", documents.stream().map(doc -> {
                Map<String, Object> docInfo = new HashMap<>();
                docInfo.put("id", doc.getId());
                docInfo.put("filename", doc.getOriginalFilename());
                docInfo.put("contentType", doc.getContentType());
                docInfo.put("originalSize", doc.getFileSize());
                docInfo.put("encryptedSize", doc.getEncryptedSize());
                docInfo.put("encryptionAlgorithm", doc.getEncryptionAlgorithm());
                docInfo.put("keyVersion", doc.getKeyVersion());
                docInfo.put("createdAt", doc.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                docInfo.put("lastAccessed", doc.getLastAccessed() != null ? 
                           doc.getLastAccessed().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
                return docInfo;
            }).toList());
            
            response.put("stats", Map.of(
                "totalDocuments", stats.getDocumentCount(),
                "totalEncryptedSize", stats.getTotalEncryptedSize()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to list documents: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get document metadata
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<Map<String, Object>> getDocumentMetadata(
            @PathVariable Long documentId,
            @RequestParam(value = "username", defaultValue = "demo-user") String username) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Document> documentOpt = documentService.getDocumentMetadata(documentId, username);
            
            if (documentOpt.isEmpty()) {
                response.put("success", false);
                response.put("error", "Document not found or access denied");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Document document = documentOpt.get();
            
            response.put("success", true);
            response.put("id", document.getId());
            response.put("filename", document.getOriginalFilename());
            response.put("contentType", document.getContentType());
            response.put("originalSize", document.getFileSize());
            response.put("encryptedSize", document.getEncryptedSize());
            response.put("encryptionAlgorithm", document.getEncryptionAlgorithm());
            response.put("keyVersion", document.getKeyVersion());
            response.put("createdAt", document.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            response.put("lastAccessed", document.getLastAccessed() != null ? 
                        document.getLastAccessed().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get document metadata: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Delete a document
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Map<String, Object>> deleteDocument(
            @PathVariable Long documentId,
            @RequestParam(value = "username", defaultValue = "demo-user") String username) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean deleted = documentService.deleteDocument(documentId, username);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "Document deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", "Document not found or access denied");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Failed to delete document: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
