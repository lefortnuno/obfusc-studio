package ma.ac2i.y1r0.z1r0.w0o;

import lombok.AllArgsConstructor;
import ma.ac2i.y1r0.z1r0.e0o.AppUser;
import ma.ac2i.y1r0.z1r0.r0o.O2w0o;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@AllArgsConstructor
public class O07sOo {

    private O2w0o a0x55x52$;

    @ModelAttribute
    public void a0x55x49x54x4D$(@AuthenticationPrincipal UserDetails u0x44, Model m0x108) {
        if (u0x44 != null) {
            m0x108.addAttribute("username", u0x44.getUsername());
            AppUser a0x55 = a0x55x52$.findByUsername(u0x44.getUsername());
            m0x108.addAttribute("currentappuser", a0x55);
        }
    }
}
