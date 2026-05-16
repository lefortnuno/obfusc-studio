package ma.ac2i.converter.converter.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ma.ac2i.converter.converter.entities.ComplexStructure;
import ma.ac2i.converter.converter.entities.SubStructure;
import ma.ac2i.converter.converter.repository.ComplexStructureRepository;
import ma.ac2i.converter.converter.repository.SubStructureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class SubStructureService {
    private ComplexStructureRepository complexStructureRepository;
    private SubStructureRepository subStructureRepository;

    public void deleteSubStructure(long subStructureId) {
        SubStructure subStructure = subStructureRepository.findById(subStructureId)
                .orElseThrow(() -> new EntityNotFoundException("SubStructure not found with id: " + subStructureId));
        List<ComplexStructure> complexStructures = complexStructureRepository.findBySubStructuresContaining(subStructure);
        for (ComplexStructure complexStructure : complexStructures) {
            complexStructure.getSubStructures().remove(subStructure);
        }
        subStructureRepository.delete(subStructure);
    }
}
