package ma.ac2i.y1r0.z1r0.c0o;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import ma.ac2i.y1r0.z1r0.s0o.O07rOo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class OoOoOo {

    private O07rOo u0x44x53x49;
    private Oo c0x41x46x48;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity h0x53) throws Exception {
        h0x53.authorizeHttpRequests(
                authorize -> authorize.requestMatchers("/assets/**").permitAll().anyRequest().authenticated()
        ).formLogin(
                form -> form.loginPage("/login").failureHandler(c0x41x46x48).permitAll()
        ).csrf((csrf) -> csrf
                .ignoringRequestMatchers("/h2-console/**")
        ).exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"message\":\"Votre session a expiré. Veuillez vous reconnecter.\"}");
                    } else {
                        response.sendRedirect(request.getContextPath() + "/login");
                    }
                })
        ).userDetailsService(u0x44x53x49);
        return h0x53.build();
    }

    //@Bean
    public JdbcUserDetailsManager jdbcUserDetailsManager(DataSource d0x53) {
        return new JdbcUserDetailsManager(d0x53);
    }

}
