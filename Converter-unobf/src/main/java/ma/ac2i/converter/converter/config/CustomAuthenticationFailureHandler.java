package ma.ac2i.converter.converter.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String errorMessage;
        if (exception instanceof BadCredentialsException) {
            errorMessage = "Nom d'utilisateur ou mot de passe incorrect";
        } else if (exception instanceof LockedException) {
            errorMessage = "Le compte utilisateur est verrouillé";
        } else if (exception instanceof DisabledException) {
            errorMessage = "Le compte utilisateur est désactivé";
        } else if (exception instanceof AccountExpiredException) {
            errorMessage = "Le compte utilisateur a expiré";
        } else if (exception instanceof CredentialsExpiredException) {
            errorMessage = "Les informations d'identification de l'utilisateur ont expiré";
        } else {
            errorMessage = "Échec de l'authentification";
        }
        request.getSession().setAttribute("errorMessage", errorMessage);
        response.sendRedirect(request.getContextPath() + "/login");
    }

}
