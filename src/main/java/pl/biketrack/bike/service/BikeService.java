package pl.biketrack.bike.service;

import pl.biketrack.bike.dto.request.CreateBikeRequest;
import pl.biketrack.bike.dto.request.UpdateBikeRequest;
import pl.biketrack.bike.dto.response.BikeDetailsResponse;
import pl.biketrack.exception.dto.response.BaseResponse;

import java.util.UUID;

public interface BikeService {

    BaseResponse createBike(CreateBikeRequest request);

    BikeDetailsResponse getBike(UUID bikeUuid);

    BaseResponse updateBike(UpdateBikeRequest request);

    BaseResponse deleteBike(UUID bikeUuid);
}