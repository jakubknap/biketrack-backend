package pl.biketrack.repair.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RepairListResponse(
        UUID repairUuid,
        String title,
        LocalDate repairDate,
        LocalDateTime createdDate
) {}