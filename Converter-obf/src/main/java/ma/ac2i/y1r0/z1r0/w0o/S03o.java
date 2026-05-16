package ma.ac2i.y1r0.z1r0.w0o;

import ma.ac2i.y1r0.z1r0.c0o.OoOo;
import ma.ac2i.y1r0.z1r0.e0o.ComplexStructure;
import ma.ac2i.y1r0.z1r0.e0o.Structure;
import ma.ac2i.y1r0.z1r0.r0o.W03o;
import ma.ac2i.y1r0.z1r0.r0o.OW5oo;
import ma.ac2i.y1r0.z1r0.s0o.R03o;
import ma.ac2i.y1r0.z1r0.s0o.RO6o0o;
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
public class S03o {
    private OW5oo s0x52$;
    private R03o c0x53$;
    private W03o c0x53x52$;

    public S03o(OW5oo s0x52$, R03o c0x53$, W03o c0x53x52$) {
        this.c0x53x52$ = c0x53x52$;
        this.s0x52$ = s0x52$;
        this.c0x53$ = c0x53$;
    }


    @GetMapping("")
    public String i0x120$(Model m0x108, @ModelAttribute("command") String c0x100) {
        m0x108.addAttribute("pageTitle", "Home");
        List<Structure> s0x4C = s0x52$.findAll();
        List<ComplexStructure> c0x53x4C = c0x53x52$.findAll();
        m0x108.addAttribute("structureList", s0x4C);
        m0x108.addAttribute("complexStructureList", c0x53x4C);
        m0x108.addAttribute("command", c0x100);
        return "index";
    }

    @GetMapping("/login")
    public String l0x110$(Model m0x108, HttpSession s0x53) {
        String e0x4D = (String) s0x53.getAttribute("errorMessage");
        if (e0x4D != null) {
            m0x108.addAttribute("errorMessage", e0x4D);
            s0x53.removeAttribute("errorMessage");
        }
        return "login";
    }


    @Value("${user.dir}")
    private String b0x114;

    @PostMapping("/convert")
    public String c0x114$(@RequestParam("file-in") MultipartFile f0x101, @RequestParam("structureList") long s0x105x100, @RequestParam("struc-type-in") String i0x5Fx101, @RequestParam("meta-in-in") String i0x5Fx97x5Fx101, @RequestParam("struc-type-out") String o0x5Fx101, @RequestParam("meta-in-out") String o0x5Fx97x5Fx101, @RequestParam(value = "meta-csv", defaultValue = "false") boolean m0x5Fx110, @RequestParam(value = "meta-plat", defaultValue = "false") boolean m0x5Fx116) {
        boolean d0x5Fx116 = false;
        boolean h0x5Fx110 = false;
        if (i0x5Fx101.equals("txt")) {
            d0x5Fx116 = m0x5Fx110;
        } else if (i0x5Fx101.equals("csv")) {
            h0x5Fx110 = m0x5Fx110; 
        }
        if (o0x5Fx101.equals("txt")) {
            d0x5Fx116 = m0x5Fx116;
        } else if (o0x5Fx101.equals("csv")) {
            h0x5Fx110 = m0x5Fx116;
        }
 
        String f0x110x101 = f0x101.getOriginalFilename().replaceAll("\\s", "_").replace("(", "").replace(")", "");
        if (!f0x101.isEmpty()) {
            try {
                // Create the directory if it doesn't exist
                File d0x121 = new File(b0x114 + File.separator + "upload");
                if (!d0x121.exists()) {
                    d0x121.mkdirs();
                }
                f0x101.transferTo(new File(b0x114 + File.separator + "upload" + File.separator +
                        f0x110x101));
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception appropriately
            }
        }
        OoOo f0x54 = new OoOo();
        Structure s0x101 = s0x52$.findById(s0x105x100).orElse(null);
        String t0x116 = "";
        if (s0x101 != null) {
            String l0x116 = "";
            if (o0x5Fx101.equals("xml")) {
                l0x116 = o0x5Fx97x5Fx101;
            } else if (i0x5Fx101.equals("xml")) {
                l0x116 = i0x5Fx97x5Fx101;
            }
            String l0x5Fx116x5Fx102=i0x5Fx101;
            if(i0x5Fx101.equals("excel-simp")) {
                l0x5Fx116x5Fx102="csv";
            }
            t0x116 = f0x54.m0x461$(b0x114, s0x101, l0x5Fx116x5Fx102, o0x5Fx101, d0x5Fx116, l0x116, h0x5Fx110,false);
        }
        String d0x114 = "";
        if (i0x5Fx101.equals("csv")) {
            d0x114 = i0x5Fx97x5Fx101;
        } else if (o0x5Fx101.equals("csv")) {
            d0x114 = o0x5Fx97x5Fx101;
        }
        if (i0x5Fx101.equals("excel")) {
            RO6o0o t0x44x53 = new RO6o0o(c0x53x52$);
            t0x44x53.setAppDirectory(b0x114);
            ComplexStructure c0x531 = c0x53x52$.findById(s0x105x100).orElse(null);
            t0x44x53.test(b0x114 + File.separator + "upload" + File.separator + f0x110x101, c0x531);
            return "redirect:/files/" + c0x531.getName() + "." + o0x5Fx101;
        } else if(i0x5Fx101.equals("excel-simp")) {
            RO6o0o t0x44x53 = new RO6o0o(c0x53x52$);
            t0x44x53.setAppDirectory(b0x114);
            String o0x5Fx1011 = t0x44x53.convertTablesToCsv(b0x114 + File.separator + "upload" + File.separator + f0x110x101,s0x101,o0x5Fx101,d0x114,m0x5Fx110,m0x5Fx116);
            if (o0x5Fx101.equals("csv")) {
                return "redirect:/files/"+o0x5Fx1011;
            } else {
                c0x53$.t0x110$(t0x116, o0x5Fx1011, b0x114 + File.separator + "download" + File.separator + s0x101.getStrName() + "_" + o0x5Fx101 + "." + o0x5Fx101, "csv", o0x5Fx101, ",");
            }
        } else {
            c0x53$.t0x110$(t0x116, b0x114 + File.separator + "upload" + File.separator + f0x110x101, b0x114 + File.separator + "download" + File.separator + s0x101.getStrName() + "_" + o0x5Fx101 + "." + o0x5Fx101, i0x5Fx101, o0x5Fx101, d0x114);
        }

        return "redirect:/files/" + s0x101.getStrName() + "_" + o0x5Fx101 + "." + o0x5Fx101;
    }

