package ma.ac2i.converter.converter.web;

import lombok.AllArgsConstructor;
import ma.ac2i.converter.converter.entities.Structure;
import ma.ac2i.converter.converter.entities.StructureDetail;
import ma.ac2i.converter.converter.repository.StructureDetailRepository;
import ma.ac2i.converter.converter.repository.StructureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import ma.ac2i.converter.converter.service.ErrorMessageHelper;

@RestController
@AllArgsConstructor
@RequestMapping("/structurs")
public class StructureRestController {
    private StructureRepository structureRepository;
    private StructureDetailRepository structureDetailRepository;

    @PostMapping(value = "/store", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> store(@RequestBody Structure structureRequest) {
        try {
            // Your implementation here
            System.out.println(structureRequest.toString());

            // Create the main Structure object
            Structure structure = new Structure();
            structure.setStrName(structureRequest.getStrName());
            structure.setLibelle(structureRequest.getLibelle());
            structure.setStrType(structureRequest.getStrType());
            structure.setStructureOrigin(structureRequest.getStructureOrigin());
            structure.setExpression(structureRequest.getExpression());
            structure.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            Structure structureSave = structureRepository.save(structure);
            List<StructureDetail> detailRequests = structureRequest.getStructureDetails();
            if (detailRequests != null) {
                List<StructureDetail> structureDetails = detailRequests.stream().map(detailRequest -> {
                    StructureDetail structureDetail = new StructureDetail();
                    structureDetail.setName(detailRequest.getName());
                    structureDetail.setType(detailRequest.getType());
                    structureDetail.setLink(detailRequest.getLink());
                    structureDetail.setDecimal(detailRequest.getDecimal());
                    structureDetail.setPosition(detailRequest.getPosition());
                    structureDetail.setLongeur(detailRequest.getLongeur());
                    structureDetail.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                    StructureDetail structureDetailsave = structureDetailRepository.save(structureDetail);
                    return structureDetailsave;
                }).collect(Collectors.toList());

                // Set the structureDetails list in the main Structure object
                structureSave.setStructureDetails(structureDetails);
                structureRepository.save(structureSave);
            }
            System.out.println(structureRequest.toString());
            Map<String, Object> okResponse = new HashMap<>();
            okResponse.put("message","Ajouté avec succès" );
            return ResponseEntity.ok(okResponse);
        } catch (Exception e) {
            // Prepare the error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", ErrorMessageHelper.toUserMessage(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

    }

    @GetMapping("/check-name")
    public ResponseEntity<Object> checkName(@RequestParam(name = "name", defaultValue = "") String name, @RequestParam(name = "id", defaultValue = "") long id) {
        long count = structureRepository.countbyname(name);
        if (id !=0 ) {
            count = structureRepository.countbyname(name,id);
        }
        if (count > 0) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "le nom de la structure est déjà utilisé");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } else {
            Map<String, Object> okResponse = new HashMap<>();
            okResponse.put("timestamp", LocalDateTime.now());
            okResponse.put("success", "good");
            okResponse.put("message", "le nom de la structure est valide");
            return ResponseEntity.ok(okResponse);
        }
    }

    @Transactional
    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> update(@PathVariable long id, @RequestBody Structure structureRequest) {
        try {
            // Find the existing structure by ID
            Optional<Structure> optionalStructure = structureRepository.findById(id);

            if (!optionalStructure.isPresent()) {
                // If the structure is not found, return a not found response
                return ResponseEntity.notFound().build();
            }

            // Get the existing structure
            Structure existingStructure = optionalStructure.get();

            // Update the fields of the existing structure
            existingStructure.setStrName(structureRequest.getStrName());
            existingStructure.setLibelle(structureRequest.getLibelle());
            existingStructure.setStrType(structureRequest.getStrType());
            existingStructure.setExpression(structureRequest.getExpression());
            // Delete the existing structure details
            List<StructureDetail>  list_to_be_deleted = existingStructure.getStructureDetails();
            existingStructure.getStructureDetails().clear();
            structureDetailRepository.deleteAll(list_to_be_deleted);

            // Create new structure details based on the structureRequest
            List<StructureDetail> updatedDetails = new ArrayList<>();
            List<StructureDetail> detailRequests = structureRequest.getStructureDetails();

            if (detailRequests != null) {
                for (StructureDetail detailRequest : detailRequests) {
                    StructureDetail newDetail = new StructureDetail();
                    newDetail.setName(detailRequest.getName());
                    newDetail.setType(detailRequest.getType());
                    newDetail.setLink(detailRequest.getLink());
                    newDetail.setDecimal(detailRequest.getDecimal());
                    newDetail.setPosition(detailRequest.getPosition());
                    newDetail.setLongeur(detailRequest.getLongeur());
                    updatedDetails.add(structureDetailRepository.save(newDetail));
                }
            }

            // Set the updated details to the existing structure
            existingStructure.setStructureDetails(updatedDetails);

            // Save the updated structure
            Structure updatedStructure = structureRepository.save(existingStructure);

            // Return the updated structure
            Map<String, Object> okResponse = new HashMap<>();
            okResponse.put("message","Modifié avec succès" );
            return ResponseEntity.ok(okResponse);
        } catch (Exception e) {
            // Prepare the error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", ErrorMessageHelper.toUserMessage(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Helper method to find a detail by its ID
    private StructureDetail findDetailById(List<StructureDetail> details, Integer id) {
        if (id !=null) {
            for (StructureDetail detail : details) {
                if (detail.getId().equals(id)) {
                    return detail;
                }
            }
        }
        return null;
    }

}
