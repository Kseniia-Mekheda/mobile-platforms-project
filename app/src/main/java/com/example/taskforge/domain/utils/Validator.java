package com.example.taskforge.domain.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {

    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    /**
     * Перевіряє правильність формату email-адреси.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * Перевірка пароля: має бути не менше 6 символів.
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Перевірка суми для фінансів (FinanceRecord / Subscription).
     * Сума має бути більшою за нуль.
     */
    public static boolean isValidAmount(double amount) {
        return amount > 0.0;
    }

    /**
     * Перевірка, чи не є рядок порожнім (для назв проектів, тасок тощо).
     */
    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }
}
