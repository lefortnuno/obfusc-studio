package ma.ac2i.converter.converter.repository;

import ma.ac2i.converter.converter.entities.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppUserRepository extends JpaRepository<AppUser,String> {
    AppUser findByUsername(String username);

    @Query("SELECT u FROM AppUser u WHERE CONCAT(u.username, ' ', u.firstname, ' ', u.lastname) LIKE %:searchTerm%")
    Page<AppUser> findByAnyColumnContaining(@Param("searchTerm") String searchTerm, Pageable pageable);
}
