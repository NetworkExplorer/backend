package at.networkexplorer.backend.utils;

import org.apache.commons.codec.binary.Hex;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

public class PasswordUtil {

    public static String generate(int length)
    {
        String symbol = "-/.^&*_!@%=+>)";
        String cap_letter = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String small_letter = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";


        String finalString = cap_letter + small_letter +
                numbers + symbol;

        Random random = new Random();

        String password = "";

        for (int i = 0; i < length; i++)
        {
            password += finalString.charAt(random.nextInt(finalString.length()));
        }

        return password;
    }

    private static final int SALT_LENGTH = 8;
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 512;

    public static String hashPassword(final String password, final String secret) {
        Pbkdf2PasswordEncoder pbkdf2 = new Pbkdf2PasswordEncoder(secret, SALT_LENGTH, ITERATIONS, KEY_LENGTH);
        return pbkdf2.encode(password);
    }

    public static boolean checkPasswordHash(final String password, final String secret, final String hash) {
        Pbkdf2PasswordEncoder pbkdf2 = new Pbkdf2PasswordEncoder(secret, SALT_LENGTH, ITERATIONS, KEY_LENGTH);
        return pbkdf2.matches(password, hash);
    }

}
