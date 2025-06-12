package pl.biketrack.repair.service;

import org.springframework.data.domain.Pageable;
import pl.biketrack.common.dto.PageResponse;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.repair.dto.request.AddRepairRequest;
import pl.biketrack.repair.dto.request.UpdateRepairRequest;
import pl.biketrack.repair.dto.response.RepairDetailsResponse;
import pl.biketrack.repair.dto.response.RepairListResponse;

import java.util.UUID;

public interface RepairService {

    PageResponse<RepairListResponse> getRepairList(Pageable pageable);

    BaseResponse addRepair(AddRepairRequest request);

    RepairDetailsResponse getRepair(UUID repairUuid);

    BaseResponse updateRepair(UpdateRepairRequest request);

    BaseResponse deleteRepair(UUID repairUuid);
}