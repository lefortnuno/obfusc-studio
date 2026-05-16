package ma.ac2i.y1r0.z1r0.r0o;

import ma.ac2i.y1r0.z1r0.e0o.SubStructure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface O07wOo extends JpaRepository<SubStructure,Long> {
    @Query("SELECT ss FROM SubStructure ss WHERE CONCAT(ss.name, ' ', ss.libelle) LIKE %:s0x54%")
    Page<SubStructure> findByAnyColumnContaining(@Param("s0x54") String s0x54, Pageable pageable);

    @Query("SELECT count(ss) FROM SubStructure ss WHERE ss.name LIKE :s0x54 ")
    long countbyname(@Param("s0x54") String s0x54);

    @Query("SELECT count(ss) FROM SubStructure ss WHERE ss.name LIKE :s0x54 and ss.id <> :id")
    long countbyname(@Param("s0x54") String s0x54,@Param("id") long id);
}
