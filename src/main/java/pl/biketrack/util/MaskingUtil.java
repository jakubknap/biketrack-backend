package pl.biketrack.util;

import lombok.experimental.UtilityClass;

import static java.util.Objects.isNull;

@UtilityClass
public class MaskingUtil {

    public static String maskEmail(String input) {
        if (isNull(input) || input.isEmpty()) {
            return null;
        }

        int atIndex = input.indexOf('@');
        if (atIndex < 0) {
            return "***";
        }

        String maskedPart = input.substring(0, 2) + "*".repeat(atIndex - 2);
        return maskedPart + input.substring(atIndex);
    }
}