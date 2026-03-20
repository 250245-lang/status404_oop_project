package parkinglot.utils;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class BCryptUtil {
    
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    
    public static boolean checkPassword(String password, String hashed) {
        if (hashed == null || !hashed.startsWith("$2a$")) {
            return password.equals(hashed); // Fallback for plain text
        }
        return BCrypt.checkpw(password, hashed);
    }
}
