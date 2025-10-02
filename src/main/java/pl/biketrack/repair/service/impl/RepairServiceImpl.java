package pl.biketrack.repair.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.biketrack.bike.dto.response.BikeSelectListResponse;
import pl.biketrack.bike.model.Bike;
import pl.biketrack.bike.service.BikeService;
import pl.biketrack.common.dto.PageResponse;
import pl.biketrack.common.enumerated.ResponseCode;
import pl.biketrack.dashboard.dto.MoneyDto;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.exception.exception.ServiceException;
import pl.biketrack.file.enumerated.FileType;
import pl.biketrack.file.service.FileStorageService;
import pl.biketrack.file.validator.FileValidator;
import pl.biketrack.repair.dto.request.AddRepairRequest;
import pl.biketrack.repair.dto.request.UpdateRepairRequest;
import pl.biketrack.repair.dto.response.RepairDetailsResponse;
import pl.biketrack.repair.dto.response.RepairListResponse;
import pl.biketrack.repair.model.Repair;
import pl.biketrack.repair.model.RepairPhoto;
import pl.biketrack.repair.repository.RepairRepository;
import pl.biketrack.repair.service.RepairService;
import pl.biketrack.user.model.User;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.nonNull;
import static pl.biketrack.common.enumerated.ResponseCode.E05001;
import static pl.biketrack.common.enumerated.ResponseCode.E06000;
import static pl.biketrack.common.enumerated.ResponseCode.E06001;
import static pl.biketrack.common.enumerated.ResponseCode.S00000;
import static pl.biketrack.file.enumerated.FileDirectory.REPAIRS;
import static pl.biketrack.repair.mapper.RepairMapper.buildRepair;
import static pl.biketrack.repair.mapper.RepairMapper.updateRepairFromRequest;
import static pl.biketrack.security.util.SecurityUtils.getLoggedUser;
import static pl.biketrack.security.util.SecurityUtils.getLoggedUserUUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepairServiceImpl implements RepairService {

    private final RepairRepository repairRepository;
    private final BikeService bikeService;
    private final FileStorageService fileStorageService;
    private final FileValidator fileValidator;

    @Override
    public PageResponse<RepairListResponse> getRepairList(Pageable pageable) {
        UUID userUuid = getLoggedUserUUID();
        log.info("Start the process of getting repair list for user with UUID: [{}]", userUuid);
        Page<RepairListResponse> page = repairRepository.getRepairList(pageable, userUuid);
        return PageResponse.of(page);
    }

    @Override
    public BaseResponse addRepair(AddRepairRequest request, List<MultipartFile> repairPhotos) {
        UUID bikeUuid = request.bikeUuid();
        log.info("Start the process of adding a new repair to bike with UUID: [{}]", bikeUuid);

        Bike bike = bikeService.findBikeWithUserOrElseThrow(bikeUuid);
        validateBikeOwner(bike.getUserUuid(), bikeUuid);
        User user = getLoggedUser();

        Repair repair = buildRepair(request, bike, user);
        handleAddingPhotos(repairPhotos, repair);

        repairRepository.save(repair);

        log.info("Successfully completed the process of adding a new repair - assigned UUID: [{}], to bike with UUID: [{}]", repair.getUuid(), bikeUuid);
        return new BaseResponse(ResponseCode.S00003);
    }

    @Override
    public RepairDetailsResponse getRepair(UUID repairUuid) {
        log.info("Start the process of retrieving a repair details for repair with UUID: [{}]", repairUuid);
        Repair repair = findRepairWithUserAndBikeOrElseThrow(repairUuid);

        validateRepairOwner(repair.getUserUuid(), repairUuid);

        Bike bike = repair.getBike();
        List<UUID> repairPhotos = repair.getPhotos()
                                        .stream()
                                        .map(RepairPhoto::getUuid)
                                        .toList();

        return new RepairDetailsResponse(repair.getUuid(),
                                         new BikeSelectListResponse(bike.getUuid(), bike.getName()),
                                         repair.getTitle(),
                                         repair.getDescription(),
                                         new MoneyDto(repair.getCost(), repair.getCurrency()),
                                         repair.getRepairDate(),
                                         repair.getCreatedDate(),
                                         repair.getLastModifiedDate(),
                                         repairPhotos);
    }

    @Override
    public BaseResponse updateRepair(UpdateRepairRequest request, List<MultipartFile> repairPhotos) {
        UUID repairUuid = request.repairUuid();
        log.info("Start the process of updating repair with UUID: [{}]", repairUuid);

        Repair repair = findRepairOrElseThrow(repairUuid);

        validateRepairOwner(repair.getUserUuid(), repairUuid);

        updateRepairFromRequest(repair, request);

        handlePhotos(repairPhotos, repair);

        repairRepository.save(repair);

        log.info("Successfully completed the process of editing repair with UUID: [{}]", repairUuid);
        return new BaseResponse(S00000);
    }

    @Override
    public BaseResponse deleteRepair(UUID repairUuid) {
        log.info("Start the process of deleting repair with UUID: [{}]", repairUuid);

        Repair repair = findRepairOrElseThrow(repairUuid);

        validateRepairOwner(repair.getUserUuid(), repairUuid);

        List<UUID> repairPhotosUuids = repair.getPhotos()
                                             .stream()
                                             .map(RepairPhoto::getUuid)
                                             .toList();
        fileStorageService.deleteFiles(repairPhotosUuids, REPAIRS);
        repair.getPhotos().clear();

        repairRepository.delete(repair);

        log.info("Successfully completed the process of deleting repair with UUID: [{}]", repairUuid);
        return new BaseResponse(S00000);
    }

    @Override
    public List<UUID> getRepairPhotos(UUID repairUuid) {
        log.info("Start the process of retrieving repair photos for repair with UUID: [{}]", repairUuid);

        Repair repair = findRepairOrElseThrow(repairUuid);
        validateRepairOwner(repair.getUserUuid(), repairUuid);

        return repair.getPhotos()
                     .stream()
                     .map(RepairPhoto::getUuid)
                     .toList();
    }

    private void validateBikeOwner(UUID bikeOwnerUuid, UUID bikeUuid) {
        UUID loggedUserUuid = getLoggedUserUUID();

        if (!loggedUserUuid.equals(bikeOwnerUuid)) {
            log.error("Logged in user is not the owner of the bike. Bike UUID: [{}], Logged user UUID: [{}], Bike owner UUID: [{}]", bikeUuid, loggedUserUuid, bikeOwnerUuid);
            throw new ServiceException(E05001);
        }
    }

    private void handleAddingPhotos(List<MultipartFile> repairPhotos, Repair repair) {
        if (nonNull(repairPhotos) && !repairPhotos.isEmpty()) {
            fileValidator.validateAll(repairPhotos, "repairPhotos", FileType.IMAGE);

            for (MultipartFile repairPhoto : repairPhotos) {
                UUID photoUuid = fileStorageService.saveFile(repairPhoto, getLoggedUserUUID(), REPAIRS, "repairPhotos");

                RepairPhoto photo = new RepairPhoto();
                photo.setUuid(photoUuid);
                photo.setRepair(repair);

                repair.getPhotos().add(photo);
            }
        }
    }

    private void validateRepairOwner(UUID repairOwnerUuid, UUID repairUuid) {
        UUID loggedUserUuid = getLoggedUserUUID();

        if (!loggedUserUuid.equals(repairOwnerUuid)) {
            log.error("Logged in user is not the owner of the repair. Repair UUID: [{}], Logged user UUID: [{}], Repair owner UUID: [{}]", repairUuid, loggedUserUuid, repairOwnerUuid);
            throw new ServiceException(E06001);
        }
    }

    private void handlePhotos(List<MultipartFile> repairPhotos, Repair repair) {
        List<UUID> repairPhotosUuids = repair.getPhotos()
                                             .stream()
                                             .map(RepairPhoto::getUuid)
                                             .toList();

        fileStorageService.deleteFiles(repairPhotosUuids, REPAIRS);
        repair.getPhotos().clear();

        handleAddingPhotos(repairPhotos, repair);
    }

    private Repair findRepairOrElseThrow(UUID repairUuid) {
        return repairRepository.findRepairWithUserByUuid(repairUuid)
                               .orElseThrow(() -> {
                                   log.error("Repair with UUID: [{}] not found", repairUuid);
                                   return new ServiceException(E06000);
                               });
    }

    private Repair findRepairWithUserAndBikeOrElseThrow(UUID repairUuid) {
        return repairRepository.findRepairWithUserAndBikeByUuid(repairUuid)
                               .orElseThrow(() -> {
                                   log.error("Repair with UUID: [{}] not found", repairUuid);
                                   return new ServiceException(E06000);
                               });
    }
}