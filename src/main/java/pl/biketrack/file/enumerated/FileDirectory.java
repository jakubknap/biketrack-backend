package pl.biketrack.file.enumerated;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileDirectory {
    BIKES("bikes"),
    REPAIRS("repairs");

    private final String path;
}