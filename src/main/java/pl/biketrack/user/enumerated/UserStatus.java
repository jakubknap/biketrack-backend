package pl.biketrack.user.enumerated;

public enum UserStatus {
    ACTIVE,
    INACTIVE;

    public boolean isActive() {
        return ACTIVE == this;
    }
}