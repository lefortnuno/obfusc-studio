package ma.ac2i.converter.converter.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import ma.ac2i.converter.converter.entities.AppUser;
import ma.ac2i.converter.converter.repository.AppUserRepository;
import ma.ac2i.converter.converter.service.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import ma.ac2i.converter.converter.service.ErrorMessageHelper;

@Controller
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private AppUserRepository appUserRepository;
    private AccountService accountService;

    @GetMapping("")
    public String index(Model model, @RequestParam(name = "page", defaultValue = "0") int p, @RequestParam(name = "size", defaultValue = "10") int s,
                        @RequestParam(name = "keyword", defaultValue = "") String kw,
                        @RequestParam(name = "message", defaultValue = "") String message
    ) {
        Page<AppUser> userPage = appUserRepository.findByAnyColumnContaining(kw.toLowerCase(), PageRequest.of(p, s));
        model.addAttribute("pageTitle", "Utilisateurs");
        model.addAttribute("listUsers", userPage.getContent());
        model.addAttribute("pages", new int[userPage.getTotalPages()]);

        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("pageSize", s);
        model.addAttribute("keyword", kw);
        if (!message.equals("")) {
            model.addAttribute("message", message);
        }
        return "user/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("pageTitle", "Create User");
        model.addAttribute("appUser", new AppUser());
        return "user/create";
    }

    @PostMapping(path = "/store", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String store(@RequestParam MultiValueMap<String, String> paramMap, @Valid AppUser appUser, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Create User");
            model.addAttribute("appUser", appUser);
            return "user/create";
        }
        try {
            accountService.addNewUser(paramMap.get("username").get(0), paramMap.get("firstname").get(0), paramMap.get("lastname").get(0), paramMap.get("password").get(0), paramMap.get("confpassword").get(0));
        } catch (Exception e) {
            System.out.println(e);
            model.addAttribute("pageTitle", "Create User");
            model.addAttribute("appUser", appUser);
            model.addAttribute("exception", ErrorMessageHelper.toUserMessage(e));
            return "user/create";
        }
        String message = "l'utilisateur a été ajouté avec succès";
        redirectAttributes.addAttribute("message", message);
        return "redirect:/users";
    }

    @GetMapping("/edit")
    public String edit(Model model, @RequestParam(name = "id") String id) {
        AppUser appUser = appUserRepository.findById(id).orElse(null);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (appUser.getUsername().equals("admin") && !authentication.getName().equals("admin")) {
            return "redirect:/users";
        }
        model.addAttribute("pageTitle", "Edit User");
        model.addAttribute("appUser", appUser);
        return "user/edit";
    }

    @PostMapping(path = "/update", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String update(@RequestParam MultiValueMap<String, String> paramMap, @Valid AppUser appUser, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasFieldErrors("firstname") || bindingResult.hasFieldErrors("lastname") || bindingResult.hasFieldErrors("username")) {
            model.addAttribute("pageTitle", "Edit User");
            model.addAttribute("appUser", appUser);
            return "user/edit";
        }
        try {
            accountService.updateUser(paramMap.get("userID").get(0), paramMap.get("username").get(0), paramMap.get("firstname").get(0), paramMap.get("lastname").get(0), paramMap.get("password").get(0), paramMap.get("confpassword").get(0));
        } catch (Exception e) {
            System.out.println(e);
            model.addAttribute("pageTitle", "Edit User");
            model.addAttribute("appUser", appUser);
            model.addAttribute("exception", ErrorMessageHelper.toUserMessage(e));
            return "user/edit";
        }

        String message = "l'utilisateur a été édité avec succès";
        redirectAttributes.addAttribute("message", message);
        return "redirect:/users";
    }

    @GetMapping(path = "/delete")
    public String delete(@RequestParam(name = "id") String id, @RequestParam(name = "keyword", defaultValue = "") String keyword, @RequestParam(name = "page", defaultValue = "0") int page, RedirectAttributes redirectAttributes) {
        AppUser appUser = appUserRepository.findById(id).orElse(null);
        if (appUser.getUsername().equals("admin")) {
            return "redirect:/users";
        }
        appUserRepository.deleteById(id);
        String message = "l'utilisateur a été supprimé avec succès";
        redirectAttributes.addAttribute("message", message);
        return "redirect:/users?page=" + page + "&keyword=" + keyword;
    }
}
