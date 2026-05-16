package ma.ac2i.y1r0.z1r0.e0o;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Structure {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(unique = true)
    private String StrName;
    private String libelle;
    private String StrType;
    private Integer StructureOrigin;
    @Lob
    @Column(columnDefinition = "CLOB")
    private String expression;
    private Timestamp createdAt;
    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<StructureDetail> structureDetails;
}
