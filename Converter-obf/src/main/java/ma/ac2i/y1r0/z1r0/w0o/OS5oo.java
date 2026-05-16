package ma.ac2i.y1r0.z1r0.w0o;

import lombok.AllArgsConstructor;
import ma.ac2i.y1r0.z1r0.s0o.R04oo;
import ma.ac2i.y1r0.z1r0.e0o.Structure;
import ma.ac2i.y1r0.z1r0.e0o.StructureDetail;
import ma.ac2i.y1r0.z1r0.r0o.W04oo;
import ma.ac2i.y1r0.z1r0.r0o.OW5oo;
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

@RestController
@AllArgsConstructor
@RequestMapping("/structurs")
public class OS5oo {
    private OW5oo s0x52$;
    private W04oo s0x44x52$;

    @PostMapping(value = "/store", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> s0x101$(@RequestBody Structure s0x52) {
        try {
            // Your implementation here
            System.out.println(s0x52.toString());

            // Create the main Structure object
            Structure s0x101 = new Structure();
            s0x101.setStrName(s0x52.getStrName());
            s0x101.setLibelle(s0x52.getLibelle());
            s0x101.setStrType(s0x52.getStrType());
            s0x101.setStructureOrigin(s0x52.getStructureOrigin());
            s0x101.setExpression(s0x52.getExpression());
            s0x101.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            Structure s0x53 = s0x52$.save(s0x101);
            List<StructureDetail> d0x52 = s0x52.getStructureDetails();
            if (d0x52 != null) {
                List<StructureDetail> s0x44 = d0x52.stream().map(d0x521 -> {
                    StructureDetail s0x441 = new StructureDetail();
                    s0x441.setName(d0x521.getName());
                    s0x441.setType(d0x521.getType());
                    s0x441.setLink(d0x521.getLink());
                    s0x441.setDecimal(d0x521.getDecimal());
                    s0x441.setPosition(d0x521.getPosition());
                    s0x441.setLongeur(d0x521.getLongeur());
                    s0x441.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                    StructureDetail s0x44x53 = s0x44x52$.save(s0x441);
                    return s0x44x53;
                }).collect(Collectors.toList());

                // Set the structureDetails list in the main Structure object
                s0x53.setStructureDetails(s0x44);
                s0x52$.save(s0x53);
            }
            System.out.println(s0x52.toString());
            Map<String, Object> o0x52 = new HashMap<>();
            o0x52.put("message","Ajouté avec succès" );
            return ResponseEntity.ok(o0x52);
        } catch (Exception e) {
            // Prepare the error response
            Map<String, Object> e0x52 = new HashMap<>();
            e0x52.put("timestamp", LocalDateTime.now());
            e0x52.put("error", "Internal Server Error");
            e0x52.put("message", R04oo.e0x4D$(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e0x52);
        }

    }

    @GetMapping("/check-name")
    public ResponseEntity<Object> checkName(@RequestParam(name = "name", defaultValue = "") String name, @RequestParam(name = "id", defaultValue = "") long i0x100) {
        long c0x116 = s0x52$.countbyname(name);
        if (i0x100 !=0 ) {
            c0x116 = s0x52$.countbyname(name,i0x100);
        }
        if (c0x116 > 0) {
            Map<String, Object> e0x52 = new HashMap<>();
            e0x52.put("timestamp", LocalDateTime.now());
            e0x52.put("error", "Internal Server Error");
            e0x52.put("message", "le nom de la structure est déjà utilisé");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e0x52);
        } else {
            Map<String, Object> o0x52 = new HashMap<>();
            o0x52.put("timestamp", LocalDateTime.now());
            o0x52.put("success", "good");
            o0x52.put("message", "le nom de la structure est valide");
            return ResponseEntity.ok(o0x52);
        }
    }

    @Transactional
    @PutMapping(value = "/update/{i0x100}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> u0x101$(@PathVariable long i0x100, @RequestBody Structure s0x52) {
        try {
            // Find the existing structure by ID
            Optional<Structure> o0x53 = s0x52$.findById(i0x100);

            if (!o0x53.isPresent()) {
                // If the structure is not found, return a not found response
                return ResponseEntity.notFound().build();
            }

            // Get the existing structure
            Structure e0x53 = o0x53.get();

            // Update the fields of the existing structure
            e0x53.setStrName(s0x52.getStrName());
            e0x53.setLibelle(s0x52.getLibelle());
            e0x53.setStrType(s0x52.getStrType());
            e0x53.setExpression(s0x52.getExpression());
            // Delete the existing structure details
            List<StructureDetail>  l0x5Fx5Fx5Fx100 = e0x53.getStructureDetails();
            e0x53.getStructureDetails().clear();
            s0x44x52$.deleteAll(l0x5Fx5Fx5Fx100);

            // Create new structure details based on the structureRequest
            List<StructureDetail> u0x44 = new ArrayList<>();
            List<StructureDetail> d0x52 = s0x52.getStructureDetails();

            if (d0x52 != null) {
                for (StructureDetail d0x521 : d0x52) {
                    StructureDetail n0x44 = new StructureDetail();
                    n0x44.setName(d0x521.getName());
                    n0x44.setType(d0x521.getType());
                    n0x44.setLink(d0x521.getLink());
                    n0x44.setDecimal(d0x521.getDecimal());
                    n0x44.setPosition(d0x521.getPosition());
                    n0x44.setLongeur(d0x521.getLongeur());
                    u0x44.add(s0x44x52$.save(n0x44));
                }
            }

            // Set the updated details to the existing structure
            e0x53.setStructureDetails(u0x44);

            // Save the updated structure
            Structure updatedStructure = s0x52$.save(e0x53);

            // Return the updated structure
            Map<String, Object> o0x52 = new HashMap<>();
            o0x52.put("message","Modifié avec succès" );
            return ResponseEntity.ok(o0x52);
        } catch (Exception e) {
            // Prepare the error response
            Map<String, Object> e0x52 = new HashMap<>();
            e0x52.put("timestamp", LocalDateTime.now());
            e0x52.put("error", "Internal Server Error");
            e0x52.put("message", R04oo.e0x4D$(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e0x52);
        }
    }

    // Helper method to find a detail by its ID
    private StructureDetail f0x44x42x49$(List<StructureDetail> d0x115, Integer i0x100) {
        if (i0x100 !=null) {
            for (StructureDetail d0x108 : d0x115) {
                if (d0x108.getId().equals(i0x100)) {
                    return d0x108;
                }
            }
        }
        return null;
    }

}
