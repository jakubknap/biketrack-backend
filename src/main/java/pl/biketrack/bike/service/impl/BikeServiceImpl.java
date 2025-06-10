package pl.biketrack.bike.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.biketrack.bike.dto.request.CreateBikeRequest;
import pl.biketrack.bike.dto.request.UpdateBikeRequest;
import pl.biketrack.bike.dto.response.BikeDetailsResponse;
import pl.biketrack.bike.model.Bike;
import pl.biketrack.bike.repository.BikeRepository;
import pl.biketrack.bike.service.BikeService;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.exception.exception.ServiceException;
import pl.biketrack.security.util.SecurityUtils;
import pl.biketrack.user.model.User;

import java.util.UUID;

import static pl.biketrack.bike.mapper.BikeMapper.buildBike;
import static pl.biketrack.bike.mapper.BikeMapper.updateBikeFromRequest;
import static pl.biketrack.common.enumerated.ResponseCode.E05000;
import static pl.biketrack.common.enumerated.ResponseCode.E05001;
import static pl.biketrack.common.enumerated.ResponseCode.S00000;
import static pl.biketrack.common.enumerated.ResponseCode.S00003;

@Slf4j
@Service
@RequiredArgsConstructor
public class BikeServiceImpl implements BikeService {

    private final BikeRepository bikeRepository;

    @Override
    public BaseResponse createBike(CreateBikeRequest request) {
        User user = SecurityUtils.getLoggedUser();
        log.info("Start the process of adding a new bike: [{}], for user with UUID: [{}]", request, user.getUuid());

        Bike bike = buildBike(request, user);
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
    public BaseResponse updateBike(UpdateBikeRequest request) {
        UUID bikeUuid = request.bikeUuid();
        log.info("Start the process of updating bike with UUID: [{}]", bikeUuid);

        Bike bike = findBikeOrElseThrow(bikeUuid);
        validateBikeOwner(bike.getUser().getUuid(), bikeUuid);

        updateBikeFromRequest(request, bike);
        bikeRepository.save(bike);

        log.info("Successfully completed the process of editing bike with UUID: [{}]", bike.getUuid());
        return new BaseResponse(S00000);
    }

    @Override
    public BaseResponse deleteBike(UUID bikeUuid) {
        log.info("Start the process of deleting bike with UUID: [{}]", bikeUuid);

        Bike bike = findBikeOrElseThrow(bikeUuid);
        validateBikeOwner(bike.getUser().getUuid(), bikeUuid);

        bikeRepository.delete(bike);

        log.info("Successfully completed the process of deleting bike with UUID: [{}]", bike.getUuid());
        return new BaseResponse(S00000);
    }

    private Bike findBikeOrElseThrow(UUID bikeUuid) {
        return bikeRepository.findBikeWithUserByUuid(bikeUuid)
                             .orElseThrow(() -> {
                                 log.error("Bike with UUID: [{}] not found", bikeUuid);
                                 return new ServiceException(E05000);
                             });
    }

    private void validateBikeOwner(UUID bikeOwnerUuid, UUID bikeUuid) {
        UUID loggedUserUuid = SecurityUtils.getLoggedUserUUID();

        if (!loggedUserUuid.equals(bikeOwnerUuid)) {
            log.error("Logged in user is not the owner of the bike. Bike UUID: [{}], Logged user UUID: [{}], Bike owner UUID: [{}]", bikeUuid, loggedUserUuid, bikeOwnerUuid);
            throw new ServiceException(E05001);
        }
    }
}