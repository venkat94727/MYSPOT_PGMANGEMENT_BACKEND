
package com.myspot.backend.repository;

import com.myspot.backend.entities.RuleRestriction;
import com.myspot.backend.entities.PGDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RuleRestrictionRepository extends JpaRepository<RuleRestriction, Long> {
    List<RuleRestriction> findByPgDetails(PGDetails pgDetails);
    List<RuleRestriction> findByPgDetailsAndIsActive(PGDetails pgDetails, Boolean isActive);
    List<RuleRestriction> findByPgDetailsOrderByDisplayOrderAsc(PGDetails pgDetails);


}
