
package ma.ac2i.converter.converter.service;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.time.LocalDate;

import ma.ac2i.converter.converter.entities.Licence;
import ma.ac2i.converter.converter.repository.LicenceRepository;
import ma.ac2i.converter.converter.service.LicenceService;
import ma.ac2i.converter.converter.middleware.R04o;
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
    
    public static boolean d0x1161_(LicenceService.LicenceDecryptee d0x100) { 
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

}
        // try {
        //     Thread.sleep(2000); // 2000 millisecondes = 2 secondes
        // } catch (InterruptedException e) {
        //     Thread.currentThread().interrupt(); // Réinterrompre le thread
        //     // Gérer l'interruption selon votre besoin
        // }  

