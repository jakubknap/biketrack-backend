package pl.biketrack.bike.dto.response;

import java.util.UUID;

public record BikeListResponse(
        UUID bikeUuid,
        String name
) {}