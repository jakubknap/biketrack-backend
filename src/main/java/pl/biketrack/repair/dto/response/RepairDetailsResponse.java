package pl.biketrack.repair.dto.response;

import pl.biketrack.bike.dto.response.BikeSelectListResponse;
import pl.biketrack.dashboard.dto.MoneyDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RepairDetailsResponse(UUID repairUuid,
                                    BikeSelectListResponse bike,
                                    String title,
                                    String description,
                                    MoneyDto cost,
                                    LocalDate repairDate,
                                    LocalDateTime createdDate,
                                    LocalDateTime lastModifiedDate,
                                    List<UUID> photos) {}