// src/main/java/ma/ac2i/converter/converter/entities/Licence.java
package ma.ac2i.converter.converter.entities;

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