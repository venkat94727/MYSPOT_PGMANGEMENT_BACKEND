
package com.myspot.backend.repository;

import com.myspot.backend.entities.ExtraCharge;
import com.myspot.backend.entities.PGDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExtraChargeRepository extends JpaRepository<ExtraCharge, Long> {
    List<ExtraCharge> findByPgDetails(PGDetails pgDetails);
    List<ExtraCharge> findByPgDetailsAndIsActive(PGDetails pgDetails, Boolean isActive);
    List<ExtraCharge> findByPgDetailsAndChargeType(PGDetails pgDetails, String chargeType);
}