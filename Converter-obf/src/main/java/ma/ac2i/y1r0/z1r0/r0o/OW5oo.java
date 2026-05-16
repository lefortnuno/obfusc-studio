package ma.ac2i.y1r0.z1r0.r0o;

import ma.ac2i.y1r0.z1r0.e0o.Structure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OW5oo extends JpaRepository<Structure,Long> {
    @Query("SELECT s FROM Structure s WHERE CONCAT(s.StrName, ' ', s.libelle, ' ', s.StrType) LIKE %:s0x54%")
    Page<Structure> findByAnyColumnContaining(@Param("s0x54") String s0x54, Pageable pageable);
    @Query("SELECT count(s) FROM Structure s WHERE s.StrName LIKE :s0x54 ")
    long countbyname(@Param("s0x54") String s0x54);

    @Query("SELECT count(s) FROM Structure s WHERE s.StrName LIKE :s0x54 and s.id <> :id")
    long countbyname(@Param("s0x54") String s0x54,@Param("id") long id);
}
