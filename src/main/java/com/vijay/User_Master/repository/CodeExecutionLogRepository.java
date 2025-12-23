package com.vijay.User_Master.repository;

import com.vijay.User_Master.entity.CodeExecutionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CodeExecutionLogRepository extends JpaRepository<CodeExecutionLog, Long> {

    Page<CodeExecutionLog> findByUserId(Long userId, Pageable pageable);

    List<CodeExecutionLog> findByUserIdOrderByExecutedAtDesc(Long userId);

    @Query("SELECT COUNT(cel) FROM CodeExecutionLog cel " +
           "WHERE cel.user.id = :userId " +
           "AND cel.executedAt >= :since")
    Long countExecutionsByUserSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT cel FROM CodeExecutionLog cel " +
           "WHERE cel.user.id = :userId " +
           "AND cel.status = 'SUCCESS' " +
           "ORDER BY cel.executedAt DESC")
    Page<CodeExecutionLog> findSuccessfulExecutionsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(cel) FROM CodeExecutionLog cel " +
           "WHERE cel.user.id = :userId AND cel.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
}
