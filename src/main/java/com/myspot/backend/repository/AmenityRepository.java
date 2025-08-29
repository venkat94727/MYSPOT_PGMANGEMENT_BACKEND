
package com.myspot.backend.repository;

import com.myspot.backend.entities.Amenity;
import com.myspot.backend.entities.PGDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    List<Amenity> findByPgDetails(PGDetails pgDetails);
    List<Amenity> findByPgDetailsAndIsAvailable(PGDetails pgDetails, Boolean isAvailable);
    List<Amenity> findByPgDetailsAndCategory(PGDetails pgDetails, String category);
   
}
