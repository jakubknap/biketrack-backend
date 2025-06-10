package pl.biketrack.common.constant;

public class Patterns {

    public static final String NICKNAME_PATTERN = "^[\\p{L}0-9._-]+$";
    public static final String PASSWORD_PATTERN = "^(?=.*\\p{Ll})(?=.*\\p{Lu})(?=.*\\d)(?=.*[@$!%*?&.\\-_])[\\p{L}\\d@$!%*?&.\\-_]{8,}$";
    public static final String SAVE_TEXT_PATTERN = "^[\\p{L}\\p{N} .,!?:;'\"()@&%\\-_]+$";
}