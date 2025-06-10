package pl.biketrack.bike.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.biketrack.bike.dto.request.CreateBikeRequest;
import pl.biketrack.bike.dto.request.UpdateBikeRequest;
import pl.biketrack.bike.dto.response.BikeDetailsResponse;
import pl.biketrack.bike.service.BikeService;
import pl.biketrack.exception.dto.response.BaseResponse;

import java.util.UUID;

import static pl.biketrack.common.constant.Urls.BIKES_URL;

@Slf4j
@RestController
@RequestMapping(BIKES_URL)
@RequiredArgsConstructor
public class BikeController {

    private final BikeService bikeService;

    @PostMapping
    public BaseResponse createBike(@RequestBody @Valid CreateBikeRequest request) {
        return bikeService.createBike(request);
    }

    @GetMapping("/{bikeUuid}")
    public BikeDetailsResponse getBike(@PathVariable UUID bikeUuid) {
        return bikeService.getBike(bikeUuid);
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