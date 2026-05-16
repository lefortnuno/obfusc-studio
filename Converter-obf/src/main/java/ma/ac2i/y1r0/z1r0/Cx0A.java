package ma.ac2i.y1r0.z1r0;

import ma.ac2i.y1r0.z1r0.e0o.AppUser;
import ma.ac2i.y1r0.z1r0.e0o.Licence;
import ma.ac2i.y1r0.z1r0.r0o.O3w0oo;
import ma.ac2i.y1r0.z1r0.s0o.O3r0oo;
import ma.ac2i.y1r0.z1r0.s0o.R03o;
import ma.ac2i.y1r0.z1r0.s0o.Oooo;
import ma.ac2i.y1r0.z1r0.s0o.O07rOo;
import ma.ac2i.y1r0.z1r0.s0o.R04oo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.File;
import java.time.LocalDate;
import java.util.Optional;


@SpringBootApplication
public class Cx0A {

    public static void main(String[] args) {

        File d0x44x42 = new File("Converter.mv.db");
        if (!d0x44x42.exists()) {
            boolean i0x53$ = args.length == 0 || "-d".equals(args[0]);
            System.err.println("\n[AVERTISSEMENT] 'Converter.mv.db' introuvable dans :");
            System.err.println("  " + System.getProperty("user.dir"));
            if (i0x53$) {
                System.err.println("Une nouvelle base vide sera creee ici. Appuyez sur Ctrl+C pour annuler.\n");
                for (int c0x63 = 5; c0x63 > 0; c0x63--) {
                    System.err.print(c0x63 + "... ");
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
            new SpringApplicationBuilder(Cx0A.class).run(args);
            return;
        }

        switch (args[0]) {

            case "-h":
                p0x48();
                System.exit(0);
                return;

            case "-l":
                if (args.length < 2) {
                    System.out.println(R04oo.d0x116_("FEMZU1ZCEx8UFAVHDxIOIyI0U14kDg=="));
                    System.exit(1);
                }
                r0x4Cx43x4Cx49(args[1]);
                return;

            case "-c":
                if (args.length < 4) {
                    System.out.println(R04oo.d0x116_("FEMZU1ZCEx8bFAVHDBkkIyZvHQEoXghBRz5aXh0KGRgMAjY6NCV7VC1VRhQPHFZeEVlQUAYCMHQ="));
                    System.exit(1);
                }
                String delimiter = (args.length == 5) ? args[4] : ";";
                r0x43x43x4Cx49(args[1], args[2], args[3], delimiter);
                return;

            default:
                p0x48();
                System.exit(1);
        }
    }

    private static ConfigurableApplicationContext s0x43x43() {
        String dbUrl = "jdbc:h2:file:./Converter;AUTO_SERVER=TRUE;";
        return new SpringApplicationBuilder(Cx0A.class)
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

            O3r0oo l0x53$ = ctx.getBean(O3r0oo.class);
            O3w0oo l0x52 = ctx.getBean(O3w0oo.class);

            int code = new Cx0A().h0x4Cx43x4Cx49$(cle, l0x53$, l0x52);
            System.exit(code);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private int h0x4Cx43x4Cx49$(String l0x4B, O3r0oo l0x53$, O3w0oo l0x52) {
        System.out.println(R04oo.d0x116_("fA1YYlYKWlQRV1hQChgsaiU0HVEgEBRdUB1dUR0UBBk="));

        Optional<Licence> l0x4F = l0x52.findTopByOrderByIdDesc();
        if (l0x4F.isPresent() && l0x4F.get().getCleLicence().equalsIgnoreCase(l0x4B)) {
            System.err.println(R04oo.d0x116_("Gnk2cnwlE34RV1xKABJiLzk4TkkgXgxR"));
            return 2;
        }

        O3r0oo.L0x44$ d0x100 = l0x53$.d0x4C$(l0x4B);
        if (d0x100 == null) { System.err.println(R04oo.d0x116_("DVkbUV0bVhIRWk9FDx4mL28=")); return 1;}

        String h0x53;
        try {
            h0x53 = java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            h0x53 = "unknown";
        } 
        if (!R04oo.d0x1161_(d0x100)) {System.out.println(R04oo.d0x116_("DVkbUV0bVhIRWk9FDx4mL28=")); return 1;} 
        Licence l = new Licence();
        l.setCleLicence(l0x4B);
        l0x52.save(l);

        System.out.println(R04oo.d0x116_("GmMtd3A9YGElFHVNABIsKSRxcnY="));
        System.out.println(R04oo.d0x116_("BEgIXUEZR1sXWhkeQw==") + d0x100.getExpirationDate());
        return 0; 
    }

    private static void r0x43x43x4Cx49(String c0x4E, String o0x46, String i0x46, String d0x114) {
        try (ConfigurableApplicationContext ctx = s0x43x43()) {

            O3w0oo l0x52 = ctx.getBean(O3w0oo.class);
            O3r0oo l0x53$ = ctx.getBean(O3r0oo.class);
            R03o c0x53$ = ctx.getBean(R03o.class);
 
            Optional<Licence> l0x4F = l0x52.findTopByOrderByIdDesc(); 
            if (l0x4F.isEmpty()) {
                System.out.println(R04oo.d0x116_("DVkbUV0bVhIZVkpBDQMn"));
                System.exit(2);
            }

            O3r0oo.L0x44$ d0x100 = l0x53$.d0x4C$(l0x4F.get().getCleLicence());
            if (d0x100 == null) { System.out.println(R04oo.d0x116_("DVkbUV0bVhIRWk9FDx4mL28=")); System.exit(1); } 
            if (!R04oo.d0x1161_(d0x100)) {System.out.println(R04oo.d0x116_("DVkbUV0bVhIRWk9FDx4mL28=")); System.exit(1);}

            String b0x101 = c0x4E;
            b0x101 = b0x101.replace("\\", "/"); 
            int l0x53 = b0x101.lastIndexOf("/");
            if (l0x53 != -1) {
                b0x101 = b0x101.substring(l0x53 + 1);
            }

            if (b0x101.endsWith(".xslt")) {
                b0x101 = b0x101.substring(0, b0x101.length() - ".xslt".length());
            } 

            String[] p0x115 = b0x101.split("_", 3);
            if (p0x115.length < 2) {
                throw new RuntimeException(R04oo.d0x116_("B18KWVIME1ZfUVdQEbTBiOg0HVQvRhlYWhxWEkIU") + c0x4E);
            }

            String f0x49x50 = p0x115[1];  
 
            int idx = f0x49x50.indexOf("to");
            if (idx == -1) {
                throw new RuntimeException(R04oo.d0x116_("CF0IW0ALWlAUURlABlcxicKTlE0gQh1GE19VQBdZHgQGA2JtKD9JUmYQHFVdCxMIWA==") + f0x49x50);
            }

            String f0x109 = f0x49x50.substring(0, idx);
            String i0x111 = f0x49x50.substring(idx + 2);
            System.out.println(
                R04oo.d0x116_("DVFYV1wWRVcKR1BLDVckIyI5VFgzEB1aExtcRwpHGUUVEiFqLTAdXi5eHl1UDUFTDF1WSl4=") + c0x4E + "\n" +
                R04oo.d0x116_("bRAeXVAQWlcKFF0DBhk2OCQ0AA==") + i0x46 + "\n" +
                R04oo.d0x116_("bRAeXVAQWlcKFF1BQwQtODU4WAA=") + o0x46 + "\n" +
                R04oo.d0x116_("bRAeW0EVUkZYR1ZRERQndw==") + f0x109 + "\n" +
                R04oo.d0x116_("bRAeW0EVUkZYV1BGDxJ/") + i0x111 + "\n" +
                R04oo.d0x116_("bRAcUV8RXlsMUUxWXg==") + d0x114 + "\n"
            ); 
            boolean o0x107 = c0x53$.t0x110$(c0x4E, i0x46, o0x46, f0x109, i0x111, d0x114);

            if (o0x107) {
                System.out.println(R04oo.d0x116_("GmMtd3A9YGElFHpLDQEnODI4UlNhQh1BQAtaV1Y="));
                System.exit(0);
            } else {
                System.out.println(R04oo.d0x116_("GnY5fX89d29YcVpMBhRiLiRxUVxhUxdaRR1BQRFbVwo="));
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
    private String b0x114;

    @Bean
    @Profile("!cli")
    public org.springframework.boot.CommandLineRunner runner(Oooo a$, O07rOo s$) {
        return args -> {
            String[] ox72 = {"IFQVXV0=","M18XQA==","AFQVXV04AQJJAQ==","AHQ1fX0=","FGM9Zg==","NEAUW1Ic","JV8PWl8XUlY=","Al8WUnURX1c="}; 
            AppUser a0x65 = a$.loadUserByUsername(R04oo.d0x116_(ox72[0]));
            if (a0x65 ==null){
                a$.a0x4Ex55$(R04oo.d0x116_(ox72[0]), R04oo.d0x116_(ox72[0]), R04oo.d0x116_(ox72[1]), R04oo.d0x116_(ox72[2]), R04oo.d0x116_(ox72[2]));
                a$.a0x4Ex52$(R04oo.d0x116_(ox72[3]));
                a$.a0x4Ex52$(R04oo.d0x116_(ox72[4]));
                a$.a0x52x54x55$(R04oo.d0x116_(ox72[0]),R04oo.d0x116_(ox72[3]));
                a$.a0x52x54x55$(R04oo.d0x116_(ox72[0]),R04oo.d0x116_(ox72[4]));
            }
            UserDetails a0x44 = s$.loadUserByUsername(R04oo.d0x116_(ox72[0])); 
            File a0x75 = new File(b0x114+File.separator+R04oo.d0x116_(ox72[5]));
            if (!a0x75.exists()) {
                a0x75.mkdirs();
            }
            File a0x43 = new File(b0x114+File.separator+R04oo.d0x116_(ox72[6]));
            if (!a0x43.exists()) {
                a0x43.mkdirs();
            }
            File a0x55 = new File(b0x114 + File.separator + R04oo.d0x116_(ox72[7]));
            if (!a0x55.exists()) {
                a0x55.mkdirs();
            } 
        };
    }
}
