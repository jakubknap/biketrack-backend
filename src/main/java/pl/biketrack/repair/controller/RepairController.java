package pl.biketrack.repair.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.repair.dto.request.AddRepairRequest;
import pl.biketrack.repair.dto.request.UpdateRepairRequest;
import pl.biketrack.repair.dto.response.RepairDetailsResponse;
import pl.biketrack.repair.service.RepairService;

import java.util.UUID;

import static pl.biketrack.common.constant.Urls.REPAIRS_URL;

@Slf4j
@RestController
@RequestMapping(REPAIRS_URL)
@RequiredArgsConstructor
public class RepairController {

    private final RepairService repairService;

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