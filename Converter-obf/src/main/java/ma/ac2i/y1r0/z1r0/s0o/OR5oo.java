package ma.ac2i.y1r0.z1r0.s0o;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ma.ac2i.y1r0.z1r0.e0o.ComplexStructure;
import ma.ac2i.y1r0.z1r0.e0o.SubStructure;
import ma.ac2i.y1r0.z1r0.r0o.W03o;
import ma.ac2i.y1r0.z1r0.r0o.O07wOo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class OR5oo {
    private W03o c0x53x52;
    private O07wOo s0x53x52;

    public void d0x53$(long s0x53x49) {
        SubStructure s0x53 = s0x53x52.findById(s0x53x49)
                .orElseThrow(() -> new EntityNotFoundException("SubStructure not found with id: " + s0x53x49));
        List<ComplexStructure> c0x53 = c0x53x52.findBySubStructuresContaining(s0x53);
        for (ComplexStructure c0x531 : c0x53) {
            c0x531.getSubStructures().remove(s0x53);
        }
        s0x53x52.delete(s0x53);
    }
}
