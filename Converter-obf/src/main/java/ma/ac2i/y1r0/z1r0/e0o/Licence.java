package ma.ac2i.y1r0.z1r0.e0o;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Licence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hostname", nullable = true)
    private String hostname = null;
 
    @Column(name = "exp_date", nullable = true)
    private LocalDate expDate =  null;

    @Column(name = "mod_base", nullable = true)
    private Boolean mod_base = null;
 
    @Column(name = "cle_licence")
    private String cleLicence;
}