package ma.ac2i.converter.converter.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ma.ac2i.converter.converter.entities.Licence;
import ma.ac2i.converter.converter.repository.LicenceRepository;
import ma.ac2i.converter.converter.service.LicenceService; 
import ma.ac2i.converter.converter.service.R04oo;

@Service
@RequiredArgsConstructor
public class ConveterService {

    private final LicenceService licenceService;
    private final LicenceRepository licenceRepository;

    @Value("${user.dir}")
    private String appDirectory;

    @Value("${app.max-concurrent-conversions:2}")
    private int maxConcurrentConversions;

    private Semaphore conversionSemaphore;

    @PostConstruct
    public void init() {
        this.conversionSemaphore = new Semaphore(maxConcurrentConversions);
    }

    private File extractResourceToTemp(String resourcePath) {
        try { 
            String os = System.getProperty("os.name").toLowerCase();
            boolean isW = os.contains("win");
            String f0x4E = resourcePath.substring(resourcePath.lastIndexOf("/") + 1); 
            File outDir = new File(System.getProperty("java.io.tmpdir"), f0x4E); 
            
            if (!outDir.exists()) {
                try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath);
                     FileOutputStream out = new FileOutputStream(outDir)) {

                    if (in == null) {
                        throw new RuntimeException("Resource introuvable dans le JAR: " + resourcePath);
                    }

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                if (!isW) {
                    outDir.setExecutable(true);
                } 
                outDir.deleteOnExit();
            } 
            return outDir;

        } catch (Exception e) {
            throw new RuntimeException("Erreur extraction resource: " + resourcePath + ", " + e.getMessage());
        }
    }


    private void verifierLicence() { 
        Optional<Licence> licenceOpt = licenceRepository.findTopByOrderByIdDesc();
        if (licenceOpt.isEmpty()) {
            throw new RuntimeException("Aucune licence trouvée.");
        }

        Licence licence = licenceOpt.get();
        LicenceService.LicenceDecryptee decrypted = licenceService.decrypterLicence(licence.getCleLicence());
        if (decrypted == null) throw new RuntimeException("Clé licence invalide.");  
        if (!R04oo.d0x1161_(decrypted)) throw new RuntimeException("Licence invalide.");
    }

    /**
     * Transformation – ne peut être exécutée que si licence valide
     */
    public boolean transformation(String xsltFilePath, String inputFilePath, String outputFilePath,
                               String from, String into, String delimiter) {

        verifierLicence(); // sécurité

        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("win"); 

        if (delimiter.equals("|") && from.equals("csv")) {
            delimiter = "\\" + delimiter;
        }

        try {
            File myObj = new File(outputFilePath);
            myObj.createNewFile();
        } catch (Exception e) {
            System.out.println("Erreur création fichier sortie : " + e.getMessage());
        }

        xsltFilePath = xsltFilePath.replace("\\", "/");
        inputFilePath = inputFilePath.replace("\\", "/");
        outputFilePath = outputFilePath.replace("\\", "/");
        String basedir = appDirectory.replace("\\", "/") + "/";
        File saxonJar_JnJar = extractResourceToTemp("saxon_he_11_6.jar");
        File resolverJar_InJar = extractResourceToTemp("xmlresolver_6_0_19.jar"); 

        String classpath;
        if (!isWindows) { 
            classpath = saxonJar_JnJar.getAbsolutePath() + ":" + resolverJar_InJar.getAbsolutePath(); 
        }
        else {  
            classpath = saxonJar_JnJar.getAbsolutePath() + ";" + resolverJar_InJar.getAbsolutePath(); 
        }  

        List<String> command = Arrays.asList("java", "-Xmx1g", "-cp", classpath, "net.sf.saxon.Transform", "-xsl:" + xsltFilePath, "-s:" + inputFilePath, "-o:" + outputFilePath);

        if (from.equals("xml") && into.equals("csv")) {
            command = Arrays.asList("java", "-Xmx1g", "-cp", classpath, "net.sf.saxon.Transform",
                    "-xsl:" + xsltFilePath, "-o:" + outputFilePath,
                    "-s:" + inputFilePath, "delimiter=" + delimiter);
        } else if (from.equals("csv") || into.equals("csv")) {
            command = Arrays.asList("java", "-Xmx1g", "-cp", classpath, "net.sf.saxon.Transform",
                    "-xsl:" + xsltFilePath, "-o:" + outputFilePath,
                    "-it:main", "text-input=file:" + inputFilePath, "delimiter=" + delimiter);
        } else if (from.equals("txt")) {
            command = Arrays.asList("java", "-Xmx1g", "-cp", classpath, "net.sf.saxon.Transform",
                    "-xsl:" + xsltFilePath, "-o:" + outputFilePath,
                    "-it:main", "text-input=file:" + inputFilePath);
        }  

        if (!conversionSemaphore.tryAcquire()) {
            throw new RuntimeException("Nombre maximum de conversions simultanées atteint. Veuillez réessayer dans quelques instants.");
        }
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(appDirectory));
            processBuilder.redirectOutput(new File("xslt_output.log"));
            processBuilder.redirectError(new File("xslt_error.log"));

            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("XSLT transformation failed with exit code: " + exitCode);
                return false;
            }

            if (saxonJar_JnJar != null && saxonJar_JnJar.exists()) saxonJar_JnJar.delete();
            if (resolverJar_InJar != null && resolverJar_InJar.exists()) resolverJar_InJar.delete();
            return true;

        } catch (Exception e) {
            System.out.println("Erreur exécution transformation : " + e.getMessage());
            return false;
        } finally {
            conversionSemaphore.release();
        }
    }

    /**
     * Génère seulement la commande — toujours bloqué si licence invalide.
     */
    public String transformationCommand(String xsltFilePath, String from, String into, String delimiter) {
        verifierLicence(); // sécurité  

        if (delimiter.equals("|") && from.equals("csv")) {
            delimiter = "\\" + delimiter;
        }

        xsltFilePath = xsltFilePath.replace("\\", "/");
        String input = "<chemin vers le fichier d'entrée>"; 
        String output = "<chemin vers le fichier de sortie>";
        String basedir = appDirectory.replace("\\", "/") + "/";
        File c0x4Ax5Fx49x4A$ = new File("converter-0.0.1-SNAPSHOT.jar");
        String c0x4Ax5Fx49x4A = "java -jar " + c0x4Ax5Fx49x4A$.getAbsolutePath();

        String classpath;

        List<String> command = Arrays.asList(c0x4Ax5Fx49x4A, "-c" , xsltFilePath, output, input );

        if (from.equals("xml") && into.equals("csv")) {
            command = Arrays.asList(c0x4Ax5Fx49x4A, "-c" , xsltFilePath, output, input, delimiter);
        } else if (from.equals("csv") || into.equals("csv")) {
            command = Arrays.asList(c0x4Ax5Fx49x4A, "-c" , xsltFilePath, output, input, delimiter);
        } else if (from.equals("txt")) {
            command = Arrays.asList(c0x4Ax5Fx49x4A, "-c" , xsltFilePath, output, input);
        }
        return String.join(" ", command);
    }

    private String getServerHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
