package pl.biketrack.repair.controller;

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
public class RepairController {

    private final RepairService repairService;

    @GetMapping
    public PageResponse<RepairListResponse> getRepairList(@PageableDefault(size = 2, sort = "createdDate", direction = DESC) Pageable pageable) {
        return repairService.getRepairList(pageable);
    }

    @PostMapping
    public BaseResponse addRepair(@RequestPart("repairData") @Valid AddRepairRequest request,
                                  @RequestPart(value = "repairPhotos", required = false) List<MultipartFile> repairPhotos) {
        return repairService.addRepair(request, repairPhotos);
    }

    @GetMapping("/{repairUuid}")
    public RepairDetailsResponse getRepair(@PathVariable UUID repairUuid) {
        return repairService.getRepair(repairUuid);
    }

    @PutMapping
    public BaseResponse updateRepair(@RequestPart("repairData") @Valid UpdateRepairRequest request,
                                     @RequestPart(value = "newPhotos", required = false) List<MultipartFile> newPhotos,
                                     @RequestPart(value = "updatedPhotos", required = false) List<MultipartFile> updatedPhotos) {
        return repairService.updateRepair(request, newPhotos, updatedPhotos);
    }

    @DeleteMapping("/{repairUuid}")
    public BaseResponse deleteRepair(@PathVariable UUID repairUuid) {
        return repairService.deleteRepair(repairUuid);
    }
}