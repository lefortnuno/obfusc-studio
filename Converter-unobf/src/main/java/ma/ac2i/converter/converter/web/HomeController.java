package ma.ac2i.converter.converter.web;

import ma.ac2i.converter.converter.config.FileTemplate;
import ma.ac2i.converter.converter.entities.ComplexStructure;
import ma.ac2i.converter.converter.entities.Structure;
import ma.ac2i.converter.converter.repository.ComplexStructureRepository;
import ma.ac2i.converter.converter.repository.StructureRepository;
import ma.ac2i.converter.converter.service.ConveterService;
import ma.ac2i.converter.converter.service.TableDetectionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/")
public class HomeController {
    private StructureRepository structureRepository;
    private ConveterService conveterService; 
    private ComplexStructureRepository complexStructureRepository;

    public HomeController(StructureRepository structureRepository, ConveterService conveterService, ComplexStructureRepository complexStructureRepository) {
        this.complexStructureRepository = complexStructureRepository;
        this.structureRepository = structureRepository;
        this.conveterService = conveterService;
    }


    @GetMapping("")
    public String index(Model model, @ModelAttribute("command") String command) {
        model.addAttribute("pageTitle", "Home");
        List<Structure> structureList = structureRepository.findAll();
        List<ComplexStructure> complexStructureList = complexStructureRepository.findAll();
        model.addAttribute("structureList", structureList);
        model.addAttribute("complexStructureList", complexStructureList);
        model.addAttribute("command", command);
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model, HttpSession session) {
        String errorMessage = (String) session.getAttribute("errorMessage");
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
            session.removeAttribute("errorMessage");
        }
        return "login";
    }


    @Value("${user.dir}")
    private String basedir;

    @PostMapping("/convert")
    public String convert(@RequestParam("file-in") MultipartFile file, @RequestParam("structureList") long struckid, @RequestParam("struc-type-in") String input_type, @RequestParam("meta-in-in") String input_meta_type, @RequestParam("struc-type-out") String output_type, @RequestParam("meta-in-out") String output_meta_type, @RequestParam(value = "meta-csv", defaultValue = "false") boolean meta_in, @RequestParam(value = "meta-plat", defaultValue = "false") boolean meta_out) {
        boolean decimal_out = false;
        boolean header_in = false;
        if (input_type.equals("txt")) {
            decimal_out = meta_in;
        } else if (input_type.equals("csv")) {
            header_in = meta_in;
            System.out.println(meta_in);
            System.out.println(header_in);
        }
        if (output_type.equals("txt")) {
            decimal_out = meta_out;
        } else if (output_type.equals("csv")) {
            header_in = meta_out;
        }

        System.out.println(decimal_out);
        String filename = file.getOriginalFilename().replaceAll("\\s", "_").replace("(", "").replace(")", "");
        if (!file.isEmpty()) {
            try {
                // Create the directory if it doesn't exist
                File directory = new File(basedir + File.separator + "upload");
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                file.transferTo(new File(basedir + File.separator + "upload" + File.separator +
                        filename));
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception appropriately
            }
        }
        FileTemplate fileTemplate = new FileTemplate();
        Structure structure = structureRepository.findById(struckid).orElse(null);
        String test = "";
        if (structure != null) {
            String layout = "";
            if (output_type.equals("xml")) {
                layout = output_meta_type;
            } else if (input_type.equals("xml")) {
                layout = input_meta_type;
            }
            String local_input_type=input_type;
            if(input_type.equals("excel-simp")) {
                local_input_type="csv";
            }
            test = fileTemplate.makeFields(basedir, structure, local_input_type, output_type, decimal_out, layout, header_in,false);
        }
        String delimiter = "";
        if (input_type.equals("csv")) {
            delimiter = input_meta_type;
        } else if (output_type.equals("csv")) {
            delimiter = output_meta_type;
        }
        if (input_type.equals("excel")) {
            TableDetectionService tableDetectionService = new TableDetectionService(complexStructureRepository);
            tableDetectionService.setAppDirectory(basedir);
            ComplexStructure complexStructure = complexStructureRepository.findById(struckid).orElse(null);
            tableDetectionService.test(basedir + File.separator + "upload" + File.separator + filename, complexStructure);
            return "redirect:/files/" + complexStructure.getName() + "." + output_type;
        } else if(input_type.equals("excel-simp")) {
            TableDetectionService tableDetectionService = new TableDetectionService(complexStructureRepository);
            tableDetectionService.setAppDirectory(basedir);
            String oi_file = tableDetectionService.convertTablesToCsv(basedir + File.separator + "upload" + File.separator + filename,structure,output_type,delimiter,meta_in,meta_out);
            if (output_type.equals("csv")) {
                return "redirect:/files/"+oi_file;
            }else {
                conveterService.transformation(test, oi_file, basedir + File.separator + "download" + File.separator + structure.getStrName() + "_" + output_type + "." + output_type, "csv", output_type, ",");
            }
        } else {
            conveterService.transformation(test, basedir + File.separator + "upload" + File.separator + filename, basedir + File.separator + "download" + File.separator + structure.getStrName() + "_" + output_type + "." + output_type, input_type, output_type, delimiter);
        }

        return "redirect:/files/" + structure.getStrName() + "_" + output_type + "." + output_type;
    }

    @GetMapping("/convert")
    public String convertCommand(RedirectAttributes redirectAttributes, @RequestParam("structureList") long struckid, @RequestParam("struc-type-in") String input_type, @RequestParam("meta-in-in") String input_meta_type, @RequestParam("struc-type-out") String output_type, @RequestParam("meta-in-out") String output_meta_type, @RequestParam(value = "meta-csv", defaultValue = "false") boolean meta_in, @RequestParam(value = "meta-plat", defaultValue = "false") boolean meta_out) {
        boolean decimal_out = false;
        boolean header_in = false;
        if (input_type.equals("txt")) {
            decimal_out = meta_in;
        } else if (input_type.equals("csv")) {
            header_in = meta_in;
            System.out.println(meta_in);
            System.out.println(header_in);
        }
        if (output_type.equals("txt")) {
            decimal_out = meta_out;
        } else if (output_type.equals("csv")) {
            header_in = meta_out;
        }
        String delimiter = "";
        if (input_type.equals("csv")) {
            delimiter = input_meta_type;
        } else if (output_type.equals("csv")) {
            delimiter = output_meta_type;
        }
        FileTemplate fileTemplate = new FileTemplate();
        Structure structure = structureRepository.findById(struckid).orElse(null);
        String test = "";
        if (structure != null) {
            String layout = "";
            if (output_type.equals("xml")) {
                layout = output_meta_type;
            } else if (input_type.equals("xml")) {
                layout = input_meta_type;
            }
            test = fileTemplate.makeFields(basedir, structure, input_type, output_type, decimal_out, layout, header_in,true);
        }
        String command = "";
        if (!input_type.equals("excel")) {
            command = conveterService.transformationCommand(test, input_type, output_type, delimiter);
        }
        redirectAttributes.addFlashAttribute("command", command);
        return "redirect:/";
    }

    @GetMapping("/files/{fileName}")
    public ResponseEntity<Resource> getFile(@PathVariable String fileName) {
        File file = new File(basedir + File.separator + "download" + File.separator + fileName);
        if (file.exists()) {
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentLength(file.length())
                    .body(resource);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/refresh")
    @ResponseBody
    public ResponseEntity<Void> keepalive() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/test")
    public String test() {
        /*TableDetectionService tableDetectionService = new TableDetectionService(complexStructureRepository);
        tableDetectionService.setAppDirectory(basedir);
        tableDetectionService.test();*/
        return "redirect:/";
    }
}
