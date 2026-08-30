package com.recoverai.repository;

import com.recoverai.domain.recovery.RecoveryCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

@Repository
public interface RecoveryCaseRepository extends JpaRepository<RecoveryCase, UUID> {

    @Query(value = "SELECT * FROM recovery_cases WHERE status IN ('ELIGIBLE', 'RETRY') AND (next_run_at IS NULL OR next_run_at <= CURRENT_TIMESTAMP) LIMIT 1 FOR UPDATE SKIP LOCKED", nativeQuery = true)
    Optional<RecoveryCase> findNextAvailableCase();

    @Query(value = "SELECT COUNT(*) FROM recovery_cases WHERE status = 'RECOVERED'", nativeQuery = true)
    long countRecoveredCases();

    @Query(value = "SELECT COUNT(*) FROM recovery_cases WHERE status != 'RECOVERED' AND status != 'STOPPED'", nativeQuery = true)
    long countActiveCases();

    @Query(value = "SELECT COALESCE(SUM(p.amount), 0) FROM recovery_cases rc JOIN payments p ON rc.payment_id = p.id WHERE rc.status = 'RECOVERED'", nativeQuery = true)
    java.math.BigDecimal sumRecoveredAmount();

    @Query(value = "SELECT to_char(rc.updated_at, 'Dy') as day, SUM(p.amount) as amount " +
                   "FROM recovery_cases rc " +
                   "JOIN payments p ON rc.payment_id = p.id " +
                   "WHERE rc.status = 'RECOVERED' " +
                   "GROUP BY DATE(rc.updated_at), to_char(rc.updated_at, 'Dy') " +
                   "ORDER BY DATE(rc.updated_at) ASC LIMIT 7", nativeQuery = true)
    java.util.List<Object[]> getRecoveryTrajectory();
}
