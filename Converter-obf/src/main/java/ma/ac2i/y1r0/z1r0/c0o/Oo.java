// vT1

// package ma.ac2i.y1r0.z1r0.c0o;
// import ma.ac2i.y1r0.z1r0.s0o.R04oo;

// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import org.springframework.security.authentication.*;
// import org.springframework.security.core.AuthenticationException;
// import org.springframework.security.web.authentication.AuthenticationFailureHandler;
// import org.springframework.stereotype.Component;

// import java.io.IOException;
// @Component
// public class Oo implements AuthenticationFailureHandler {
//     @Override
//     public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
//         String e0x4D;
//         if (exception instanceof BadCredentialsException) { 
//             e0x4D = R04oo.d0x116_("D18VFFdfRkYRWFBXAgMnPzNxUkhhXRdAExxWEghVSlcGVyskIj5PT4Kzup1QDA==");
//         } else if (exception instanceof LockedException) {
//             e0x4D = R04oo.d0x116_("DVVYV1wVQ0YdFExQChsrOSAlWEgzEB1HR1hFVwpGVlEKGy6JwpOU");
//         } else if (exception instanceof DisabledException) {
//             e0x4D = R04oo.d0x116_("DVVYV1wVQ0YdFExQChsrOSAlWEgzEB1HR1hX8fv2kFcCFDYjN5K+/+g=");
//         } else if (exception instanceof AccountExpiredException) {
//             e0x4D = R04oo.d0x116_("DVVYV1wVQ0YdFExQChsrOSAlWEgzEBkUVgBDWwr3uubK");
//         } else if (exception instanceof CredentialsExpiredException) {
//             e0x4D = R04oo.d0x116_("DVULFFoWVV0KWVhQChgsOWE1GlQlVRZAWh5aURlAUEsNVyYvYT0aSDVZFF1AGUdXDUYZSw0DYi85IVRPgrO6nQ==");
//         } else {
//             e0x4D = R04oo.d0x116_("grOatIMbW1cbFF1BQxtlKzQlVVgvRBFSWhtSRhFbVw==");
//         }
//         request.getSession().setAttribute("e0x4D", e0x4D);
//         request.getRequestDispatcher("/login").forward(request, response);
//     }

// }


// package ma.ac2i.y1r0.z1r0.c0o;

// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import org.springframework.security.authentication.*;
// import org.springframework.security.core.AuthenticationException;
// import org.springframework.security.web.authentication.AuthenticationFailureHandler;
// import org.springframework.stereotype.Component;

// import java.io.IOException;
// @Component
// public class Oo implements AuthenticationFailureHandler {
//     @Override
//     public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
//         String e0x4D;
//         if (exception instanceof BadCredentialsException) {
//             e0x4D = "Nom d'utilisateur ou mot de passe incorrect";
//         } else if (exception instanceof LockedException) {
//             e0x4D = "Le compte utilisateur est verrouillé";
//         } else if (exception instanceof DisabledException) {
//             e0x4D = "Le compte utilisateur est désactivé";
//         } else if (exception instanceof AccountExpiredException) {
//             e0x4D = "Le compte utilisateur a expiré";
//         } else if (exception instanceof CredentialsExpiredException) {
//             e0x4D = "Les informations d'identification de l'utilisateur ont expiré";
//         } else {
//             e0x4D = "Échec de l'authentification";
//         }
//         request.getSession().setAttribute("e0x4D", e0x4D);
//         request.getRequestDispatcher("/login").forward(request, response);
//     }

// }


package ma.ac2i.y1r0.z1r0.c0o;
import ma.ac2i.y1r0.z1r0.s0o.R04oo;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class Oo implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String e0x4D;
        if (exception instanceof BadCredentialsException) { 
            e0x4D = R04oo.d0x116_("D18VFFdfRkYRWFBXAgMnPzNxUkhhXRdAExxWEghVSlcGVyskIj5PTyRTDA==");
        } else if (exception instanceof LockedException) {
            e0x4D = R04oo.d0x116_("DVVYV1wVQ0YdFExQChsrOSAlWEgzEB1HR1hFVwpGVlEKGy6JwpOU");
        } else if (exception instanceof DisabledException) {
            e0x4D = R04oo.d0x116_("DVVYV1wVQ0YdFExQChsrOSAlWEgzEB1HR1hX8fv2kFcCFDYjN5K+/+g=");
        } else if (exception instanceof AccountExpiredException) {
            e0x4D = R04oo.d0x116_("DVVYV1wVQ0YdFExQChsrOSAlWEgzEBkUVgBDWwr3uubK");
        } else if (exception instanceof CredentialsExpiredException) {
            e0x4D = R04oo.d0x116_("DVULFFoWVV0KWVhQChgsOWE1GlQlVRZAWh5aURlAUEsNVyYvYT0aSDVZFF1AGUdXDUYZSw0DYi85IVRPgrO6nQ==");
        } else {
            e0x4D = R04oo.d0x116_("grOatIMbW1cbFF1BQxtlKzQlVVgvRBFSWhtSRhFbVw==");
        }
        request.getSession().setAttribute("errorMessage", e0x4D);
        response.sendRedirect(request.getContextPath() + "/login");
    }

}
