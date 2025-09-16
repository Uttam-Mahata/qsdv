package com.qsdv.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String filename;
    
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;
    
    @Column(name = "content_type")
    private String contentType;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "encrypted_size")
    private Long encryptedSize;
    
    @Lob
    @Column(name = "encrypted_data", nullable = false)
    private byte[] encryptedData;
    
    @Lob
    @Column(name = "encapsulated_key", nullable = false)
    private byte[] encapsulatedKey;
    
    @Lob
    @Column(name = "encryption_iv", nullable = false)
    private byte[] encryptionIv;
    
    @Column(name = "encryption_algorithm", nullable = false)
    private String encryptionAlgorithm;
    
    @Column(name = "key_version", nullable = false)
    private String keyVersion;
    
    @Column(name = "encryption_metadata", columnDefinition = "TEXT")
    private String encryptionMetadata;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AuditLog> auditLogs;
    
    // Constructors
    public Document() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Document(String filename, String originalFilename, String contentType, 
                   Long fileSize, byte[] encryptedData, byte[] encapsulatedKey, 
                   byte[] encryptionIv, String encryptionAlgorithm, 
                   String keyVersion, User owner) {
        this();
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.encryptedData = encryptedData;
        this.encapsulatedKey = encapsulatedKey;
        this.encryptionIv = encryptionIv;
        this.encryptionAlgorithm = encryptionAlgorithm;
        this.keyVersion = keyVersion;
        this.owner = owner;
        this.encryptedSize = (long) encryptedData.length;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public Long getEncryptedSize() { return encryptedSize; }
    public void setEncryptedSize(Long encryptedSize) { this.encryptedSize = encryptedSize; }
    
    public byte[] getEncryptedData() { return encryptedData; }
    public void setEncryptedData(byte[] encryptedData) { 
        this.encryptedData = encryptedData;
        if (encryptedData != null) {
            this.encryptedSize = (long) encryptedData.length;
        }
    }
    
    public byte[] getEncapsulatedKey() { return encapsulatedKey; }
    public void setEncapsulatedKey(byte[] encapsulatedKey) { this.encapsulatedKey = encapsulatedKey; }
    
    public byte[] getEncryptionIv() { return encryptionIv; }
    public void setEncryptionIv(byte[] encryptionIv) { this.encryptionIv = encryptionIv; }
    
    public String getEncryptionAlgorithm() { return encryptionAlgorithm; }
    public void setEncryptionAlgorithm(String encryptionAlgorithm) { this.encryptionAlgorithm = encryptionAlgorithm; }
    
    public String getKeyVersion() { return keyVersion; }
    public void setKeyVersion(String keyVersion) { this.keyVersion = keyVersion; }
    
    public String getEncryptionMetadata() { return encryptionMetadata; }
    public void setEncryptionMetadata(String encryptionMetadata) { this.encryptionMetadata = encryptionMetadata; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(LocalDateTime lastAccessed) { this.lastAccessed = lastAccessed; }
    
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    
    public List<AuditLog> getAuditLogs() { return auditLogs; }
    public void setAuditLogs(List<AuditLog> auditLogs) { this.auditLogs = auditLogs; }
}
