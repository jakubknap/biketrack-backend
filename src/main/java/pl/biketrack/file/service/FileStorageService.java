package pl.biketrack.file.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import pl.biketrack.file.enumerated.FileDirectory;

import java.util.List;
import java.util.UUID;

public interface FileStorageService {

    UUID saveFile(MultipartFile file, UUID userUuid, FileDirectory fileDirectory, String fileFieldName);

    ResponseEntity<Resource> serveFile(UUID fileUuid, FileDirectory fileDirectory, boolean inline);

    void deleteFile(UUID fileUuid, FileDirectory fileDirectory);

    void deleteFiles(List<UUID> fileUuids, FileDirectory fileDirectory);
}