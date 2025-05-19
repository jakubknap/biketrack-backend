package pl.biketrack.common.constant;

public class Patterns {

    public static final String NICKNAME_PATTERN = "^[\\p{L}0-9]+$";
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.\\-_])[A-Za-z\\d@$!%*?&.\\-_]{8,}$";
}