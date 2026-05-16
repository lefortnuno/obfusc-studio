package ma.ac2i.converter.converter.web;

import lombok.AllArgsConstructor;
import ma.ac2i.converter.converter.entities.ComplexStructure;
import ma.ac2i.converter.converter.entities.Structure;
import ma.ac2i.converter.converter.entities.SubStructure;
import ma.ac2i.converter.converter.entities.SubStructureDetail;
import ma.ac2i.converter.converter.repository.ComplexStructureRepository;
import ma.ac2i.converter.converter.repository.SubStructureRepository;
import ma.ac2i.converter.converter.service.SubStructureService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/complex-structures")
@AllArgsConstructor
public class ComplexStructureController {
    private ComplexStructureRepository complexStructureRepository;
    private SubStructureRepository subStructureRepository;
    private SubStructureService subStructureService;
    @GetMapping("/main")
    public String main(Model model, @RequestParam(name = "page", defaultValue = "0") int p, @RequestParam(name = "size", defaultValue = "10") int s, @RequestParam(name = "keyword", defaultValue = "") String kw) {
        Page<ComplexStructure> userPage = complexStructureRepository.findByAnyColumnContaining(kw.toLowerCase(), PageRequest.of(p, s));
        model.addAttribute("pageTitle", "Format à multi structures");
        model.addAttribute("listStructures", userPage.getContent());
        model.addAttribute("pages", new int[userPage.getTotalPages()]);
        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("pageSize", s);
        model.addAttribute("keyword", kw);
        return "complexstructure/index.html";
    }

    @GetMapping("/main/create")
    public String mainCreate(Model model) {
        model.addAttribute("pageTitle", "Create Structure");
        return "complexstructure/create.html";
    }

    @GetMapping("/main/edit")
    public String mainEdit(Model model, @RequestParam(name = "id") long id) {
        ComplexStructure complexStructure = complexStructureRepository.findById(id).orElse(null);
        List<SubStructure> complexsubStructures = complexStructure.getSubStructures();
        List<SubStructure> subStructures = subStructureRepository.findAll();
        model.addAttribute("complexStructure", complexStructure);
        model.addAttribute("complexsubStructures", complexsubStructures);
        model.addAttribute("subStructures", subStructures);
        return "complexstructure/edit.html";
    }

    @GetMapping("/main/delete")
    public String mainDelete(@RequestParam(name = "id") long id,
                             @RequestParam(name = "keyword", defaultValue = "") String keyword,
                             @RequestParam(name = "page", defaultValue = "0") int page) {

        complexStructureRepository.deleteById(id);
        return "redirect:/complex-structures/main?page=" + page + "&keyword=" + keyword;
    }

    /*

           _____       _        _____ _                   _
          / ____|     | |      / ____| |                 | |
         | (___  _   _| |__   | (___ | |_ _ __ _   _  ___| |_ _   _ _ __ ___
          \___ \| | | | '_ \   \___ \| __| '__| | | |/ __| __| | | | '__/ _ \
          ____) | |_| | |_) |  ____) | |_| |  | |_| | (__| |_| |_| | | |  __/
         |_____/ \__,_|_.__/  |_____/ \__|_|   \__,_|\___|\__|\__,_|_|  \___|


    */
 /*   @GetMapping("/sub")
    public String sub(Model model, @RequestParam(name = "page", defaultValue = "0") int p, @RequestParam(name = "size", defaultValue = "10") int s, @RequestParam(name = "keyword", defaultValue = "") String kw){
        Page<SubStructure> userPage = subStructureRepository.findByAnyColumnContaining(kw.toLowerCase(), PageRequest.of(p, s));
        model.addAttribute("pageTitle", "Sous Structure");
        model.addAttribute("listStructures", userPage.getContent());
        model.addAttribute("pages", new int[userPage.getTotalPages()]);
        model.addAttribute("currentPage", p);
        model.addAttribute("keyword", kw);
        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("pageSize", s);
        return "substructure/index.html";
    }*/
    @GetMapping("/sub")
    public String sub(Model model,
                      @RequestParam(name = "page", defaultValue = "0") int p,
                      @RequestParam(name = "size", defaultValue = "10") int s,
                      @RequestParam(name = "keyword", defaultValue = "") String kw) {
        Page<SubStructure> userPage = subStructureRepository.findByAnyColumnContaining(kw.toLowerCase(), PageRequest.of(p, s));
        model.addAttribute("pageTitle", "Structures");
        model.addAttribute("listStructures", userPage.getContent());
        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("pageSize", s);
        model.addAttribute("keyword", kw);
        return "substructure/index.html";
    }

    @GetMapping("/sub/create")
    public String subCreate(Model model) {
        model.addAttribute("pageTitle", "Nouvelle Structure");
        return "substructure/create.html";
    }

    @GetMapping("/sub/edit")
    public String subEdit(Model model, @RequestParam(name = "id") long id) {
        SubStructure subStructure = subStructureRepository.findById(id).orElse(null);
        model.addAttribute("pageTitle", "Edit Structure");
        model.addAttribute("substructure", subStructure);
        List<SubStructureDetail> subStructureDetails = subStructure.getSubStructureDetails();
        model.addAttribute("substructuredetail", subStructureDetails);
        return "substructure/edit.html";
    }

    @GetMapping("/sub/delete")
    public String subDelete(@RequestParam(name = "id") long id,
                            @RequestParam(name = "keyword", defaultValue = "") String keyword,
                            @RequestParam(name = "page", defaultValue = "0") int page) {
        subStructureService.deleteSubStructure(id);
        //subStructureRepository.deleteById(id);
        return "redirect:/complex-structures/sub?page=" + page + "&keyword=" + keyword;
    }
}
