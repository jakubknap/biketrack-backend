package pl.biketrack.bike.mapper;

import com.neovisionaries.i18n.CurrencyCode;
import lombok.experimental.UtilityClass;
import pl.biketrack.bike.dto.request.CreateBikeRequest;
import pl.biketrack.bike.dto.request.UpdateBikeRequest;
import pl.biketrack.bike.dto.response.BikeRepairStatisticsResponse;
import pl.biketrack.bike.model.Bike;
import pl.biketrack.repair.dto.RepairStatisticsDto;
import pl.biketrack.user.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;

import static pl.biketrack.repair.dto.RepairStatisticsDto.getAverageRepairCost;
import static pl.biketrack.repair.dto.RepairStatisticsDto.getFirstRepairDate;
import static pl.biketrack.repair.dto.RepairStatisticsDto.getLastRepairDate;
import static pl.biketrack.repair.dto.RepairStatisticsDto.getRepairsCurrency;
import static pl.biketrack.repair.dto.RepairStatisticsDto.getRepairsInYear;
import static pl.biketrack.repair.dto.RepairStatisticsDto.getTotalRepairCost;
import static pl.biketrack.repair.dto.RepairStatisticsDto.getTotalRepairs;

@UtilityClass
public class BikeMapper {

    public static Bike buildBike(CreateBikeRequest request, User user) {
        return Bike.builder()
                   .uuid(UUID.randomUUID())
                   .name(request.name())
                   .brand(request.brand())
                   .model(request.model())
                   .type(request.type())
                   .purchaseDate(request.purchaseDate())
                   .serialNumber(request.serialNumber())
                   .mileageKm(request.mileageKm())
                   .description(request.description())
                   .user(user)
                   .build();
    }

    public static void updateBikeFromRequest(UpdateBikeRequest request, Bike bike) {
        bike.setName(request.name())
            .setBrand(request.brand())
            .setModel(request.model())
            .setType(request.type())
            .setPurchaseDate(request.purchaseDate())
            .setSerialNumber(request.serialNumber())
            .setMileageKm(request.mileageKm())
            .setDescription(request.description());
    }

    public static BikeRepairStatisticsResponse mapToBikeRepairStatisticsResponse(List<RepairStatisticsDto> repairStatisticsDtoList) {
        long totalRepairs = getTotalRepairs(repairStatisticsDtoList);
        BigDecimal totalRepairCost = getTotalRepairCost(repairStatisticsDtoList);
        CurrencyCode repairsCurrency = getRepairsCurrency(repairStatisticsDtoList);
        LocalDateTime dateOfLastRepair = getLastRepairDate(repairStatisticsDtoList);
        LocalDateTime dateOfFirstRepair = getFirstRepairDate(repairStatisticsDtoList);
        BigDecimal averageRepairCost = getAverageRepairCost(totalRepairs, totalRepairCost);
        long repairsInYear = getRepairsInYear(repairStatisticsDtoList, Year.now().getValue());

        return new BikeRepairStatisticsResponse(totalRepairs,
                                                totalRepairCost,
                                                repairsCurrency,
                                                dateOfLastRepair,
                                                dateOfFirstRepair,
                                                averageRepairCost,
                                                repairsInYear);
    }
}