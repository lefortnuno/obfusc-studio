package ma.ac2i.converter.converter.service;

import ma.ac2i.converter.converter.entities.AppRole;
import ma.ac2i.converter.converter.entities.AppUser;

public interface AccountService {
    AppUser addNewUser(String username,String firstname,String lastname,String pasword,String confirmPassword);
    AppUser updateUser(String userID,String username, String firstname, String lastname, String newPassword, String confirmPassword);
    AppRole addNewRole(String role);
    void addRoleToUser(String username,String role);
    void removeRoleFromUser(String username,String role);

    AppUser loadUserByUsername(String username);
}
