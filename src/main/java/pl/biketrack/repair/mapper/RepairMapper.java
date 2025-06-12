package pl.biketrack.repair.mapper;

import lombok.experimental.UtilityClass;
import pl.biketrack.bike.model.Bike;
import pl.biketrack.repair.dto.request.AddRepairRequest;
import pl.biketrack.repair.dto.request.UpdateRepairRequest;
import pl.biketrack.repair.model.Repair;
import pl.biketrack.user.model.User;

import java.util.UUID;

@UtilityClass
public class RepairMapper {

    public static Repair buildRepair(AddRepairRequest request, Bike bike, User user) {
        return Repair.builder()
                     .uuid(UUID.randomUUID())
                     .title(request.title())
                     .description(request.description())
                     .cost(request.cost())
                     .currency(request.currency())
                     .repairDate(request.repairDate())
                     .bike(bike)
                     .user(user)
                     .build();
    }

    public static void updateRepairFromRequest(Repair repair, UpdateRepairRequest request) {
        repair.setTitle(request.title())
              .setDescription(request.description())
              .setCost(request.cost())
              .setCurrency(repair.getCurrency())
              .setRepairDate(repair.getRepairDate());
    }
}