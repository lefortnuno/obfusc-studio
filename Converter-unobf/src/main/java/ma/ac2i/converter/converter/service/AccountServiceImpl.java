package ma.ac2i.converter.converter.service;

import lombok.AllArgsConstructor;
import ma.ac2i.converter.converter.entities.AppRole;
import ma.ac2i.converter.converter.entities.AppUser;
import ma.ac2i.converter.converter.repository.AppRoleRepository;
import ma.ac2i.converter.converter.repository.AppUserRepository;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {
    private AppUserRepository appUserRepository;
    private AppRoleRepository appRoleRepository;

    @Override
    public AppUser addNewUser(String username, String firstname, String lastname, String pasword, String confirmPassword) {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        AppUser appUser = appUserRepository.findByUsername(username);
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        if (appUser != null) throw new RuntimeException("ce nom d'utilisateur existe déjà");
        if (!pasword.matches(passwordRegex)) throw new RuntimeException("Mot de passe invalide. Veuillez suivre les critères de sécurité : \n- Au moins 8 caractères\n- Au moins une lettre majuscule\n- Au moins une lettre minuscule\n- Au moins un chiffre\n- Au moins un caractère spécial parmi @#$%^&+=!\n- Aucun espace n'est autorisé.");
        if (!pasword.equals(confirmPassword)) throw new RuntimeException("Le mot de passe de confirmation et le mot de passe ne concordent pas.");
        AppUser User = AppUser.builder().userID(UUID.randomUUID().toString()).username(username).firstname(firstname).lastname(lastname).password(passwordEncoder.encode(pasword)).build();
        AppUser saveAppUser = appUserRepository.save(User);
        return saveAppUser;
    }
    @Override
    public AppUser updateUser(String userID,String username, String firstname, String lastname, String newPassword, String confirmPassword) {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        AppUser appUser = appUserRepository.findById(userID).orElse(null);
        // Check if the user exists
        if (appUser == null) {
            throw new RuntimeException("User not found");
        }
        if(!appUser.getUsername().equals(username)){
            AppUser appUser1 = appUserRepository.findByUsername(username);
            if (appUser1 != null) throw new RuntimeException("ce nom d'utilisateur existe déjà");
            if (appUser.getUsername().equals("admin")) throw new RuntimeException("ce nom d'utilisateur n'est pas disponible");
            appUser.setUsername(username);
        }
        // Update the user's information
        appUser.setFirstname(firstname);
        appUser.setLastname(lastname);
        // Update the password if newPassword is provided
        if (newPassword != null && !newPassword.isEmpty()) {
            // Check if the passwords match if newPassword is provided
            if (newPassword != null && !newPassword.equals(confirmPassword)) {
                throw new RuntimeException("Le mot de passe de confirmation et le mot de passe ne concordent pas.");
            }
            appUser.setPassword(passwordEncoder.encode(newPassword));
        }
        // Save the updated user
        AppUser updatedUser = appUserRepository.save(appUser);
        return updatedUser;
    }


    @Override
    public AppRole addNewRole(String role) {
        AppRole appRole = appRoleRepository.findById(role).orElse(null);
        if (appRole != null) throw new RuntimeException("this role already exists");
        appRole = AppRole.builder().role(role).build();
        return appRoleRepository.save(appRole);
    }

    @Override
    public void addRoleToUser(String username, String role) {
        AppUser appUser = appUserRepository.findByUsername(username);
        if (appUser == null) throw new RuntimeException("this user don't exsists");
        AppRole appRole = appRoleRepository.findById(role).orElse(null);
        if (appRole == null) throw new RuntimeException("this role don't exists");
        if (!appUser.getRoles().contains(appRole)) {
            appUser.getRoles().add(appRole);
        }
    }

    @Override
    public void removeRoleFromUser(String username, String role) {
        AppUser appUser = appUserRepository.findByUsername(username);
        if (appUser == null) throw new RuntimeException("this user don't exsists");
        AppRole appRole = appRoleRepository.findById(role).orElse(null);
        if (appRole == null) throw new RuntimeException("this role don't exists");
        if (appUser.getRoles().contains(appRole)) {
            appUser.getRoles().remove(appRole);
        } else throw new RuntimeException("this user don't have the role");
    }

    @Override
    public AppUser loadUserByUsername(String username) {
        AppUser appUser = appUserRepository.findByUsername(username);
        //if (appUser == null) throw new RuntimeException("this user don't exsists");
        return appUser;
    }
}
