package com.heritage.platform.security;

import java.util.regex.Pattern;

public final class PasswordPolicy {

    public static final int MIN_LENGTH = 6;
    public static final String ERROR_MESSAGE =
            "Password must be at least 6 characters and include both letters and numbers";

    private static final Pattern LETTER_PATTERN = Pattern.compile("[A-Za-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");

    private PasswordPolicy() {
    }

    public static boolean isValid(String password) {
        return password != null
                && password.length() >= MIN_LENGTH
                && LETTER_PATTERN.matcher(password).find()
                && DIGIT_PATTERN.matcher(password).find();
    }

    public static void validateOrThrow(String password) {
        if (!isValid(password)) {
            throw new RuntimeException(ERROR_MESSAGE);
        }
    }
}
