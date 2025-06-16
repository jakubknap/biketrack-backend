package pl.biketrack.bike.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record BikeRepairResponse(
        UUID repairUuid,
        String title,
        LocalDateTime createdDate
) {}