package pl.biketrack.bike.service;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import pl.biketrack.bike.dto.request.CreateBikeRequest;
import pl.biketrack.bike.dto.request.UpdateBikeRequest;
import pl.biketrack.bike.dto.response.BikeDetailsResponse;
import pl.biketrack.bike.dto.response.BikeListResponse;
import pl.biketrack.bike.dto.response.BikeRepairResponse;
import pl.biketrack.bike.dto.response.BikeRepairStatisticsResponse;
import pl.biketrack.bike.model.Bike;
import pl.biketrack.common.dto.PageResponse;
import pl.biketrack.exception.dto.response.BaseResponse;

import java.util.UUID;

public interface BikeService {

    PageResponse<BikeListResponse> getBikeList(Pageable pageable);

    BaseResponse createBike(CreateBikeRequest request, MultipartFile bikePhoto);

    BikeDetailsResponse getBike(UUID bikeUuid);

    PageResponse<BikeRepairResponse> getBikeRepairs(UUID bikeUuid, Pageable pageable);

    BikeRepairStatisticsResponse getBikeStatistics(UUID bikeUuid);

    BaseResponse updateBike(UpdateBikeRequest request);

    BaseResponse deleteBike(UUID bikeUuid);

    Bike findBikeWithUserOrElseThrow(UUID bikeUuid);
}