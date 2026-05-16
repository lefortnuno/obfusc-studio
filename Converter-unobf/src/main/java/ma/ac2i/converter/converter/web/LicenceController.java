package ma.ac2i.converter.converter.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import ma.ac2i.converter.converter.entities.Licence;
import ma.ac2i.converter.converter.repository.LicenceRepository;
import ma.ac2i.converter.converter.service.LicenceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.net.InetAddress;
import java.time.LocalDate;

@Controller
@RequestMapping("/licence")
@RequiredArgsConstructor
public class LicenceController {

    private final LicenceRepository licenceRepository;
    private final LicenceService licenceService;

    @GetMapping
    public String showLicenceForm(Model model, HttpServletRequest request) {
        Licence licence = licenceRepository.findTopByOrderByIdDesc()
                .orElse(new Licence()); 

        // Pré-remplir le hostname si vide 
        if (licence.getHostname() == null || licence.getHostname().isEmpty()) {
            try {
                licence.setHostname(InetAddress.getLocalHost().getHostName());
            } catch (Exception e) {
                licence.setHostname("serveur-local");
            }
        }

        // Gérer les messages d'erreur
        String error = request.getParameter("error");
        if (error != null) {
            switch (error) {
                case "no_licence":
                    model.addAttribute("errorMessage", "Aucune licence configurée");
                    break;
                case "invalid_hostname":
                    model.addAttribute("errorMessage", "Licence non valide pour ce serveur");
                    break;
                case "expired":
                    model.addAttribute("errorMessage", "Licence expirée");
                    break;
                case "module_not_authorized":
                    model.addAttribute("errorMessage", "Module non autorisé");
                    break;
            }
        }

        model.addAttribute("licence", licence);
        return "licence/index";
    }

    @PostMapping
    public String saveLicence(@RequestParam("licencekey") String licenceKey,
                              Model model,
                              HttpServletRequest request) { 

        // Décrypter la licence
        LicenceService.LicenceDecryptee licenceDecryptee = licenceService.decrypterLicence(licenceKey);

        if (licenceDecryptee == null) {
            model.addAttribute("errorMessage", "Clé de licence invalide ou corrompue");
            return preparerModelLicence(model);
        }

        // Vérifier le hostname
        String hostnameSysteme;
        try {
            hostnameSysteme = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            hostnameSysteme = "unknown";
        }

        if (!licenceDecryptee.getHostname().equalsIgnoreCase(hostnameSysteme)) {
            model.addAttribute("errorMessage",
                    "Licence non valide pour ce serveur. Hostname attendu: " +
                            licenceDecryptee.getHostname() + ", trouvé: " + hostnameSysteme);
            return preparerModelLicence(model);
        }

        // Vérifier la date d'expiration
        if (licenceDecryptee.getExpirationDate().isBefore(LocalDate.now())) {
            model.addAttribute("errorMessage", "Licence expirée depuis le: " + licenceDecryptee.getExpirationDate());
            return preparerModelLicence(model);
        }
        
        if (!licenceDecryptee.isMOD_BASE()){
            model.addAttribute("errorMessage", "Licence active mais aucun module activé.");
            return preparerModelLicence(model);
        }

        // Sauvegarder la licence
        Licence licence = new Licence();  
        licence.setCleLicence(licenceKey);    
        licenceRepository.save(licence);

        return "redirect:/?success=Licence activée avec succès jusqu'au " + licenceDecryptee.getExpirationDate();
    }

    private String preparerModelLicence(Model model) {
        Licence licence = licenceRepository.findTopByOrderByIdDesc().orElse(new Licence());
 
        if (licence.getHostname() == null) {
            try {
                licence.setHostname(InetAddress.getLocalHost().getHostName());
            } catch (Exception e) {
                licence.setHostname("serveur-local");
            }
        }

        model.addAttribute("licence", licence);
        return "licence/index";
    }
}