package com.qsdv.app.repository;

import com.qsdv.app.entity.EncryptionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EncryptionKeyRepository extends JpaRepository<EncryptionKey, Long> {
    Optional<EncryptionKey> findByKeyVersion(String keyVersion);
    
    @Query("SELECT e FROM EncryptionKey e WHERE e.isActive = true ORDER BY e.createdAt DESC")
    List<EncryptionKey> findActiveKeys();
    
    @Query("SELECT e FROM EncryptionKey e WHERE e.isActive = true AND (e.expiresAt IS NULL OR e.expiresAt > :now) ORDER BY e.createdAt DESC")
    List<EncryptionKey> findValidKeys(@Param("now") LocalDateTime now);
    
    @Query("SELECT e FROM EncryptionKey e WHERE e.isActive = true AND (e.expiresAt IS NULL OR e.expiresAt > :now) ORDER BY e.createdAt DESC LIMIT 1")
    Optional<EncryptionKey> findLatestValidKey(@Param("now") LocalDateTime now);
    
    @Query("SELECT e FROM EncryptionKey e WHERE e.expiresAt IS NOT NULL AND e.expiresAt <= :now")
    List<EncryptionKey> findExpiredKeys(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(e) FROM EncryptionKey e WHERE e.isActive = true")
    Long countActiveKeys();
    
    List<EncryptionKey> findByAlgorithmOrderByCreatedAtDesc(String algorithm);
}
