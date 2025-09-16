package com.qsdv.app.repository;

import com.qsdv.app.entity.Document;
import com.qsdv.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByOwner(User owner);
    List<Document> findByOwnerOrderByCreatedAtDesc(User owner);
    Optional<Document> findByIdAndOwner(Long id, User owner);
    
    @Query("SELECT d FROM Document d WHERE d.owner = :owner AND d.originalFilename LIKE %:filename%")
    List<Document> findByOwnerAndFilenameContaining(@Param("owner") User owner, @Param("filename") String filename);
    
    @Query("SELECT SUM(d.encryptedSize) FROM Document d WHERE d.owner = :owner")
    Long getTotalEncryptedSizeByOwner(@Param("owner") User owner);
    
    @Query("SELECT COUNT(d) FROM Document d WHERE d.owner = :owner")
    Long getDocumentCountByOwner(@Param("owner") User owner);
    
    @Query("SELECT d FROM Document d WHERE d.owner = :owner AND d.createdAt BETWEEN :startDate AND :endDate")
    List<Document> findByOwnerAndCreatedAtBetween(@Param("owner") User owner, 
                                                  @Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
}
