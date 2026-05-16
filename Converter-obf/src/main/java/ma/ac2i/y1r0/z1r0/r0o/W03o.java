package ma.ac2i.y1r0.z1r0.r0o;

import ma.ac2i.y1r0.z1r0.e0o.ComplexStructure;
import ma.ac2i.y1r0.z1r0.e0o.SubStructure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface W03o extends JpaRepository<ComplexStructure, Long> {
    @Query("SELECT cs FROM ComplexStructure cs WHERE CONCAT(cs.name, ' ', cs.libelle) LIKE %:s0x54%")
    Page<ComplexStructure> findByAnyColumnContaining(@Param("s0x54") String s0x54, Pageable pageable);

    @Query("SELECT count(cs) FROM ComplexStructure cs WHERE cs.name LIKE :s0x54 ")
    long countbyname(@Param("s0x54") String s0x54);

    @Query("SELECT count(cs) FROM ComplexStructure cs WHERE cs.name LIKE :s0x54 and cs.id <> :id")
    long countbyname(@Param("s0x54") String s0x54, @Param("id") long id);

    List<ComplexStructure> findBySubStructuresContaining(SubStructure subStructure);
}
