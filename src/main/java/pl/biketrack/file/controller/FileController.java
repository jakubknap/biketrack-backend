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

import static pl.biketrack.common.constant.Urls.FILES_URL;

@Slf4j
@RestController
@RequestMapping(FILES_URL)
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @GetMapping("/images/{fileName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        log.info("Get image for file: {}", fileName);
        return fileStorageService.serveFile(fileName, FileDirectory.BIKES, true);
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        log.info("Download file: {}", fileName);
        return fileStorageService.serveFile(fileName, FileDirectory.BIKES, false);
    }
}