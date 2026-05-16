package ma.ac2i.converter.converter.middleware;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ma.ac2i.converter.converter.entities.Licence;
import ma.ac2i.converter.converter.repository.LicenceRepository;
import ma.ac2i.converter.converter.service.LicenceService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor; 
import org.springframework.web.servlet.ModelAndView; 
import ma.ac2i.converter.converter.service.R04oo;


import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LicenceMiddleware implements HandlerInterceptor {

    private final LicenceRepository licenceRepository;
    private final LicenceService licenceService;   
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/assets/")
                || requestURI.equals("/login")
                || requestURI.equals("/logout")
                || requestURI.equals("/licence")
                || requestURI.startsWith("/error")
                || requestURI.startsWith("/static/")
                || requestURI.equals("/favicon.ico")) {
            return true;
        }

        // -------------------------
        // 🔥 CONTROL DE LA BDD ICI (A COMMENTER)
        // -------------------------
        List<Licence> licences = licenceRepository.findAll();
        System.out.println("=== TOUTES LES LICENCES EN BASE ===");
         
        if (licences.isEmpty()) {
            System.out.println("Aucune licence trouvée");
        } else {
            for (Licence l : licences) {
                System.out.println("ID: " + l.getId() + 
                                    " | Hostname: " + l.getHostname() + 
                                    " | Exp: " + l.getExpDate() + 
                                    " | Mod_base: " + l.getMod_base() +
                                    " | Clé: " + (l.getCleLicence() != null ? l.getCleLicence() : "null")) ;
            }
        }
        System.out.println("Total: " + licences.size() + " licences");

        long count = licenceRepository.count();
        System.out.println("Il y a " + count + " licences...");
        if (count > 0) {
            System.out.println("Suppression de " + count + " licences...");
            // licenceRepository.deleteAll();
            // licenceRepository.deleteById(34);
            System.out.println("Base de données vidée !");
        }
        // --------------------------------
                
        Optional<Licence> licenceOpt = licenceRepository.findTopByOrderByIdDesc();

        if (licenceOpt.isEmpty()) {
            response.sendRedirect("/licence?error=no_licence");
            return false;
        }

        Licence licence = licenceOpt.get();

        // -------------------------
        // 🔥 DÉCRYPTAGE ICI (NOUVEAU)
        // -------------------------
        LicenceService.LicenceDecryptee decrypted = licenceService.decrypterLicence(licence.getCleLicence());

        if (decrypted == null) {
            response.sendRedirect("/licence?error=invalid_key");
            return false;
        } 

        // ---------------------------
        // 🔥 VÉRIFICATION Licence (décrypté) 
        if (!R04oo.d0x1161_(decrypted)) {
            System.out.println("Licence invalide.");
            response.sendRedirect("/licence?error=invalid_key");
            return false;
        }

        // ---------------------------
        // 🔥 VÉRIFICATION MODULES (décryptés)
        // ---------------------------
        if (requestURI.matches(".*(/|/users|/download|/upload|/structurs|/complex-structures/main|/complex-structures/sub).*")) { 
            if (!decrypted.isMOD_BASE()) { 
                response.sendRedirect("/licence?error=invalid_key");
                return false;
            }
        }

        return true;
    }

    private String getServerHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    @Override
    public void postHandle(HttpServletRequest r0x116, HttpServletResponse r0x101$, Object h0x114, ModelAndView modelAndView) throws Exception {
        // ac2i
    }

    @Override
    public void afterCompletion(HttpServletRequest r0x116, HttpServletResponse r0x101$, Object h0x114, Exception ex) throws Exception {
        // ac2i
    }
}
