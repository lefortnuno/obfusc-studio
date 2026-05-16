package ma.ac2i.converter.converter;

import ma.ac2i.converter.converter.entities.AppUser;
import ma.ac2i.converter.converter.entities.Licence; 
import ma.ac2i.converter.converter.repository.LicenceRepository;
import ma.ac2i.converter.converter.service.LicenceService;
import ma.ac2i.converter.converter.service.ConveterService;
import ma.ac2i.converter.converter.service.AccountService;
import ma.ac2i.converter.converter.service.UserDetailServiceImp;
import ma.ac2i.converter.converter.service.R04oo; 

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.RequiredArgsConstructor;
import java.io.File;
import java.time.LocalDate; 
import java.util.Optional;
import java.util.List;


@SpringBootApplication 
public class ConverterApplication { 
    
    public static void main(String[] args) {

        File dbFile = new File("Converter.mv.db");
        if (!dbFile.exists()) {
            boolean isServerMode = args.length == 0 || "-d".equals(args[0]);
            System.err.println("\n[AVERTISSEMENT] 'Converter.mv.db' introuvable dans :");
            System.err.println("  " + System.getProperty("user.dir"));
            if (isServerMode) {
                System.err.println("Une nouvelle base vide sera creee ici. Appuyez sur Ctrl+C pour annuler.\n");
                for (int i = 5; i > 0; i--) {
                    System.err.print(i + "... ");
                    System.err.flush();
                    try { Thread.sleep(1000); } catch (InterruptedException e) { System.exit(1); }
                }
                System.err.println("\n");
            } else {
                System.err.println("Lancez l'application depuis le repertoire contenant Converter.mv.db\n");
                System.exit(1);
            }
        }

        if (args.length == 0 || "-d".equals(args[0])) {
            new SpringApplicationBuilder(ConverterApplication.class).run(args);
            return;
        }

        switch (args[0]) {

            case "-h":
                p0x48();
                System.exit(0);
                return; 

            case "-l":
                if (args.length < 2) {
                    System.out.println("Usage: -l <cleLicence>");
                    System.exit(1);
                }
                r0x4Cx43x4Cx49(args[1]);
                return;

            case "-c":
                if (args.length < 4) {
                    System.out.println("Usage: -c <config> <inputFile> <outputFile> [delimiteur]"); 
                    System.exit(1);
                }
                String delimiteur = (args.length == 5) ? args[4] : ";";
                runConversionCLI(args[1], args[2], args[3], delimiteur);
                return;

            default:
                p0x48();
                System.exit(1);
        }
    }

    private static ConfigurableApplicationContext s0x43x43() {
        String dbUrl = "jdbc:h2:file:./Converter;AUTO_SERVER=TRUE;";
        return new SpringApplicationBuilder(ConverterApplication.class)
                .web(WebApplicationType.NONE)
                .profiles("cli")
                .properties(
                        "spring.main.banner-mode=off",
                        "spring.main.lazy-initialization=true",
                        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration",
                        "spring.datasource.url=" + dbUrl
                )
                .run();
    }

