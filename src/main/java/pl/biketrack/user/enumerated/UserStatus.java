package pl.biketrack.user.enumerated;

public enum UserStatus {
    REGISTERED,
    ACTIVE,
    BLOCKED,
    DEACTIVATED;

    public boolean isActive() {
        return ACTIVE == this;
    }

    public boolean isBlocked() {
        return BLOCKED == this;
    }
}