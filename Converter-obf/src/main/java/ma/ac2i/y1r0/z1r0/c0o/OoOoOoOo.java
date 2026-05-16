package ma.ac2i.y1r0.z1r0.c0o;
import ma.ac2i.y1r0.z1r0.m0o.OoOoOoOoOo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class OoOoOoOo implements WebMvcConfigurer {

    @Autowired
    private final OoOoOoOoOo l0x4D$;

    public OoOoOoOo(OoOoOoOoOo l0x4D$) {
        this.l0x4D$ = l0x4D$;
    }

    @Override
    public void addInterceptors(InterceptorRegistry r0x121$) {
        r0x121$.addInterceptor(l0x4D$)
                .addPathPatterns("/**");
    }
}
