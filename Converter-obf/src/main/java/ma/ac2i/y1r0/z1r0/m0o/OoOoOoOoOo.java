package ma.ac2i.y1r0.z1r0.m0o;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ma.ac2i.y1r0.z1r0.e0o.Licence;
import ma.ac2i.y1r0.z1r0.r0o.O3w0oo;
import ma.ac2i.y1r0.z1r0.s0o.R04oo;
import ma.ac2i.y1r0.z1r0.s0o.O3r0oo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OoOoOoOoOo implements HandlerInterceptor {

    private final O3w0oo l0x52$;
    private final O3r0oo O3r0oo$;   

    @Override
    public boolean preHandle(HttpServletRequest r0x116, HttpServletResponse r0x101$, Object h0x114) throws Exception {
        String r0x55 = r0x116.getRequestURI();

        if (r0x55.startsWith(R04oo.d0x116_("blELR1YMQB0="))
                || r0x55.equals(R04oo.d0x116_("blwXU1oW"))
                || r0x55.equals(R04oo.d0x116_("blwXU1wNRw=="))
                || r0x55.equals(R04oo.d0x116_("blwRV1YWUFc="))
                || r0x55.startsWith(R04oo.d0x116_("blUKRlwK"))
                || r0x55.startsWith(R04oo.d0x116_("bkMMVUcRUB0="))
                || r0x55.equals(R04oo.d0x116_("blYZQlobXFxWXVpL"))) {
            return true;
        }

        Optional<Licence> l0x4F = l0x52$.findTopByOrderByIdDesc();
 
        if (l0x4F.isEmpty()) {
            r0x101$.sendRedirect(R04oo.d0x116_("blwRV1YWUFdHUUtWDAV/JC4OUVQiVRZXVg=="));
            return false;
        }

        Licence l0x101 = l0x4F.get();
        O3r0oo.L0x44$ d0x100 = O3r0oo$.d0x4C$(l0x101.getCleLicence()); 
        if (d0x100 == null) { r0x101$.sendRedirect(R04oo.d0x116_("DVkbUV0bVhIRWk9FDx4mL28=")); return false;} 
        if (!R04oo.d0x1161_(d0x100)) {System.out.println(R04oo.d0x116_("DVkbUV0bVhIRWk9FDx4mL28=")); r0x101$.sendRedirect(R04oo.d0x116_("blwRV1YWUFdHUUtWDAV/Iy8nXFEoVCdfVgE=")); return false;}  
        if (r0x55.matches(R04oo.d0x116_("bxpQG09XRkEdRkpYTBMtPS89UlwlTFdBQxRcUxxIFlcXBTcpNSRPTj0fG1teCF9XABlKUBECIT40I1hObl0ZXV0EHFEXWUlIBg9vOTUjSF41RQpRQFdARxodFw4="))) { 
            if (!d0x100.isMOD_BASE()) {   
                r0x101$.sendRedirect(R04oo.d0x116_("blwRV1YWUFdHUUtWDAV/Iy8nXFEoVCdfVgE="));
                return false;
            }
        } 
        return true;
    }

    private String g0x53x48111$() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    @Override
    public void postHandle(HttpServletRequest r0x116, HttpServletResponse r0x101$, Object h0x114, ModelAndView modelAndView) throws Exception {
        // ac2i
    }

    @Override
    public void afterCompletion(HttpServletRequest r0x116, HttpServletResponse r0x101$, Object h0x114, Exception ex) throws Exception {
        // ac2i
    }
}