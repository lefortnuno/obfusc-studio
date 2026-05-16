package ma.ac2i.converter.converter.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import ma.ac2i.converter.converter.entities.ComplexStructure;
import ma.ac2i.converter.converter.entities.SubStructure;
import ma.ac2i.converter.converter.entities.SubStructureDetail;
import ma.ac2i.converter.converter.repository.ComplexStructureRepository;
import ma.ac2i.converter.converter.repository.SubStructureDetailRepository;
import ma.ac2i.converter.converter.repository.SubStructureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import ma.ac2i.converter.converter.service.ErrorMessageHelper;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/complex-structures")
@AllArgsConstructor
public class ComplexStructureRestController {
    private ComplexStructureRepository complexStructureRepository;
    private SubStructureRepository subStructureRepository;
    private SubStructureDetailRepository subStructureDetailRepository;


    @GetMapping("/main/sub-structure")
    public ResponseEntity<List<SubStructure>> get_structures() {
        List<SubStructure> subStructures = subStructureRepository.findAll();
        return ResponseEntity.ok(subStructures);
    }

    @GetMapping("/main/check-name")
    public ResponseEntity<Object> checkMainName(@RequestParam(name = "name", defaultValue = "") String name, @RequestParam(name = "id", defaultValue = "") long id) {
        long count = complexStructureRepository.countbyname(name);
        if (id != 0) {
            count = complexStructureRepository.countbyname(name, id);
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

    @PostMapping(path = "/main/store", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> mainstore(@RequestBody Map<String, Object> request) {
        try {
            String struckName = (String) request.get("name");
            String struckLibelle = (String) request.get("libelle");
            String bigExpression = (String) request.get("expression");
            List<Integer> listOfSubStructures = (List<Integer>) request.get("subStructuresids");
            ComplexStructure complexStructure = new ComplexStructure();
            complexStructure.setName(struckName);
            complexStructure.setLibelle(struckLibelle);
            complexStructure.setExpression(bigExpression);
            complexStructure.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            List<SubStructure> subStructureList = new ArrayList<>();
            for (int i = 0; i < listOfSubStructures.size(); i++) {
                SubStructure subStructure = subStructureRepository.findById(Long.valueOf(listOfSubStructures.get(i).toString())).orElse(null);
                if (subStructure == null) throw new RuntimeException("l'une des sous-structures est introuvable");
                subStructureList.add(subStructure);
            }
            complexStructure.setSubStructures(subStructureList);
            complexStructureRepository.save(complexStructure);
            Map<String, Object> okResponse = new HashMap<>();
            okResponse.put("message", "Ajouté avec succès");
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

    @PutMapping(path = "/main/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updateMainStore(@PathVariable("id") Long id, @RequestBody Map<String, Object> request) {
        try {
            ComplexStructure complexStructureOptional = complexStructureRepository.findById(id).orElse(null);
            if (complexStructureOptional == null) {
                throw new RuntimeException("La structure complexe avec l'identifiant " + id + " est introuvable");
            }

            ComplexStructure complexStructure = complexStructureOptional;
            String struckName = (String) request.get("name");
            String struckLibelle = (String) request.get("libelle");
            String bigExpression = (String) request.get("expression");
            List<Integer> listOfSubStructures = (List<Integer>) request.get("subStructuresids");

            complexStructure.setName(struckName);
            complexStructure.setLibelle(struckLibelle);
            complexStructure.setExpression(bigExpression);

            List<SubStructure> subStructureList = new ArrayList<>();
            for (int i = 0; i < listOfSubStructures.size(); i++) {
                SubStructure subStructure = subStructureRepository.findById(Long.valueOf(listOfSubStructures.get(i).toString())).orElse(null);
                if (subStructure == null) throw new RuntimeException("L'une des sous-structures est introuvable");
                subStructureList.add(subStructure);
            }

            complexStructure.setSubStructures(subStructureList);
            complexStructureRepository.save(complexStructure);

            Map<String, Object> okResponse = new HashMap<>();
            okResponse.put("message", "Modifié avec succès");
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

    /*

       _____       _        _____ _                   _
      / ____|     | |      / ____| |                 | |
     | (___  _   _| |__   | (___ | |_ _ __ _   _  ___| |_ _   _ _ __ ___
      \___ \| | | | '_ \   \___ \| __| '__| | | |/ __| __| | | | '__/ _ \
      ____) | |_| | |_) |  ____) | |_| |  | |_| | (__| |_| |_| | | |  __/
     |_____/ \__,_|_.__/  |_____/ \__|_|   \__,_|\___|\__|\__,_|_|  \___|


*/
    @GetMapping("/sub/check-name")
    public ResponseEntity<Object> checkSubName(@RequestParam(name = "name", defaultValue = "") String name, @RequestParam(name = "id", defaultValue = "") long id) {
        long count = subStructureRepository.countbyname(name);
        if (id != 0) {
            count = subStructureRepository.countbyname(name, id);
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

    @PostMapping("/sub/store")
    public ResponseEntity<Object> substore(@RequestBody SubStructure subStructure) {
        try {
            if (subStructure.getSubStructureDetails() != null) {
                for (SubStructureDetail detail : subStructure.getSubStructureDetails()) {
                    subStructureDetailRepository.save(detail);
                }
            }
            subStructure.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            SubStructure savedSubStructure = subStructureRepository.save(subStructure);
            Map<String, Object> okResponse = new HashMap<>();
            okResponse.put("message", "Ajouté avec succès");
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

    @PutMapping(value = "/sub/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> subUpdate(@PathVariable long id, @RequestBody SubStructure updatedSubStructure) {
        try {
            if (!subStructureRepository.existsById(id)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            if (updatedSubStructure.getSubStructureDetails() != null) {
                for (SubStructureDetail detail : updatedSubStructure.getSubStructureDetails()) {
                    subStructureDetailRepository.save(detail);
                }
            }
            updatedSubStructure.setId(id);
            SubStructure savedSubStructure = subStructureRepository.save(updatedSubStructure);
            Map<String, Object> okResponse = new HashMap<>();
            okResponse.put("message", "Modifié avec succès");
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

}
