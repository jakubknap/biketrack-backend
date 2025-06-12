package pl.biketrack.repair.service;

import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.repair.dto.request.AddRepairRequest;
import pl.biketrack.repair.dto.request.UpdateRepairRequest;
import pl.biketrack.repair.dto.response.RepairDetailsResponse;

import java.util.UUID;

public interface RepairService {

    BaseResponse addRepair(AddRepairRequest request);

    RepairDetailsResponse getRepair(UUID repairUuid);

    BaseResponse updateRepair(UpdateRepairRequest request);

    BaseResponse deleteRepair(UUID repairUuid);
}