package pl.biketrack.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MaskingUtil {
    
    public static String maskEmail(String input) {
        int atIndex = input.indexOf('@');
        if (atIndex < 0) {
            return "***";
        }

        String maskedPart = input.substring(0, 2) + "*".repeat(atIndex - 2);
        return maskedPart + input.substring(atIndex);
    }
}