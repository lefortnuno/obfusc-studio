package ma.ac2i.converter.converter.web;

import lombok.AllArgsConstructor;
import ma.ac2i.converter.converter.entities.AppUser;
import ma.ac2i.converter.converter.entities.Structure;
import ma.ac2i.converter.converter.entities.StructureDetail;
import ma.ac2i.converter.converter.repository.StructureDetailRepository;
import ma.ac2i.converter.converter.repository.StructureRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/structurs")
public class StructureController {
    private StructureRepository structureRepository;
    private StructureDetailRepository structureDetailRepository;
    
    @GetMapping("")
    public String index(Model model, @RequestParam(name = "page", defaultValue = "0") int p, @RequestParam(name = "size", defaultValue = "10") int s, @RequestParam(name = "keyword", defaultValue = "") String kw) {
        Page<Structure> userPage = structureRepository.findByAnyColumnContaining(kw.toLowerCase(), PageRequest.of(p, s));
        model.addAttribute("pageTitle", "Format à mono structure");
        model.addAttribute("listStructures", userPage.getContent());
        model.addAttribute("pages", new int[userPage.getTotalPages()]);
        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("pageSize", s);
        model.addAttribute("keyword", kw);
        return "structures/index";
    }
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("pageTitle", "Nouveau format à mono structure");
        return "structures/create";
    }

    @GetMapping("/edit")
    public String edit(Model model, @RequestParam(name = "id") long id){
        model.addAttribute("pageTitle", "Edit format à mono structure");
        Structure structure = structureRepository.findById(id).orElse(null);
        model.addAttribute("toto", structure);
        List<StructureDetail> structureDetail = structure.getStructureDetails();
        model.addAttribute("structureDetail", structureDetail);
        return "structures/edit";
    }

    @GetMapping("/delete")
    public String delete(@RequestParam(name = "id") long id,
                         @RequestParam(name = "keyword",defaultValue = "") String keyword,
                         @RequestParam(name = "page",defaultValue = "0") int page){

        structureRepository.deleteById(id);
        return "redirect:/structurs?page="+page+"&keyword="+keyword;
    }

}
