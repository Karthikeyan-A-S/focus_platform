package com.example.focusplatform.util;

/**
 * Quiz options are keyed by letter (A–D). Teachers store the correct key;
 * students submit the same letter when answering.
 */
public final class QuestionOptionUtil {

    private static final String VALID_OPTIONS = "ABCD";

    private QuestionOptionUtil() {
    }

    /** Normalizes and validates a teacher's correct-option choice (A, B, C, or D). */
    public static String normalizeCorrectOption(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Correct option is required. Choose A, B, C, or D.");
        }
        String letter = value.trim().toUpperCase();
        if (letter.length() == 1 && VALID_OPTIONS.indexOf(letter.charAt(0)) >= 0) {
            return letter;
        }
        throw new IllegalArgumentException(
                "Correct option must be a single letter A, B, C, or D (not the full answer text).");
    }

    /** Normalizes a student's selected option for comparison. */
    public static String normalizeSelectedOption(String value) {
        return normalizeCorrectOption(value);
    }

    public static boolean optionsMatch(String storedCorrect, String studentSelection) {
        return normalizeCorrectOption(storedCorrect).equals(normalizeSelectedOption(studentSelection));
    }
}
