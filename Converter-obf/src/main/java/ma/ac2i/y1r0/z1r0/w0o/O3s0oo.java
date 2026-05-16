package ma.ac2i.y1r0.z1r0.w0o;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import ma.ac2i.y1r0.z1r0.e0o.Licence;
import ma.ac2i.y1r0.z1r0.r0o.O3w0oo;
import ma.ac2i.y1r0.z1r0.s0o.O3r0oo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.net.InetAddress;
import java.time.LocalDate;

@Controller
@RequestMapping("/licence")
@RequiredArgsConstructor
public class O3s0oo {

    private final O3w0oo l0x52$;
    private final O3r0oo l0x53$;

    @GetMapping
    public String s0x4Cx46$(Model m0x108, HttpServletRequest r0x116) {
        Licence l0x101$ = l0x52$.findTopByOrderByIdDesc()
                .orElse(new Licence());
 
        if (l0x101$.getHostname() == null || l0x101$.getHostname().isEmpty()) {
            try {
                l0x101$.setHostname(InetAddress.getLocalHost().getHostName());
            } catch (Exception e) {
                l0x101$.setHostname("serveur-local");
            }
        }
 
        String e0x114 = r0x116.getParameter("error");
        if (e0x114 != null) {
            switch (e0x114) {
                case "no_licence":
                    m0x108.addAttribute("errorMessage", "Aucune licence configurée");
                    break;
                case "invalid_hostname":
                    m0x108.addAttribute("errorMessage", "Licence non valide pour ce serveur");
                    break;
                case "expired":
                    m0x108.addAttribute("errorMessage", "Licence expirée");
                    break;
                case "module_not_authorized":
                    m0x108.addAttribute("errorMessage", "Module non autorisé");
                    break;
            }
        }

        m0x108.addAttribute("licence", l0x101$);
        return "licence/index";
    }

    @PostMapping
    public String s0x4C$(@RequestParam("licencekey") String l0x4B,
                              Model m0x108,
                              HttpServletRequest r0x116) {
        O3r0oo.L0x44$ l0x44 = l0x53$.d0x4C$(l0x4B);

        if (l0x44 == null) {
            m0x108.addAttribute("errorMessage", "Clé de licence invalide ou corrompue");
            return p0x4Dx4C$(m0x108);
        }
 
        String h0x53;
        try {
            h0x53 = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            h0x53 = "unknown";
        }

        if (!l0x44.getHostname().equalsIgnoreCase(h0x53)) {
            m0x108.addAttribute("errorMessage",
                    "Licence non valide pour ce serveur. Hostname attendu: " +
                            l0x44.getHostname() + ", trouvé: " + h0x53);
            return p0x4Dx4C$(m0x108);
        }
 
        if (l0x44.getExpirationDate().isBefore(LocalDate.now())) {
            m0x108.addAttribute("errorMessage", "Licence expirée depuis le: " + l0x44.getExpirationDate());
            return p0x4Dx4C$(m0x108);
        }

        if (!l0x44.isMOD_BASE()) {  
            m0x108.addAttribute("errorMessage", "Licence active mais aucun module activé.");
            return p0x4Dx4C$(m0x108);
        }

        Licence l0x101$ = new Licence(); 
        l0x101$.setCleLicence(l0x4B); 
        l0x52$.save(l0x101$);

        return "redirect:/?success=Licence activée avec succès jusqu'au " + l0x44.getExpirationDate();
    }

    private String p0x4Dx4C$(Model m0x108) {
        Licence l0x101$ = l0x52$.findTopByOrderByIdDesc().orElse(new Licence());

        if (l0x101$.getHostname() == null) {
            try {
                l0x101$.setHostname(InetAddress.getLocalHost().getHostName());
            } catch (Exception e) {
                l0x101$.setHostname("serveur-local");
            }
        }

        m0x108.addAttribute("licence", l0x101$);
        return "licence/index";
    }
}