package at.networkexplorer.backend.utils;

import java.util.Arrays;
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

}
