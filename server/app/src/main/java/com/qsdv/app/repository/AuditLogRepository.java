package com.qsdv.app.repository;

import com.qsdv.app.entity.AuditLog;
import com.qsdv.app.entity.User;
import com.qsdv.app.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    Page<AuditLog> findByDocumentOrderByCreatedAtDesc(Document document, Pageable pageable);
    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.user = :user AND a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<AuditLog> findByUserAndDateRange(@Param("user") User user,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM AuditLog a WHERE a.success = false ORDER BY a.createdAt DESC")
    Page<AuditLog> findFailedOperations(Pageable pageable);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.user = :user AND a.action = :action AND a.createdAt >= :since")
    Long countByUserAndActionSince(@Param("user") User user, 
                                  @Param("action") String action, 
                                  @Param("since") LocalDateTime since);
    
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.createdAt >= :since GROUP BY a.action")
    List<Object[]> getActionStatisticsSince(@Param("since") LocalDateTime since);
}
