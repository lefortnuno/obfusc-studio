package ma.ac2i.converter.converter.repository;

import ma.ac2i.converter.converter.entities.Structure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StructureRepository extends JpaRepository<Structure,Long> {
    @Query("SELECT s FROM Structure s WHERE CONCAT(s.StrName, ' ', s.libelle, ' ', s.StrType) LIKE %:searchTerm%")
    Page<Structure> findByAnyColumnContaining(@Param("searchTerm") String searchTerm, Pageable pageable);
    @Query("SELECT count(s) FROM Structure s WHERE s.StrName LIKE :searchTerm ")
    long countbyname(@Param("searchTerm") String searchTerm);

    @Query("SELECT count(s) FROM Structure s WHERE s.StrName LIKE :searchTerm and s.id <> :id")
    long countbyname(@Param("searchTerm") String searchTerm,@Param("id") long id);
}
