package pl.biketrack.bike.dto.response;

import java.util.UUID;

public record BikeListResponse(
        UUID uuid,
        String name,
        UUID photoFileName
) {}