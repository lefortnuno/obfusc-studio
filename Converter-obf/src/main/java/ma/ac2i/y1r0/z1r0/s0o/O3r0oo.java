package ma.ac2i.y1r0.z1r0.s0o;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;
import ma.ac2i.y1r0.z1r0.s0o.R04oo;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class O3r0oo {
    
    private static final String A0x77 = R04oo.d0x116_("AHUr");
    private static final String T0x78 = R04oo.d0x116_("AHUrG3A6cB0of3p3VicjLiU4U1o=");
    private static final String S0x84_K0x89 = R04oo.d0x116_("IFNKXXMUWlEdWkpBUUdzfw==");
    private static final String I0x86 = R04oo.d0x116_("cAlBDQFIAgdKBAsWWk57cw==");
    private final ObjectMapper o0x4D = new ObjectMapper();
    private static final DateTimeFormatter D0x69_F0x82 = DateTimeFormatter.ofPattern(R04oo.d0x116_("JVRVeX5VSksBTQ=="));

    public L0x44$ d0x4C$(String c0x43) {
        try {
            byte[] e0x44 = Base64.getDecoder().decode(c0x43); 

            String j0x44 = d0x41x45x53$(e0x44); 

            if (j0x44 == null) {
                return null;
            }

            // Configurer ObjectMapper pour ignorer les champs inconnus
            o0x4D.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            L0x44$ l0x101 = o0x4D.readValue(j0x44, L0x44$.class); 

            return l0x101;

        } catch (Exception e) {
            System.err.println("Erreur lors du décryptage: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String d0x41x45x53$(byte[] e0x44) {
        try {
            SecretKeySpec s0x48x53 = new SecretKeySpec(S0x84_K0x89.getBytes(), A0x77);
            IvParameterSpec i0x50x53 = new IvParameterSpec(I0x86.getBytes());

            Cipher c0x114 = Cipher.getInstance(T0x78);
            c0x114.init(Cipher.DECRYPT_MODE, s0x48x53, i0x50x53);

            byte[] d0x44 = c0x114.doFinal(e0x44);
            return new String(d0x44);

        } catch (Exception e) {
            System.err.println("Erreur décryptage AES: " + e.getMessage());
            return null;
        }
    }

    public static class L0x44$ {
        @JsonProperty("HOSTNAME")
        private String hostname;

        @JsonProperty("EXPDATE")
        private String expDate;

        @JsonProperty("MOD_BASE")
        private String modBase;

        // Constructeur par défaut (nécessaire pour Jackson)
        public L0x44$() {}

        // Getters et Setters
        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        // Méthode pour obtenir la date d'expiration parsée
        public LocalDate getExpirationDate() {
            try {
                return LocalDate.parse(expDate, D0x69_F0x82);
            } catch (Exception e) {
                System.err.println("Erreur parsing date: " + expDate);
                return LocalDate.now().minusDays(1); // Date expirée par défaut
            }
        }

        public void setExpDate(String expDate) {
            this.expDate = expDate;
        }

        // Méthode pour obtenir le booléen MOD_BASE
        public boolean isMOD_BASE() {
            return "True".equalsIgnoreCase(modBase) || "true".equalsIgnoreCase(modBase);
        }

        public void setModBase(String modBase) {
            this.modBase = modBase;
        }

        @Override
        public String toString() { 
            String[] ox72 = {"DVkbUV0bVnYdV0tdEwMnLzo=","KV8LQF0ZXldFEw==","bRAdTEM8UkYdCR4=","bRAVW1c6UkEdCR4=","bRAdTEMRQVMMXVZKJxY2L3w=","bRA1e3cncXMrcQQ=","PA=="};
            return  R04oo.d0x116_(ox72[0]) +
                    R04oo.d0x116_(ox72[1]) + hostname + '\'' +
                    R04oo.d0x116_(ox72[2]) + expDate + '\'' +
                    R04oo.d0x116_(ox72[3]) + modBase + '\'' +
                    R04oo.d0x116_(ox72[4]) + getExpirationDate() +
                    R04oo.d0x116_(ox72[5]) + isMOD_BASE() +
                    R04oo.d0x116_(ox72[6]);
        }
    }
}