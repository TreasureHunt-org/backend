package org.treasurehunt.common.util;

import java.security.SecureRandom;

public final class UniqueKeyGenerator {
    private UniqueKeyGenerator(){}

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int KEY_LENGTH = 10;
    private static final SecureRandom random = new SecureRandom();

    public static String generateKey() {
        StringBuilder sb = new StringBuilder(KEY_LENGTH);
        for (int i = 0; i < KEY_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}