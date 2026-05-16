package ma.ac2i.converter.converter.web;

import lombok.AllArgsConstructor;
import ma.ac2i.converter.converter.entities.AppUser;
import ma.ac2i.converter.converter.repository.AppUserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@AllArgsConstructor
public class UserControllerAdvice {

    private AppUserRepository appUserRepository;

    @ModelAttribute
    public void addUserInfoToModel(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
            AppUser appUser = appUserRepository.findByUsername(userDetails.getUsername());
            model.addAttribute("currentappuser", appUser);
        }
    }
}
