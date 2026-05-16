package ma.ac2i.y1r0.z1r0.s0o;

import lombok.AllArgsConstructor;
import ma.ac2i.y1r0.z1r0.e0o.AppRole;
import ma.ac2i.y1r0.z1r0.e0o.AppUser;
import ma.ac2i.y1r0.z1r0.r0o.Ooo;
import ma.ac2i.y1r0.z1r0.r0o.O2w0o;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class O2r0o implements Oooo {
    private O2w0o a0x55x52$;
    private Ooo a0x52x52$;

    @Override
    public AppUser a0x4Ex55$(String u0x101, String f0x101, String l0x101, String d0x100, String c0x50) {
        PasswordEncoder p0x45 = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        AppUser a0x55 = a0x55x52$.findByUsername(u0x101);
        String p0x52 = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        if (a0x55 != null) throw new RuntimeException("ce nom d'utilisateur existe déjà");
        if (!d0x100.matches(p0x52)) throw new RuntimeException("Mot de passe invalide. Veuillez suivre les critères de sécurité : \n- Au moins 8 caractères\n- Au moins une lettre majuscule\n- Au moins une lettre minuscule\n- Au moins un chiffre\n- Au moins un caractère spécial parmi @#$%^&+=!\n- Aucun espace n'est autorisé.");
        if (!d0x100.equals(c0x50)) throw new RuntimeException("Le mot de passe de confirmation et le mot de passe ne concordent pas.");
        AppUser U0x114 = AppUser.builder().userID(UUID.randomUUID().toString()).username(u0x101).firstname(f0x101).lastname(l0x101).password(p0x45.encode(d0x100)).build();
        AppUser s0x41x55 = a0x55x52$.save(U0x114);
        return s0x41x55;
    }
    @Override
    public AppUser u0x55$(String u0x49x44, String u0x101, String f0x101, String l0x101, String n0x50, String c0x50) {
        PasswordEncoder p0x45 = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        AppUser a0x55 = a0x55x52$.findById(u0x49x44).orElse(null);
        // Check if the user exists
        if (a0x55 == null) {
            throw new RuntimeException("User not found");
        }
        if(!a0x55.getUsername().equals(u0x101)){
            AppUser a0x551 = a0x55x52$.findByUsername(u0x101);
            if (a0x551 != null) throw new RuntimeException("ce nom d'utilisateur existe déjà");
            if (a0x55.getUsername().equals("admin")) throw new RuntimeException("ce nom d'utilisateur n'est pas disponible");
            a0x55.setUsername(u0x101);
        }
        // Update the user's information
        a0x55.setFirstname(f0x101);
        a0x55.setLastname(l0x101);
        // Update the password if newPassword is provided
        if (n0x50 != null && !n0x50.isEmpty()) {
            // Check if the passwords match if newPassword is provided
            if (n0x50 != null && !n0x50.equals(c0x50)) {
                throw new RuntimeException("Le mot de passe de confirmation et le mot de passe ne concordent pas.");
            }
            a0x55.setPassword(p0x45.encode(n0x50));
        }
        // Save the updated user
        AppUser u0x55 = a0x55x52$.save(a0x55);
        return u0x55;
    }


    @Override
    public AppRole a0x4Ex52$(String r0x101) {
        AppRole a0x52 = a0x52x52$.findById(r0x101).orElse(null);
        if (a0x52 != null) throw new RuntimeException("this role already exists");
        a0x52 = AppRole.builder().role(r0x101).build();
        return a0x52x52$.save(a0x52);
    }

    @Override
    public void a0x52x54x55$(String u0x101, String r0x101) {
        AppUser a0x55 = a0x55x52$.findByUsername(u0x101);
        if (a0x55 == null) throw new RuntimeException("this user don't exsists");
        AppRole a0x52 = a0x52x52$.findById(r0x101).orElse(null);
        if (a0x52 == null) throw new RuntimeException("this role don't exists");
        if (!a0x55.getRoles().contains(a0x52)) {
            a0x55.getRoles().add(a0x52);
        }
    }

    @Override
    public void r0x52x46x55$(String u0x101, String r0x101) {
        AppUser a0x55 = a0x55x52$.findByUsername(u0x101);
        if (a0x55 == null) throw new RuntimeException("this user don't exsists");
        AppRole a0x52 = a0x52x52$.findById(r0x101).orElse(null);
        if (a0x52 == null) throw new RuntimeException("this role don't exists");
        if (a0x55.getRoles().contains(a0x52)) {
            a0x55.getRoles().remove(a0x52);
        } else throw new RuntimeException("this user don't have the role");
    }

    @Override
    public AppUser loadUserByUsername(String u0x101) {
        AppUser a0x55 = a0x55x52$.findByUsername(u0x101); 
        return a0x55;
    }
}
