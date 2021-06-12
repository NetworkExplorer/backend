package at.networkexplorer.backend.utils;

import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import java.util.Random;

public class PasswordUtil {

    private static final int SALT_LENGTH = 8;
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 512;

    /**
     * Generate a new password
     * @param length Length of the password in characters
     * @return Password as String
     */
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

    /**
     * Hashes a given password using the given secret
     * @param password The password to hash
     * @param secret The secret to use
     * @return Hex string of the hashed password
     */
    public static String hashPassword(final String password, final String secret) {
        Pbkdf2PasswordEncoder pbkdf2 = new Pbkdf2PasswordEncoder(secret, SALT_LENGTH, ITERATIONS, KEY_LENGTH);
        return pbkdf2.encode(password);
    }

    /**
     * Compares a given password to the given hash.
     * @param password The password to compare
     * @param secret The secret that was used to create the hash
     * @param hash The hash to compare the password to
     * @return True if the password is correct, otherwise false
     */
    public static boolean checkPasswordHash(final String password, final String secret, final String hash) {
        Pbkdf2PasswordEncoder pbkdf2 = new Pbkdf2PasswordEncoder(secret, SALT_LENGTH, ITERATIONS, KEY_LENGTH);
        return pbkdf2.matches(password, hash);
    }

}
