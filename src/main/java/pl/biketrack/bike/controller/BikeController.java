package pl.biketrack.bike.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.biketrack.bike.dto.request.CreateBikeRequest;
import pl.biketrack.bike.dto.request.UpdateBikeRequest;
import pl.biketrack.bike.dto.response.BikeDetailsResponse;
import pl.biketrack.bike.dto.response.BikeListResponse;
import pl.biketrack.bike.dto.response.BikeRepairResponse;
import pl.biketrack.bike.dto.response.BikeRepairStatisticsResponse;
import pl.biketrack.bike.service.BikeService;
import pl.biketrack.common.dto.PageResponse;
import pl.biketrack.exception.dto.response.BaseResponse;

import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static pl.biketrack.common.constant.Urls.BIKES_URL;

@Slf4j
@RestController
@RequestMapping(BIKES_URL)
@RequiredArgsConstructor
public class BikeController {

    private final BikeService bikeService;

    @GetMapping
    public PageResponse<BikeListResponse> getBikeList(@PageableDefault(size = 2, sort = "createdDate", direction = DESC) Pageable pageable) {
        return bikeService.getBikeList(pageable);
    }

    @PostMapping
    public BaseResponse createBike(@RequestPart("bikeData") @Valid CreateBikeRequest request,
                                   @RequestPart(value = "bikePhoto", required = false) MultipartFile bikePhoto) {
        return bikeService.createBike(request, bikePhoto);
    }

    @GetMapping("/{bikeUuid}")
    public BikeDetailsResponse getBike(@PathVariable UUID bikeUuid) {
        return bikeService.getBike(bikeUuid);
    }

    @GetMapping("/{bikeUuid}/repairs")
    public PageResponse<BikeRepairResponse> getBikeRepairs(@PathVariable UUID bikeUuid,
                                                           @PageableDefault(size = 2, sort = "createdDate", direction = DESC) Pageable pageable) {
        return bikeService.getBikeRepairs(bikeUuid, pageable);
    }

    @GetMapping("/{bikeUuid}/statistics")
    public BikeRepairStatisticsResponse getBikeStatistics(@PathVariable UUID bikeUuid) {
        return bikeService.getBikeStatistics(bikeUuid);
    }

    @PutMapping
    public BaseResponse updateBike(@RequestBody @Valid UpdateBikeRequest request) {
        return bikeService.updateBike(request);
    }

    @DeleteMapping("/{bikeUuid}")
    public BaseResponse deleteBike(@PathVariable UUID bikeUuid) {
        return bikeService.deleteBike(bikeUuid);
    }
}