package pl.biketrack.file.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.biketrack.file.enumerated.FileDirectory;
import pl.biketrack.file.service.FileStorageService;
import pl.biketrack.openApi.file.ApiGetFileResponse;

import java.util.UUID;

import static pl.biketrack.common.constant.Urls.FILES_URL;

@Slf4j
@RestController
@RequestMapping(FILES_URL)
@RequiredArgsConstructor
@Tag(name = "Udostępnianie zdjęć", description = "Udostępnianie zdjęć użytkownika np. naprawy, roweru")
public class FileController {

    private final FileStorageService fileStorageService;

    @GetMapping("/{fileDirectory}/{fileUuid}/inline")
    @ApiGetFileResponse
    public ResponseEntity<Resource> getFileInline(@PathVariable FileDirectory fileDirectory, @PathVariable UUID fileUuid) {
        log.info("Start serving file inline with UUID: [{}]", fileUuid);
        return fileStorageService.serveFile(fileUuid, fileDirectory, true);
    }
}