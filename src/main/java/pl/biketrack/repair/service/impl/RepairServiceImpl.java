package pl.biketrack.repair.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.biketrack.bike.model.Bike;
import pl.biketrack.bike.service.BikeService;
import pl.biketrack.common.dto.PageResponse;
import pl.biketrack.common.enumerated.ResponseCode;
import pl.biketrack.exception.dto.BaseApiValidationError;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.exception.exception.CustomValidationException;
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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static pl.biketrack.common.enumerated.ResponseCode.E00000;
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
        RepairDetailsResponse repairDetailsResponse = repairRepository.getRepairDetails(repairUuid)
                                                                      .orElseThrow(() -> {
                                                                          log.error("Repair with UUID: [{}] not found", repairUuid);
                                                                          return new ServiceException(E06000);
                                                                      });

        validateRepairOwner(repairDetailsResponse.userUuid(), repairUuid);

        return repairDetailsResponse;
    }

    @Override
    public BaseResponse updateRepair(UpdateRepairRequest request, List<MultipartFile> newPhotos, List<MultipartFile> updatedPhotos) {
        UUID repairUuid = request.repairUuid();
        log.info("Start the process of updating repair with UUID: [{}]", repairUuid);

        Repair repair = findRepairOrElseThrow(repairUuid);

        validateRepairOwner(repair.getUserUuid(), repairUuid);

        updateRepairFromRequest(repair, request);
        handlePhotos(request, newPhotos, updatedPhotos, repair);

        repairRepository.save(repair);

        log.info("Successfully completed the process of editing repair with UUID: [{}]", repairUuid);
        return new BaseResponse(S00000);
    }

    @Override
    public BaseResponse deleteRepair(UUID repairUuid) {
        log.info("Start the process of deleting repair with UUID: [{}]", repairUuid);

        Repair repair = findRepairOrElseThrow(repairUuid);

        validateRepairOwner(repair.getUserUuid(), repairUuid);

        repairRepository.delete(repair);

        log.info("Successfully completed the process of deleting repair with UUID: [{}]", repairUuid);
        return new BaseResponse(S00000);
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

    private void handlePhotos(UpdateRepairRequest request, List<MultipartFile> newPhotos, List<MultipartFile> updatedPhotos, Repair repair) {
        if (nonNull(request.deletedPhotoUuids())) {
            repair.getPhotos().removeIf(photo -> request.deletedPhotoUuids().contains(photo.getUuid()));
        }

        if (nonNull(newPhotos)) {
            if (repair.getPhotos().size() >= 10 || (repair.getPhotos().size() + newPhotos.size()) >= 10) {
                throw new CustomValidationException(E00000, List.of(new BaseApiValidationError("newPhotos", "photo limit has been reached")));
            }

            for (MultipartFile repairPhoto : newPhotos) {
                fileValidator.validate(repairPhoto, "newPhotos", FileType.IMAGE);
                UUID fileName = fileStorageService.saveFile(repairPhoto, getLoggedUserUUID(), REPAIRS, "newPhotos");

                RepairPhoto photo = new RepairPhoto();
                photo.setUuid(fileName);
                photo.setRepair(repair);

                repair.getPhotos().add(photo);
            }
        }

        if (nonNull(updatedPhotos)) {
            fileValidator.validateAll(updatedPhotos, "updatedPhotos", FileType.IMAGE);

            for (MultipartFile file : updatedPhotos) {

                String originalName = file.getOriginalFilename();
                if (isNull(originalName) || !originalName.contains("_")) {
                    throw new CustomValidationException(E00000, List.of(new BaseApiValidationError("updatedPhotos", "filename must contain uuid prefix")));
                }

                String uuidPart = originalName.substring(0, originalName.indexOf("_"));
                UUID photoUuid = UUID.fromString(uuidPart);

                RepairPhoto existingPhoto = repair.getPhotos()
                                                  .stream()
                                                  .filter(p -> p.getUuid().equals(photoUuid))
                                                  .findFirst()
                                                  .orElseThrow(() -> new CustomValidationException(E00000,
                                                                                                   List.of(new BaseApiValidationError("updatedPhotos", "photo not found"))));

                UUID newFileUuid = fileStorageService.saveFile(file, getLoggedUserUUID(), REPAIRS, "newPhotos");
                existingPhoto.setUuid(newFileUuid);
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

    private Repair findRepairOrElseThrow(UUID repairUuid) {
        return repairRepository.findRepairWithUserByUuid(repairUuid)
                               .orElseThrow(() -> {
                                   log.error("Repair with UUID: [{}] not found", repairUuid);
                                   return new ServiceException(E06000);
                               });
    }
}