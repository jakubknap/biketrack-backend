package pl.biketrack.bike.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.biketrack.bike.dto.request.CreateBikeRequest;
import pl.biketrack.bike.dto.request.UpdateBikeRequest;
import pl.biketrack.bike.dto.response.BikeDetailsResponse;
import pl.biketrack.bike.dto.response.BikeListResponse;
import pl.biketrack.bike.dto.response.BikeRepairResponse;
import pl.biketrack.bike.dto.response.BikeRepairStatisticsResponse;
import pl.biketrack.bike.dto.response.BikeSelectListResponse;
import pl.biketrack.bike.model.Bike;
import pl.biketrack.bike.repository.BikeRepository;
import pl.biketrack.bike.service.BikeService;
import pl.biketrack.common.dto.PageResponse;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.exception.exception.ServiceException;
import pl.biketrack.file.enumerated.FileType;
import pl.biketrack.file.service.FileStorageService;
import pl.biketrack.file.validator.FileValidator;
import pl.biketrack.repair.dto.RepairStatisticsDto;
import pl.biketrack.repair.repository.RepairRepository;
import pl.biketrack.security.util.SecurityUtils;
import pl.biketrack.user.model.User;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.nonNull;
import static pl.biketrack.bike.mapper.BikeMapper.buildBike;
import static pl.biketrack.bike.mapper.BikeMapper.mapToBikeRepairStatisticsResponse;
import static pl.biketrack.bike.mapper.BikeMapper.updateBikeFromRequest;
import static pl.biketrack.common.enumerated.ResponseCode.E05000;
import static pl.biketrack.common.enumerated.ResponseCode.E05001;
import static pl.biketrack.common.enumerated.ResponseCode.S00000;
import static pl.biketrack.common.enumerated.ResponseCode.S00003;
import static pl.biketrack.file.enumerated.FileDirectory.BIKES;

@Slf4j
@Service
@RequiredArgsConstructor
public class BikeServiceImpl implements BikeService {

    private final BikeRepository bikeRepository;
    private final RepairRepository repairRepository;
    private final FileValidator fileValidator;
    private final FileStorageService fileStorageService;

    @Override
    public PageResponse<BikeListResponse> getBikeList(Pageable pageable) {
        UUID userUuid = SecurityUtils.getLoggedUserUUID();
        log.info("Start the process of getting bike list for user with UUID: [{}]", userUuid);

        Page<BikeListResponse> bikeList = bikeRepository.getBikeList(pageable, userUuid);

        return PageResponse.of(bikeList);
    }

    @Override
    public List<BikeSelectListResponse> getUserBikes() {
        UUID userUuid = SecurityUtils.getLoggedUserUUID();
        return bikeRepository.getBikeList(userUuid);
    }

    @Override
    public BaseResponse createBike(CreateBikeRequest request, MultipartFile bikePhoto) {
        User user = SecurityUtils.getLoggedUser();
        log.info("Start the process of adding a new bike: [{}], for user with UUID: [{}]", request, user.getUuid());

        Bike bike = buildBike(request, user);
        handleAddingPhoto(bikePhoto, user, bike);

        bikeRepository.save(bike);

        log.info("Successfully completed the process of adding a new bike - assigned UUID: [{}], for user with UUID: [{}]", bike.getUuid(), user.getUuid());

        return new BaseResponse(S00003);
    }

    @Override
    public BikeDetailsResponse getBike(UUID bikeUuid) {
        log.info("Start the process of retrieving a bike details for bike with UUID: [{}]", bikeUuid);

        BikeDetailsResponse bikeDetailsResponse = bikeRepository.getBikeDetails(bikeUuid)
                                                                .orElseThrow(() -> {
                                                                    log.error("Bike with UUID: [{}] not found", bikeUuid);
                                                                    return new ServiceException(E05000);
                                                                });

        validateBikeOwner(bikeDetailsResponse.userUuid(), bikeUuid);

        return bikeDetailsResponse;
    }

    @Override
    public PageResponse<BikeRepairResponse> getBikeRepairs(UUID bikeUuid, Pageable pageable) {
        log.info("Start the process of getting repairs for bike with UUID: [{}]", bikeUuid);

        Bike bike = findBikeWithUserOrElseThrow(bikeUuid);

        validateBikeOwner(bike.getUserUuid(), bikeUuid);

        Page<BikeRepairResponse> repairs = repairRepository.getRepairsByBike(pageable, bikeUuid);

        return PageResponse.of(repairs);
    }

    @Override
    public BikeRepairStatisticsResponse getBikeStatistics(UUID bikeUuid) {
        log.info("Start the process of retrieving a bike statistics for bike with UUID: [{}]", bikeUuid);

        Bike bike = findBikeWithUserOrElseThrow(bikeUuid);

        validateBikeOwner(bike.getUserUuid(), bikeUuid);

        List<RepairStatisticsDto> repairStatisticsDto = repairRepository.getRepairStatisticsDtoForBike(bikeUuid);

        return mapToBikeRepairStatisticsResponse(repairStatisticsDto);
    }

    @Override
    public BaseResponse updateBike(UpdateBikeRequest request, MultipartFile bikePhoto) {
        UUID bikeUuid = request.bikeUuid();
        log.info("Start the process of updating bike with UUID: [{}]", bikeUuid);

        Bike bike = findBikeWithUserOrElseThrow(bikeUuid);

        validateBikeOwner(bike.getUserUuid(), bikeUuid);

        updateBikeFromRequest(request, bike);
        bikeRepository.save(bike);

        log.info("Successfully completed the process of editing bike with UUID: [{}]", bike.getUuid());
        return new BaseResponse(S00000);
    }

    @Override
    public BaseResponse deleteBike(UUID bikeUuid) {
        log.info("Start the process of deleting bike with UUID: [{}]", bikeUuid);

        Bike bike = findBikeWithUserOrElseThrow(bikeUuid);

        validateBikeOwner(bike.getUserUuid(), bikeUuid);

        bikeRepository.delete(bike);

        log.info("Successfully completed the process of deleting bike with UUID: [{}]", bike.getUuid());
        return new BaseResponse(S00000);
    }

    @Override
    public Bike findBikeWithUserOrElseThrow(UUID bikeUuid) {
        return bikeRepository.findBikeWithUserByUuid(bikeUuid)
                             .orElseThrow(() -> {
                                 log.error("Bike with UUID: [{}] not found", bikeUuid);
                                 return new ServiceException(E05000);
                             });
    }

    private void handleAddingPhoto(MultipartFile bikePhoto, User user, Bike bike) {
        if (nonNull(bikePhoto) && !bikePhoto.isEmpty()) {
            fileValidator.validate(bikePhoto, "bikePhoto", FileType.IMAGE);
            UUID fileName = fileStorageService.saveFile(bikePhoto, user.getUuid(), BIKES, "bikePhoto");
            bike.setPhotoUuid(fileName);
        }
    }

    private void validateBikeOwner(UUID bikeOwnerUuid, UUID bikeUuid) {
        UUID loggedUserUuid = SecurityUtils.getLoggedUserUUID();

        if (!loggedUserUuid.equals(bikeOwnerUuid)) {
            log.error("Logged in user is not the owner of the bike. Bike UUID: [{}], Logged user UUID: [{}], Bike owner UUID: [{}]", bikeUuid, loggedUserUuid, bikeOwnerUuid);
            throw new ServiceException(E05001);
        }
    }
}