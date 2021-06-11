package at.networkexplorer.backend.utils;

import org.apache.commons.codec.binary.Hex;

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

    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 512;
    private static final String SALT = "saltKey";

    public static String convertStringToHash(String password, String secret) {
        return Hex.encodeHexString(hashPassword(password.toCharArray(), secret));
    }

    private static byte[] hashPassword(final char[] password, String secret) {
        SecretKeyFactory secretKeyFactory = null;
        try {
            secretKeyFactory = SecretKeyFactory.getInstance(secret);
            PBEKeySpec spec = new PBEKeySpec(password, SALT.getBytes(), ITERATIONS, KEY_LENGTH);
            SecretKey key = secretKeyFactory.generateSecret(spec);
            return key.getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

}
