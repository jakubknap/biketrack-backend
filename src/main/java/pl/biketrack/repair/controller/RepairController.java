package pl.biketrack.repair.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.biketrack.common.dto.PageResponse;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.openApi.repair.ApiAddRepairResponse;
import pl.biketrack.openApi.repair.ApiDeleteRepairResponse;
import pl.biketrack.openApi.repair.ApiGetRepairDetailsResponse;
import pl.biketrack.openApi.repair.ApiGetRepairListResponse;
import pl.biketrack.openApi.repair.ApiGetRepairPhotosResponse;
import pl.biketrack.openApi.repair.ApiUpdateRepairResponse;
import pl.biketrack.repair.dto.request.AddRepairRequest;
import pl.biketrack.repair.dto.request.UpdateRepairRequest;
import pl.biketrack.repair.dto.response.RepairDetailsResponse;
import pl.biketrack.repair.dto.response.RepairListResponse;
import pl.biketrack.repair.service.RepairService;

import java.util.List;
import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static pl.biketrack.common.constant.Urls.REPAIRS_URL;

@Slf4j
@RestController
@RequestMapping(REPAIRS_URL)
@RequiredArgsConstructor
@Tag(name = "Naprawy", description = "Zarządzanie naprawami rowerów")
public class RepairController {

    private final RepairService repairService;

    @GetMapping
    @ApiGetRepairListResponse
    public PageResponse<RepairListResponse> getRepairList(@PageableDefault(size = 2, sort = "createdDate", direction = DESC) Pageable pageable) {
        return repairService.getRepairList(pageable);
    }

    @PostMapping
    @ApiAddRepairResponse
    public BaseResponse addRepair(@RequestPart("repairData") @Valid AddRepairRequest request,
                                  @RequestPart(value = "repairPhotos", required = false) List<MultipartFile> repairPhotos) {
        return repairService.addRepair(request, repairPhotos);
    }

    @GetMapping("/{repairUuid}")
    @ApiGetRepairDetailsResponse
    public RepairDetailsResponse getRepair(@PathVariable UUID repairUuid) {
        return repairService.getRepair(repairUuid);
    }

    @PutMapping
    @ApiUpdateRepairResponse
    public BaseResponse updateRepair(@RequestPart("repairData") @Valid UpdateRepairRequest request,
                                     @RequestPart(value = "repairPhotos", required = false) List<MultipartFile> repairPhotos) {
        return repairService.updateRepair(request, repairPhotos);
    }

    @DeleteMapping("/{repairUuid}")
    @ApiDeleteRepairResponse
    public BaseResponse deleteRepair(@PathVariable UUID repairUuid) {
        return repairService.deleteRepair(repairUuid);
    }

    @GetMapping("/{repairUuid}/photos")
    @ApiGetRepairPhotosResponse
    public List<UUID> getRepairPhotos(@PathVariable UUID repairUuid) {
        return repairService.getRepairPhotos(repairUuid);
    }
}