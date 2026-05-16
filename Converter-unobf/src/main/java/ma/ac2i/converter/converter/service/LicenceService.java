// src/main/java/ma/ac2i/converter/converter/service/LicenceService.java
package ma.ac2i.converter.converter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class LicenceService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY = "ac2i@license2015";
    private static final String IV = "1999201520229999";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public LicenceDecryptee decrypterLicence(String cleCryptee) {
        try {
            byte[] encryptedData = Base64.getDecoder().decode(cleCryptee);

            String jsonDecrypte = decryptAES(encryptedData);  
            if (jsonDecrypte == null) {
                return null;
            }

            // Configurer ObjectMapper pour ignorer les champs inconnus
            objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            LicenceDecryptee licence = objectMapper.readValue(jsonDecrypte, LicenceDecryptee.class); 
            return licence;

        } catch (Exception e) {
            System.err.println("Erreur lors du décryptage: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String decryptAES(byte[] encryptedData) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(IV.getBytes());

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

            byte[] decryptedData = cipher.doFinal(encryptedData);
            return new String(decryptedData);

        } catch (Exception e) {
            System.err.println("Erreur décryptage AES: " + e.getMessage());
            return null;
        }
    }

    public static class LicenceDecryptee {
        @JsonProperty("HOSTNAME")
        private String hostname;

        @JsonProperty("EXPDATE")
        private String expDate;

        @JsonProperty("MOD_BASE")
        private String modBase;

        // Constructeur par défaut (nécessaire pour Jackson)
        public LicenceDecryptee() {}

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
                return LocalDate.parse(expDate, DATE_FORMATTER);
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
            return "LicenceDecryptee{" +
                    "hostname='" + hostname + '\'' +
                    ", expDate='" + expDate + '\'' +
                    ", modBase='" + modBase + '\'' +
                    ", expirationDate=" + getExpirationDate() +
                    ", MOD_BASE=" + isMOD_BASE() +
                    '}';
        }
    }
}