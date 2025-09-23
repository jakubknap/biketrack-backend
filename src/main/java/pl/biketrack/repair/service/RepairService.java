package pl.biketrack.repair.service;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import pl.biketrack.common.dto.PageResponse;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.repair.dto.request.AddRepairRequest;
import pl.biketrack.repair.dto.request.UpdateRepairRequest;
import pl.biketrack.repair.dto.response.RepairDetailsResponse;
import pl.biketrack.repair.dto.response.RepairListResponse;

import java.util.List;
import java.util.UUID;

public interface RepairService {

    PageResponse<RepairListResponse> getRepairList(Pageable pageable);

    BaseResponse addRepair(AddRepairRequest request, List<MultipartFile> repairPhotos);

    RepairDetailsResponse getRepair(UUID repairUuid);

    BaseResponse updateRepair(UpdateRepairRequest request, List<MultipartFile> repairPhotos);

    BaseResponse deleteRepair(UUID repairUuid);
}