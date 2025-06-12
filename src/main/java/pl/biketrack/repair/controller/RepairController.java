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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.biketrack.common.dto.PageResponse;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.repair.dto.request.AddRepairRequest;
import pl.biketrack.repair.dto.request.UpdateRepairRequest;
import pl.biketrack.repair.dto.response.RepairDetailsResponse;
import pl.biketrack.repair.dto.response.RepairListResponse;
import pl.biketrack.repair.service.RepairService;

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
    public BaseResponse addRepair(@RequestBody @Valid AddRepairRequest request) {
        return repairService.addRepair(request);
    }

    @GetMapping("/{repairUuid}")
    public RepairDetailsResponse getRepair(@PathVariable UUID repairUuid) {
        return repairService.getRepair(repairUuid);
    }

    @PutMapping
    public BaseResponse updateRepair(@RequestBody @Valid UpdateRepairRequest request) {
        return repairService.updateRepair(request);
    }

    @DeleteMapping("/{repairUuid}")
    public BaseResponse deleteRepair(@PathVariable UUID repairUuid) {
        return repairService.deleteRepair(repairUuid);
    }
}