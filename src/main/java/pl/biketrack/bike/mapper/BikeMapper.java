package pl.biketrack.bike.mapper;

import lombok.experimental.UtilityClass;
import pl.biketrack.bike.dto.request.CreateBikeRequest;
import pl.biketrack.bike.dto.request.UpdateBikeRequest;
import pl.biketrack.bike.model.Bike;
import pl.biketrack.user.model.User;

import java.util.UUID;

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
}