    @GetMapping("/convert")
    public String c0x43$(RedirectAttributes r0x41, @RequestParam("structureList") long s0x105x100, @RequestParam("struc-type-in") String i0x5Fx101, @RequestParam("meta-in-in") String i0x5Fx97x5Fx101, @RequestParam("struc-type-out") String o0x5Fx101, @RequestParam("meta-in-out") String o0x5Fx97x5Fx101, @RequestParam(value = "meta-csv", defaultValue = "false") boolean m0x5Fx110, @RequestParam(value = "meta-plat", defaultValue = "false") boolean m0x5Fx116) {
        boolean d0x5Fx116 = false;
        boolean h0x5Fx110 = false;
        if (i0x5Fx101.equals("txt")) {
            d0x5Fx116 = m0x5Fx110;
        } else if (i0x5Fx101.equals("csv")) {
            h0x5Fx110 = m0x5Fx110; 
        }
        if (o0x5Fx101.equals("txt")) {
            d0x5Fx116 = m0x5Fx116;
        } else if (o0x5Fx101.equals("csv")) {
            h0x5Fx110 = m0x5Fx116;
        }
        String d0x114 = "";
        if (i0x5Fx101.equals("csv")) {
            d0x114 = i0x5Fx97x5Fx101;
        } else if (o0x5Fx101.equals("csv")) {
            d0x114 = o0x5Fx97x5Fx101;
        }
        OoOo f0x54 = new OoOo();
        Structure s0x101 = s0x52$.findById(s0x105x100).orElse(null);
        String t0x116 = "";
        if (s0x101 != null) {
            String l0x116 = "";
            if (o0x5Fx101.equals("xml")) {
                l0x116 = o0x5Fx97x5Fx101;
            } else if (i0x5Fx101.equals("xml")) {
                l0x116 = i0x5Fx97x5Fx101;
            }
            t0x116 = f0x54.m0x461$(b0x114, s0x101, i0x5Fx101, o0x5Fx101, d0x5Fx116, l0x116, h0x5Fx110,true);
        }
        String c0x100 = "";
        if (!i0x5Fx101.equals("excel")) {
            c0x100 = c0x53$.t0x43$(t0x116, i0x5Fx101, o0x5Fx101, d0x114);
        }
        r0x41.addFlashAttribute("command", c0x100);
        return "redirect:/";
    }

    @GetMapping("/files/{f0x4E}")
    public ResponseEntity<Resource> g0x46$(@PathVariable String f0x4E) {
        File f0x101 = new File(b0x114 + File.separator + "download" + File.separator + f0x4E);
        if (f0x101.exists()) {
            Resource r0x101 = new FileSystemResource(f0x101);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + f0x4E + "\"")
                    .contentLength(f0x101.length())
                    .body(r0x101);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/refresh")
    @ResponseBody
    public ResponseEntity<Void> k0x65$() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/test")
    public String test() {
        /*RO6o0o tableDetectionService = new RO6o0o(c0x53x52$);
        tableDetectionService.setAppDirectory(basedir);
        tableDetectionService.test();*/
        return "redirect:/";
    }
}
