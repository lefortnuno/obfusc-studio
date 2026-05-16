package ma.ac2i.converter.converter.repository;

import ma.ac2i.converter.converter.entities.SubStructure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubStructureRepository extends JpaRepository<SubStructure,Long> {
    @Query("SELECT ss FROM SubStructure ss WHERE CONCAT(ss.name, ' ', ss.libelle) LIKE %:searchTerm%")
    Page<SubStructure> findByAnyColumnContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT count(ss) FROM SubStructure ss WHERE ss.name LIKE :searchTerm ")
    long countbyname(@Param("searchTerm") String searchTerm);

    @Query("SELECT count(ss) FROM SubStructure ss WHERE ss.name LIKE :searchTerm and ss.id <> :id")
    long countbyname(@Param("searchTerm") String searchTerm,@Param("id") long id);
}
