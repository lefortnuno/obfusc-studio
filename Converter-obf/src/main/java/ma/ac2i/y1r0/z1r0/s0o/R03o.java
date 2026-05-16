package ma.ac2i.y1r0.z1r0.s0o;
 
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
import ma.ac2i.y1r0.z1r0.e0o.Licence;
import ma.ac2i.y1r0.z1r0.r0o.O3w0oo;
import ma.ac2i.y1r0.z1r0.s0o.R04oo;
import ma.ac2i.y1r0.z1r0.s0o.O3r0oo;  

@Service
@RequiredArgsConstructor
public class R03o {
 
    private final O3r0oo l0x53$; 
    private final O3w0oo l0x52$; 

    @Value("${user.dir}")
    private String a0x44$;

    @Value("${app.max-concurrent-conversions:2}")
    private int m0x4Ax43x43$;

    private Semaphore s0x53x4D$;

    @PostConstruct
    public void i0x49x4Ex49x54$() {
        this.s0x53x4D$ = new Semaphore(m0x4Ax43x43$);
    }

    private File e0x52x54x54$(String r0x50$) {
        try { 
            String os = System.getProperty("os.name").toLowerCase();
            boolean isW = os.contains("win");
            String f0x4E = r0x50$.substring(r0x50$.lastIndexOf("/") + 1); 
            File o0x46 = new File(System.getProperty("java.io.tmpdir"), f0x4E); 

            if (!o0x46.exists()) {
                try (InputStream in = getClass().getClassLoader().getResourceAsStream(r0x50$);
                     FileOutputStream out = new FileOutputStream(o0x46)) {

                    if (in == null) {
                        throw new RuntimeException("Resource introuvable dans le JAR: " + r0x50$);
                    }

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                if (!isW) {
                    o0x46.setExecutable(true);
                } 
                o0x46.deleteOnExit();
            } 
            return o0x46;

        } catch (Exception e) {
            throw new RuntimeException("Erreur extraction resource: " + r0x50$ + ", " + e.getMessage());
        }
    }
 
    private void v0x45$() {  
        Optional<Licence> l0x4F = l0x52$.findTopByOrderByIdDesc();
        if (l0x4F.isEmpty()) {
            throw new RuntimeException(R04oo.d0x116_("AEUbQV0dE14RV1xKABJiPjM+SEuCs7qdVlY="));
        }

        Licence l0x101 = l0x4F.get();
        O3r0oo.L0x44$ d0x100 = l0x53$.d0x4C$(l0x101.getCleLicence());
        if (d0x100 == null) throw new RuntimeException(R04oo.d0x116_("DVkbUV0bVhIRWk9FDx4mL28="));  
        if (!R04oo.d0x1161_(d0x100)) throw new RuntimeException(R04oo.d0x116_("DVkbUV0bVhIRWk9FDx4mL28="));    
    }

    public boolean t0x110$(String x0x46x50, String i0x46x50, String o0x46x50, String f0x109, String i0x111, String d0x114) {
        v0x45$();

        String os = System.getProperty("os.name").toLowerCase();
        boolean isW = os.contains("win"); 

        if (d0x114.equals("|") && f0x109.equals("csv")) {
            d0x114 = "\\" + d0x114;
        }

        try {
            File m0x4F = new File(o0x46x50);
            m0x4F.createNewFile();
        } catch (Exception e) {
            System.out.println(e);
        }

        x0x46x50 = x0x46x50.replace("\\", "/");
        i0x46x50 = i0x46x50.replace("\\", "/");
        o0x46x50 = o0x46x50.replace("\\", "/");
        String b0x114 = a0x44$.replace("\\", "/") + "/";
        File s0x4Ax5Fx4Ax4A = e0x52x54x54$("sdIl8WQlYKQFsXWg==.jar");
        File r0x4Ax5Fx49x4A = e0x52x54x54$("xkYRW1cEWQ=.jar");
  
        String c0x104; 
        if (!isW) { 
            c0x104 = s0x4Ax5Fx4Ax4A.getAbsolutePath() + ":" + r0x4Ax5Fx49x4A.getAbsolutePath(); 
        }
        else {  
            c0x104 = s0x4Ax5Fx4Ax4A.getAbsolutePath() + ";" + r0x4Ax5Fx49x4A.getAbsolutePath(); 
        } 
    
        List<String> c0x100 = Arrays.asList("java", "-Xmx1g", "-cp", c0x104, "net.sf.saxon.Transform",
                "-xsl:" + x0x46x50, "-s:" + i0x46x50, "-o:" + o0x46x50);

        if (f0x109.equals("xml") && i0x111.equals("csv")) {
            c0x100 = Arrays.asList("java", "-Xmx1g", "-cp", c0x104, "net.sf.saxon.Transform",
                    "-xsl:" + x0x46x50, "-o:" + o0x46x50, "-s:" + i0x46x50, "delimiter=" + d0x114);
        } else if (f0x109.equals("csv") || i0x111.equals("csv")) {
            c0x100 = Arrays.asList("java", "-Xmx1g", "-cp", c0x104, "net.sf.saxon.Transform",
                    "-xsl:" + x0x46x50, "-o:" + o0x46x50,
                    "-it:main", "text-input=file:" + i0x46x50, "delimiter=" + d0x114);
        } else if (f0x109.equals("txt")) {
            c0x100 = Arrays.asList("java", "-Xmx1g", "-cp", c0x104, "net.sf.saxon.Transform",
                    "-xsl:" + x0x46x50, "-o:" + o0x46x50,
                    "-it:main", "text-input=file:" + i0x46x50);
        }   

        if (!s0x53x4D$.tryAcquire()) {
            throw new RuntimeException("Nombre maximum de conversions simultanées atteint. Veuillez réessayer dans quelques instants.");
        }
        try {
            ProcessBuilder p0x42 = new ProcessBuilder(c0x100);
            p0x42.directory(new File(a0x44$));
            File o0x46 = new File("xslt_output.log");
            File e0x46 = new File("xslt_error.log");
            p0x42.redirectOutput(o0x46);
            p0x42.redirectError(e0x46);
            Process p0x115 = p0x42.start();
            int e0x43 = p0x115.waitFor();

            if (e0x43 != 0) {
                System.out.println("XSLT transformation failed with exit code: " + e0x43);
                return false;
            }

            if (s0x4Ax5Fx4Ax4A != null && s0x4Ax5Fx4Ax4A.exists()) s0x4Ax5Fx4Ax4A.delete();
            if (r0x4Ax5Fx49x4A != null && r0x4Ax5Fx49x4A.exists()) r0x4Ax5Fx49x4A.delete();
            return true;

        } catch (Exception e) {
            System.out.println("Erreur de convertion: " + e);
            return false;
        } finally {
            s0x53x4D$.release();
        }
    }

    public String t0x43$(String x0x46x50, String f0x109, String i0x111, String d0x114) {
        v0x45$();
        // String os = System.getProperty("os.name").toLowerCase();
        // boolean isW = os.contains("win");

        if (d0x114.equals("|") && f0x109.equals("csv")) {
            d0x114 = "\\" + d0x114;
        }

        x0x46x50 = x0x46x50.replace("\\", "/");
        String i0x46x50 = "<chemin vers le fichier d'entrée>";
        String o0x46x50 = "<chemin vers le fichier de sortie>";
        String b0x114 = a0x44$.replace("\\", "/") + "/";
        // File s0x4Ax5Fx4Ax4A = e0x52x54x54$("sdIl8WQlYKQFsXWg==.jar");
        // File r0x4Ax5Fx49x4A = e0x52x54x54$("xkYRW1cEWQ=.jar"); 
        File c0x4Ax5Fx49x4A$ = new File("converter-0.0.1-SNAPSHOT.jar");
        String c0x4Ax5Fx49x4A = "java -jar " + c0x4Ax5Fx49x4A$.getAbsolutePath();

        String c0x104; 
        // if (!isW) { 
        //      c0x104 = s0x4Ax5Fx4Ax4A.getAbsolutePath() + ":" + r0x4Ax5Fx49x4A.getAbsolutePath(); 
        // } 
        // else {
        //     c0x104 = s0x4Ax5Fx4Ax4A.getAbsolutePath() + ";" + r0x4Ax5Fx49x4A.getAbsolutePath();
        // } 
        
        List<String> c0x100 = Arrays.asList(c0x4Ax5Fx49x4A, "-c" , x0x46x50, o0x46x50, i0x46x50 );

        if (f0x109.equals("xml") && i0x111.equals("csv")) {
            c0x100 = Arrays.asList(c0x4Ax5Fx49x4A, "-c", x0x46x50, o0x46x50, i0x46x50, d0x114);
        } else if (f0x109.equals("csv") || i0x111.equals("csv")) {
            c0x100 = Arrays.asList(c0x4Ax5Fx49x4A, "-c", x0x46x50, o0x46x50, i0x46x50, d0x114);
        } else if (f0x109.equals("txt")) {
            c0x100 = Arrays.asList(c0x4Ax5Fx49x4A, "-c", x0x46x50, o0x46x50, i0x46x50);
        } 

        // List<String> c0x100 = Arrays.asList("java", "-cp", c0x104, "net.sf.saxon.Transform", "-xsl:" + x0x46x50, "-s:" + i0x46x50, "-o:" + o0x46x50);
        // if (f0x109.equals("xml") && i0x111.equals("csv")) {
        //     c0x100 = Arrays.asList("java", "-cp", c0x104,
        //             "net.sf.saxon.Transform", "-xsl:" + x0x46x50, "-o:" + o0x46x50,
        //             "-s:" + i0x46x50, "delimiter='" + d0x114 + "'");
        // } else if (f0x109.equals("csv") || i0x111.equals("csv")) {
        //     c0x100 = Arrays.asList("java", "-cp", c0x104,
        //             "net.sf.saxon.Transform", "-xsl:" + x0x46x50, "-o:" + o0x46x50,
        //             "-it:main", "text-input=file:" + i0x46x50, "delimiter='" + d0x114 + "'");
        // } else if (f0x109.equals("txt")) {
        //     c0x100 = Arrays.asList("java", "-cp", c0x104,
        //             "net.sf.saxon.Transform", "-xsl:" + x0x46x50, "-o:" + o0x46x50,
        //             "-it:main", "text-input=file:" + i0x46x50);
        // } 
        return String.join(" ", c0x100);
    }
    
    private String g0x53x48111$() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
