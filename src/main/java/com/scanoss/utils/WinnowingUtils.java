package com.scanoss.utils;

public class WinnowingUtils {

    /**
     * Normalise the given character
     *
     * @param c character to normalise
     * @return normalised character
     */
    public static char normalize(char c) {
        if (c < '0' || c > 'z') {
            return 0;
        } else if (c <= '9' || c >= 'a') {
            return c;
        } else if (c >= 'A' && c <= 'Z') {
            return (char) (c + 32);
        } else {
            return 0;
        }
    }
}
