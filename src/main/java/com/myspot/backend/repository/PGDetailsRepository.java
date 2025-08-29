
package com.myspot.backend.repository;

import com.myspot.backend.entities.PGDetails;
import com.myspot.backend.entities.PGManagementOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PGDetailsRepository extends JpaRepository<PGDetails, Long> {
    Optional<PGDetails> findByPgManagementOwner(PGManagementOwner pgManagementOwner);
    Optional<PGDetails> findByPgManagementOwner_PgId(Long pgId);
    
    @Query("SELECT pd FROM PGDetails pd WHERE pd.pgManagementOwner.pgId = :pgId AND pd.isActive = true")
    Optional<PGDetails> findActiveByPgId(@Param("pgId") Long pgId);
}