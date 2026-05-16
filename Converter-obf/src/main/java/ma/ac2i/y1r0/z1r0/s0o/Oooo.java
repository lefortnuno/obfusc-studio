package ma.ac2i.y1r0.z1r0.s0o;

import ma.ac2i.y1r0.z1r0.e0o.AppRole;
import ma.ac2i.y1r0.z1r0.e0o.AppUser;

public interface Oooo {
    AppUser a0x4Ex55$(String username,String firstname,String lastname,String pasword,String confirmPassword);
    AppUser u0x55$(String userID,String username, String firstname, String lastname, String newPassword, String confirmPassword);
    AppRole a0x4Ex52$(String role);
    void a0x52x54x55$(String username,String role);
    void r0x52x46x55$(String username,String role);

    AppUser loadUserByUsername(String username);
}
