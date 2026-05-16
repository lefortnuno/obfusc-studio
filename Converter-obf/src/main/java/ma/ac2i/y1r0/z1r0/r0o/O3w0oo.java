// src/main/java/ma/ac2i/converter/converter/repository/O3w0oo.java
package ma.ac2i.y1r0.z1r0.r0o;

import ma.ac2i.y1r0.z1r0.e0o.Licence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface O3w0oo extends JpaRepository<Licence, Long> {
    Optional<Licence> findTopByOrderByIdDesc();
}
