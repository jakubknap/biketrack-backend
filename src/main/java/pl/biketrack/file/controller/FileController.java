package pl.biketrack.file.controller;

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

import java.util.UUID;

import static pl.biketrack.common.constant.Urls.FILES_URL;

@Slf4j
@RestController
@RequestMapping(FILES_URL)
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @GetMapping("/{fileUuid}/inline")
    public ResponseEntity<Resource> getFileInline(@PathVariable UUID fileUuid) {
        log.info("Start serving file inline with UUID: [{}]", fileUuid);
        return fileStorageService.serveFile(fileUuid, FileDirectory.BIKES, true);
    }

    @GetMapping("/{fileUuid}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID fileUuid) {
        log.info("Start downloading file with UUID: [{}]", fileUuid);
        return fileStorageService.serveFile(fileUuid, FileDirectory.BIKES, false);
    }
}