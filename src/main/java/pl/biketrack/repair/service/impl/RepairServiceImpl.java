package pl.biketrack.repair.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.biketrack.bike.model.Bike;
import pl.biketrack.bike.service.BikeService;
import pl.biketrack.common.dto.PageResponse;
import pl.biketrack.common.enumerated.ResponseCode;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.exception.exception.ServiceException;
import pl.biketrack.repair.dto.request.AddRepairRequest;
import pl.biketrack.repair.dto.request.UpdateRepairRequest;
import pl.biketrack.repair.dto.response.RepairDetailsResponse;
import pl.biketrack.repair.dto.response.RepairListResponse;
import pl.biketrack.repair.model.Repair;
import pl.biketrack.repair.repository.RepairRepository;
import pl.biketrack.repair.service.RepairService;
import pl.biketrack.security.util.SecurityUtils;
import pl.biketrack.user.model.User;

import java.util.UUID;

import static pl.biketrack.common.enumerated.ResponseCode.E06000;
import static pl.biketrack.common.enumerated.ResponseCode.E06001;
import static pl.biketrack.common.enumerated.ResponseCode.S00000;
import static pl.biketrack.repair.mapper.RepairMapper.buildRepair;
import static pl.biketrack.repair.mapper.RepairMapper.updateRepairFromRequest;
import static pl.biketrack.security.util.SecurityUtils.getLoggedUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepairServiceImpl implements RepairService {

    private final RepairRepository repairRepository;
    private final BikeService bikeService;

    @Override
    public PageResponse<RepairListResponse> getRepairList(Pageable pageable) {
        UUID userUuid = SecurityUtils.getLoggedUserUUID();
        log.info("Start the process of getting repair list for user with UUID: [{}]", userUuid);
        Page<RepairListResponse> page = repairRepository.getRepairList(pageable, userUuid);
        return PageResponse.of(page);
    }

    @Override
    public BaseResponse addRepair(AddRepairRequest request) {
        UUID bikeUuid = request.bikeUuid();
        log.info("Start the process of adding a new repair to bike with UUID: [{}]", bikeUuid);

        Bike bike = bikeService.findBikeWithUserOrElseThrow(bikeUuid);
        User user = getLoggedUser();

        Repair repair = buildRepair(request, bike, user);
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
    public BaseResponse updateRepair(UpdateRepairRequest request) {
        UUID repairUuid = request.repairUuid();
        log.info("Start the process of updating repair with UUID: [{}]", repairUuid);

        Repair repair = findRepairOrElseThrow(repairUuid);

        validateRepairOwner(repair.getUser().getUuid(), repairUuid);

        updateRepairFromRequest(repair, request);
        repairRepository.save(repair);

        log.info("Successfully completed the process of editing repair with UUID: [{}]", repairUuid);
        return new BaseResponse(S00000);
    }

    @Override
    public BaseResponse deleteRepair(UUID repairUuid) {
        log.info("Start the process of deleting repair with UUID: [{}]", repairUuid);

        Repair repair = findRepairOrElseThrow(repairUuid);

        validateRepairOwner(repair.getUser().getUuid(), repairUuid);

        repairRepository.delete(repair);

        log.info("Successfully completed the process of deleting repair with UUID: [{}]", repairUuid);
        return new BaseResponse(S00000);
    }

    private void validateRepairOwner(UUID repairOwnerUuid, UUID repairUuid) {
        UUID loggedUserUuid = SecurityUtils.getLoggedUserUUID();

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