package ma.ac2i.y1r0.z1r0.s0o;
import ma.ac2i.y1r0.z1r0.m0o.R04o;
import ma.ac2i.y1r0.z1r0.s0o.O3r0oo;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.time.LocalDate;
import org.springframework.dao.DataIntegrityViolationException;

        // try {
        //     Thread.sleep(2000); // 2000 millisecondes = 2 secondes
        // } catch (InterruptedException e) {
        //     Thread.currentThread().interrupt(); // Réinterrompre le thread
        //     // Gérer l'interruption selon votre besoin
        // }  
        
public class R04oo { 
    public static String d0x116_(String e0x100) {
        byte[] a0x97 = Base64.getDecoder().decode(e0x100);
        String k0x531 = R04o.d0x116_("AAAAAAAAAAAAAAAAAAAAAAAAAAA="); 
        // System.err.println("[-----] : " + k0x531); 
        byte[] k0x121 = "A0x43x32x49$cwBJAQ==".getBytes(StandardCharsets.UTF_8);
        // byte[] k0x121 = k0x531.getBytes(StandardCharsets.UTF_8);
        byte[] r0x116 = new byte[a0x97.length];

        for (int i = 0; i < a0x97.length; i++) {
            r0x116[i] = (byte) (a0x97[i] ^ k0x121[i % k0x121.length]);  
        }

        return new String(r0x116, StandardCharsets.UTF_8);
    }
    
    public static boolean d0x1161_(O3r0oo.L0x44$ d0x100) { 
        String h0x101 = g0x53x48$();
        if (!d0x100.getHostname().equalsIgnoreCase(h0x101)) return false;
        if (d0x100.getExpirationDate().isBefore(LocalDate.now())) return false;
        if (!d0x100.isMOD_BASE()) return false; 
        return true;
    }
    
    private static String g0x53x48$() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    public static String e0x4D$(Exception e) {
        if (e instanceof DataIntegrityViolationException) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("unique index") || msg.contains("unique constraint") || msg.contains("23505")) {
                return "Ce nom est déjà utilisé. Veuillez en choisir un autre.";
            }
            if (msg.contains("not-null") || msg.contains("null value")) {
                return "Un champ obligatoire est manquant.";
            }
            if (msg.contains("foreign key") || msg.contains("referential integrity")) {
                return "Impossible de supprimer : cet élément est utilisé par d'autres données.";
            }
            return "Erreur de données : contrainte de base de données violée.";
        }
        if (e instanceof RuntimeException && e.getMessage() != null
                && !e.getMessage().toLowerCase().contains("hibernate")
                && !e.getMessage().toLowerCase().contains("jdbc")
                && !e.getMessage().toLowerCase().contains("sql")) {
            return e.getMessage();
        } 
        String errorCode = Long.toHexString(System.currentTimeMillis()).toUpperCase();
        System.err.println("[ERROR-" + errorCode + "] " + e.getClass().getName() + " - " + e.getMessage());
        return "Une erreur inattendue est survenue (réf. " + errorCode + "). Veuillez consulter les logs ou contacter l'administrateur.";

    }

}
        // try {
        //     Thread.sleep(2000); // 2000 millisecondes = 2 secondes
        // } catch (InterruptedException e) {
        //     Thread.currentThread().interrupt(); // Réinterrompre le thread
        //     // Gérer l'interruption selon votre besoin
        // }  

