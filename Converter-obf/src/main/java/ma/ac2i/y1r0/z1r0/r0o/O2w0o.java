package ma.ac2i.y1r0.z1r0.r0o;

import ma.ac2i.y1r0.z1r0.e0o.AppUser;
import ma.ac2i.y1r0.z1r0.s0o.R04oo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface O2w0o extends JpaRepository<AppUser,String> {
    AppUser findByUsername(String username); 
    
    @Query("SELECT u FROM AppUser u WHERE CONCAT(u.username, ' ', u.firstname, ' ', u.lastname) LIKE %:s0x54%")
    Page<AppUser> findByAnyColumnContaining(@Param("s0x54") String s0x54, Pageable pageable);
}
