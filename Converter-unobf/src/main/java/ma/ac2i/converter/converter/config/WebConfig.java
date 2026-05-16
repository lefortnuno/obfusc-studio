package ma.ac2i.converter.converter.config;
import ma.ac2i.converter.converter.middleware.LicenceMiddleware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private final LicenceMiddleware licenceMiddleware;

    public WebConfig(LicenceMiddleware licenceMiddleware) {
        this.licenceMiddleware = licenceMiddleware;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(licenceMiddleware)
                .addPathPatterns("/**");
    }
}
