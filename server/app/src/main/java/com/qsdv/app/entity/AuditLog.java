package com.qsdv.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String action;
    
    @Column(name = "resource_type", nullable = false)
    private String resourceType;
    
    @Column(name = "resource_id")
    private Long resourceId;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(nullable = false)
    private boolean success;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;
    
    // Constructors
    public AuditLog() {
        this.createdAt = LocalDateTime.now();
    }
    
    public AuditLog(String action, String resourceType, Long resourceId, 
                   boolean success, User user) {
        this();
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.success = success;
        this.user = user;
    }
    
    // Static factory methods for common audit events
    public static AuditLog documentUpload(Document document, User user, long executionTimeMs) {
        AuditLog log = new AuditLog("UPLOAD", "DOCUMENT", document.getId(), true, user);
        log.setDocument(document);
        log.setExecutionTimeMs(executionTimeMs);
        log.setDetails("Document encrypted and uploaded: " + document.getOriginalFilename());
        return log;
    }
    
    public static AuditLog documentDownload(Document document, User user, long executionTimeMs) {
        AuditLog log = new AuditLog("DOWNLOAD", "DOCUMENT", document.getId(), true, user);
        log.setDocument(document);
        log.setExecutionTimeMs(executionTimeMs);
        log.setDetails("Document decrypted and downloaded: " + document.getOriginalFilename());
        return log;
    }
    
    public static AuditLog keyGeneration(String keyVersion, User user, long executionTimeMs) {
        AuditLog log = new AuditLog("KEY_GENERATION", "ENCRYPTION_KEY", null, true, user);
        log.setExecutionTimeMs(executionTimeMs);
        log.setDetails("Post-quantum key generated: " + keyVersion);
        return log;
    }
    
    public static AuditLog encryptionOperation(String operation, User user, boolean success, 
                                              String errorMessage, long executionTimeMs) {
        AuditLog log = new AuditLog(operation, "ENCRYPTION", null, success, user);
        log.setExecutionTimeMs(executionTimeMs);
        if (!success && errorMessage != null) {
            log.setErrorMessage(errorMessage);
        }
        return log;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }
}
