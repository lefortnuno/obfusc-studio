// src/main/java/ma/ac2i/converter/converter/repository/LicenceRepository.java
package ma.ac2i.converter.converter.repository;

import ma.ac2i.converter.converter.entities.Licence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LicenceRepository extends JpaRepository<Licence, Long> {
    Optional<Licence> findTopByOrderByIdDesc();
}
