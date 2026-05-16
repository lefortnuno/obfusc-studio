package ma.ac2i.converter.converter.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import ma.ac2i.converter.converter.service.UserDetailServiceImp;
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
public class SecurityConfig {

    private UserDetailServiceImp userDetailServiceImp;
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(
                authorize -> authorize.requestMatchers("/assets/**").permitAll().anyRequest().authenticated()
        ).formLogin(
                form -> form.loginPage("/login").failureHandler(customAuthenticationFailureHandler).permitAll()
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
        ).userDetailsService(userDetailServiceImp);
        return httpSecurity.build();
    }

    //@Bean
    public JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }
    
    // @Bean
    // public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    //     http
    //         .csrf(csrf -> csrf.disable())
    //         .authorizeHttpRequests(auth -> auth
    //             .anyRequest().permitAll()
    //         )
    //         .securityMatcher("/**")  // facultatif
    //         .headers(headers -> headers.frameOptions().disable()) // si tu utilises H2
    //         .httpBasic(httpBasic -> {}) // facultatif
    //         .formLogin(form -> form.disable()); // important si form login actif

    //     return http.build();
    // }

}
