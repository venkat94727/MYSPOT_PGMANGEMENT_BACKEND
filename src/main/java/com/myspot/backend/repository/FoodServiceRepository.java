
package com.myspot.backend.repository;

import com.myspot.backend.entities.FoodService;
import com.myspot.backend.entities.PGDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FoodServiceRepository extends JpaRepository<FoodService, Long> {
    List<FoodService> findByPgDetails(PGDetails pgDetails);
    List<FoodService> findByPgDetailsAndIsAvailable(PGDetails pgDetails, Boolean isAvailable);
    List<FoodService> findByPgDetailsAndServiceType(PGDetails pgDetails, String serviceType);
}
