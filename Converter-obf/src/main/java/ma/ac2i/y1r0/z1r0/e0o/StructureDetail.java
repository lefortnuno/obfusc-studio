package ma.ac2i.y1r0.z1r0.e0o;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class StructureDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String type;
    private String link;
    private Integer decimal;
    private Integer position;
    private Integer longeur;
    private Timestamp createdAt;

}
