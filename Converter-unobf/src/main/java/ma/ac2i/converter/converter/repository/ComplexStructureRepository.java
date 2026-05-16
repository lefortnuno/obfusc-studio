package ma.ac2i.converter.converter.repository;

import ma.ac2i.converter.converter.entities.ComplexStructure;
import ma.ac2i.converter.converter.entities.SubStructure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComplexStructureRepository extends JpaRepository<ComplexStructure, Long> {
    @Query("SELECT cs FROM ComplexStructure cs WHERE CONCAT(cs.name, ' ', cs.libelle) LIKE %:searchTerm%")
    Page<ComplexStructure> findByAnyColumnContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT count(cs) FROM ComplexStructure cs WHERE cs.name LIKE :searchTerm ")
    long countbyname(@Param("searchTerm") String searchTerm);

    @Query("SELECT count(cs) FROM ComplexStructure cs WHERE cs.name LIKE :searchTerm and cs.id <> :id")
    long countbyname(@Param("searchTerm") String searchTerm, @Param("id") long id);

    List<ComplexStructure> findBySubStructuresContaining(SubStructure subStructure);
}
