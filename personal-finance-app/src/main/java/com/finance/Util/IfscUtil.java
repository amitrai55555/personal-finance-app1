package com.finance.Util;

import java.util.Locale;
import java.util.Map;

public final class IfscUtil {

    private static final Map<String, String> BANK_PREFIXES = Map.ofEntries(
            Map.entry("STATE BANK OF INDIA", "SBIN"),
            Map.entry("SBI", "SBIN"),
            Map.entry("HDFC BANK", "HDFC"),
            Map.entry("HDFC", "HDFC"),
            Map.entry("ICICI BANK", "ICIC"),
            Map.entry("ICICI", "ICIC"),
            Map.entry("AXIS BANK", "UTIB"),
            Map.entry("AXIS", "UTIB"),
            Map.entry("KOTAK MAHINDRA BANK", "KKBK"),
            Map.entry("KOTAK", "KKBK"),
            Map.entry("PUNJAB NATIONAL BANK", "PUNB"),
            Map.entry("PNB", "PUNB")
    );

    private IfscUtil() {}

    public static String normalize(String bankName, String rawIfsc) {
        if (bankName == null || rawIfsc == null) {
            throw new IllegalArgumentException("Bank name and IFSC are required");
        }

        String trimmed = rawIfsc.trim().toUpperCase(Locale.ROOT);
        String normalizedBank = bankName.trim().toUpperCase(Locale.ROOT);

        if (!trimmed.matches("^[A-Z0-9]{7,}$")) {
            throw new IllegalArgumentException("IFSC must contain branch code (7+ chars)");
        }

        String prefix = BANK_PREFIXES.get(normalizedBank);
        if (prefix == null) {
            // Unknown bank: keep user-supplied code but enforce full IFSC shape
            validateIfsc(trimmed);
            return trimmed;
        }

        if (trimmed.length() < 11) {
            throw new IllegalArgumentException("Full IFSC (11 chars) required to auto-adjust prefix");
        }

        // Replace the first 4 characters with the required prefix, keep the last 7 (0 + branch code)
        String rebuilt = prefix + trimmed.substring(trimmed.length() - 7);

        validateIfsc(rebuilt);
        return rebuilt;
    }

    private static void validateIfsc(String ifsc) {
        if (!ifsc.matches("^[A-Z]{4}0[A-Z0-9]{6}$")) {
            throw new IllegalArgumentException("Invalid IFSC format");
        }
    }
}