    private static void r0x4Cx43x4Cx49(String cle) {
        try (ConfigurableApplicationContext ctx = s0x43x43()) {

            LicenceService l0x53$ = ctx.getBean(LicenceService.class);
            LicenceRepository l0x52 = ctx.getBean(LicenceRepository.class);

            int code = new ConverterApplication().handleLicenceCLI(cle, l0x53$, l0x52);
            System.exit(code);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /*
    *  CLI MODE : CREATE LICENCE 
    */
    private int handleLicenceCLI(String licenceKey, LicenceService licenceService, LicenceRepository licenceRepository ) {
        System.out.println("== Vérification de la licence ==");

        Optional<Licence> licenceOpt = licenceRepository.findTopByOrderByIdDesc();
        if(licenceOpt.isPresent() && licenceOpt.get().getCleLicence().equalsIgnoreCase(licenceKey)){
            System.err.println("[INFO] : Licence existante.");
            return 2;
        }  

        // Décryptage
        LicenceService.LicenceDecryptee licence = licenceService.decrypterLicence(licenceKey);

        if (licence == null) {
            System.err.println("[ERREUR] : Licence invalide.");
            return 1;
        }

        // Vérification hostname système
        String hostnameSysteme;
        try {
            hostnameSysteme = java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            hostnameSysteme = "unknown";
        }

        if (!R04oo.d0x1161_(licence)){ 
            System.err.println("[ERREUR] : Licence invalide."); 
            return 1;
        }  
        Licence l = new Licence();
        l.setCleLicence(licenceKey);
        licenceRepository.save(l);

        System.out.println("[SUCCESS] : Licence activée avec succès.");
        System.out.println("Expiration : " + licence.getExpirationDate());
        return 0;
    }

    /*
    *  CLI MODE : RUN CONVERSION
    */
    private static void runConversionCLI(String configName, String outputFile, String inputFile, String delimiteur) {
        try (ConfigurableApplicationContext ctx = s0x43x43()) { 

            LicenceRepository licenceRepository = ctx.getBean(LicenceRepository.class);
            LicenceService licenceService = ctx.getBean(LicenceService.class);
            ConveterService c0x53$ = ctx.getBean(ConveterService.class);

            /*
            * Vérification de la licence
            */
            
            Optional<Licence> licenceOpt = licenceRepository.findTopByOrderByIdDesc(); 
            if (licenceOpt.isEmpty()) {
                System.out.println("Aucune licence trouvée.");
                System.exit(2);
            }

            LicenceService.LicenceDecryptee decrypted = licenceService.decrypterLicence(licenceOpt.get().getCleLicence());

            if (decrypted == null) {
                System.out.println("Licence invalide.");
                System.exit(1);
            } 

            if ((!R04oo.d0x1161_(decrypted))) {
                System.out.println("Licence invalide.");
                System.exit(1);
            }

            /*
            * Licence validée → exécuter Saxon embarqué
            */
            String base = configName;
            base = base.replace("\\", "/"); 
            int lastSlash = base.lastIndexOf("/");
            if (lastSlash != -1) {
                base = base.substring(lastSlash + 1);
            }

            if (base.endsWith(".xslt")) {
                base = base.substring(0, base.length() - ".xslt".length());
            } 

            String[] parts = base.split("_", 3);
            if (parts.length < 2) {
                throw new RuntimeException("Format configName invalide : " + configName);
            }

            String fromIntoPart = parts[1]; // csvtotxt

            // Séparer sur "to"
            int idx = fromIntoPart.indexOf("to");
            if (idx == -1) {
                throw new RuntimeException("Impossible de séparer 'from' et 'into' dans : " + fromIntoPart);
            }

            String from = fromIntoPart.substring(0, idx);
            String into = fromIntoPart.substring(idx + 2);
            System.out.println(
                "La conversion fichier en cours avec la configuration=" + configName +
                ",\nfichier d'entree=" + inputFile +
                ",\nfichier de sortie=" + outputFile +
                ",\nformat source=" + from +
                ",\nformat cible=" + into +
                ",\ndelimiteur=" + delimiteur + "\n"
            );

            boolean o0x107 = c0x53$.transformation(configName, inputFile, outputFile, from, into, delimiteur);

            if (o0x107) {
                System.out.println("Conversion reussie.");
                System.exit(0);
            } else {
                System.out.println("Echec de la Conversion.");
                System.exit(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    } 

    private static void p0x48() {
        System.out.println(R04oo.d0x116_("YWUMXV8RUkYRW1cEWQ=="));
        System.out.println(R04oo.d0x116_("YRBVUBNYExJYFBkEQ1diamFxHR1hEFgUE1gTElgUGQRDV2JqYXEdHWEQWBQTVQ0SHFFURREFJ2otcVxNMVwRV1IMWl0WFE5BAQ=="));
        System.out.println(R04oo.d0x116_("YRBVWBNEUF4deFBHBhkhL39xHR1hEFgUE1gTElgUGQRDV2JqYXEdHWEQWBQTVQ0SHVpLQQQeMT4zNB1IL1VYWlwNRVcUWFwEDx4hLy8yWA=="));
        System.out.println(R04oo.d0x116_("YRBVVxNEUF0WUlBDXVd+Iy8hSEl/EERbRgxDRwwKGRgHEi4jLG8dHWEQWBQTVQ0SHUxcRxYDJ2o0P1gdIl8WQlYKQFsXWg=="));
        System.out.println(R04oo.d0x116_("YRBVXBNYExJYFBkEQ1diamFxHR1hEFgUE1gTElgUGQRDV2JqYXEdHWEQWBQTVQ0SOV1dQUMWYiZhME1NLVkbVUcRXFxYQ1xG"));
    }

    @Value("${user.dir}")
    private String basedir;

    @Bean
    @Profile("!cli")
    public org.springframework.boot.CommandLineRunner runner( AccountService accountService,UserDetailServiceImp userDetailServiceImp ) {
        return args -> {
            String[] ox72 = {"IFQVXV0=","M18XQA==","AFQVXV04AQJJAQ==","AHQ1fX0=","FGM9Zg==","NEAUW1Ic","JV8PWl8XUlY=","Al8WUnURX1c="};
            System.out.println("not done");
            AppUser appUser = accountService.loadUserByUsername(R04oo.d0x116_(ox72[0])); // admin
            if (appUser ==null){
                accountService.addNewUser(R04oo.d0x116_(ox72[0]), R04oo.d0x116_(ox72[0]), R04oo.d0x116_(ox72[1]), R04oo.d0x116_(ox72[2]), R04oo.d0x116_(ox72[2])); // "admin", "admin", "root", "Admin@2015", "Admin@2015"
                accountService.addNewRole(R04oo.d0x116_(ox72[3])); // ADMIN
                accountService.addNewRole(R04oo.d0x116_(ox72[4])); // USER
                accountService.addRoleToUser(R04oo.d0x116_(ox72[0]),R04oo.d0x116_(ox72[3])); // "admin","ADMIN"
                accountService.addRoleToUser(R04oo.d0x116_(ox72[0]),R04oo.d0x116_(ox72[4])); // "admin","USER"
            }
            UserDetails userDetails = userDetailServiceImp.loadUserByUsername(R04oo.d0x116_(ox72[0])); // admin
            if (userDetails == null){
                System.out.println("nahh not loaded");
            }
            File upload = new File(basedir+File.separator+R04oo.d0x116_(ox72[5]));// upload
            if (!upload.exists()) {
                upload.mkdirs();
            }
            File ConfFile = new File(basedir+File.separator+R04oo.d0x116_(ox72[6])); // download
            if (!ConfFile.exists()) {
                ConfFile.mkdirs();
            }
            File confFile = new File(basedir + File.separator + R04oo.d0x116_(ox72[7])); // ConfFile
            if (!confFile.exists()) {
                confFile.mkdirs();
            }
            System.out.println("done");
        };
    }
}
