package pl.biketrack.bike.dto;

import com.neovisionaries.i18n.CurrencyCode;
import pl.biketrack.bike.model.Bike;
import pl.biketrack.file.service.FileStorageService;
import pl.biketrack.repair.model.Repair;
import pl.biketrack.repair.model.RepairPhoto;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static pl.biketrack.file.enumerated.FileDirectory.REPAIRS;

public record BikeReportDto(BikeDto bike, String generationTime) {

    public static Map<String, Object> prepareVariables(Bike bike, FileStorageService fileStorageService) {
        BikeReportDto bikeReportDto = fromBike(bike, fileStorageService);

        Map<String, Object> variables = new HashMap<>();

        variables.put("generationTime", bikeReportDto.generationTime());
        variables.put("bike", bikeReportDto.bike());

        return variables;
    }

    private static BikeReportDto fromBike(Bike bike, FileStorageService fileStorageService) {
        return new BikeReportDto(BikeDto.fromBike(bike, fileStorageService), LocalDateTime.now().format(ofPattern("yyyy-MM-dd HH:mm")));
    }

    private record BikeDto(String name,
                           String brand,
                           String model,
                           String type,
                           String mileageKm,
                           String serialNumber,
                           LocalDate purchaseDate,
                           String description,
                           List<RepairDto> repairs) {

        private static BikeDto fromBike(Bike bike, FileStorageService fileStorageService) {
            return new BikeDto(bike.getName(),
                               bike.getBrand(),
                               bike.getModel(),
                               bike.getType(),
                               bike.getMileageKm(),
                               bike.getSerialNumber(),
                               bike.getPurchaseDate(),
                               bike.getDescription(),
                               RepairDto.fromRepairs(bike.getRepairs(), fileStorageService));
        }

        private record RepairDto(String title,
                                 String description,
                                 BigDecimal cost,
                                 String currency,
                                 LocalDate repairDate,
                                 List<String> photos) {

            private static List<RepairDto> fromRepairs(List<Repair> repairs, FileStorageService fileStorageService) {
                return repairs.stream()
                              .map(repair -> fromRepair(repair, fileStorageService))
                              .toList();
            }

            private static RepairDto fromRepair(Repair repair, FileStorageService fileStorageService) {
                return new RepairDto(repair.getTitle(),
                                     repair.getDescription(),
                                     repair.getCost(),
                                     fromCurrencyCodeToCurrencyName(repair.getCurrency()),
                                     repair.getRepairDate(),
                                     getRepairsUrl(repair.getPhotos(), fileStorageService));

            }

            private static String fromCurrencyCodeToCurrencyName(CurrencyCode currencyCode) {
                if (nonNull(currencyCode) && currencyCode == CurrencyCode.PLN) {
                    return "zł";
                }
                return null;
            }

            private static List<String> getRepairsUrl(List<RepairPhoto> repairPhotos, FileStorageService fileStorageService) {
                return repairPhotos.stream()
                                   .map(repairPhoto -> fileStorageService.serveFile(repairPhoto.getUuid(), REPAIRS, true))
                                   .map(response -> {
                                       try {
                                           return requireNonNull(response.getBody()).getFile()
                                                                                    .toURI()
                                                                                    .toString();
                                       } catch (IOException e) {
                                           throw new RuntimeException(e);
                                       }
                                   })
                                   .toList();
            }
        }
    }
}