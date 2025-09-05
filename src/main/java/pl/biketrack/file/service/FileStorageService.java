package pl.biketrack.file.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import pl.biketrack.file.enumerated.FileDirectory;

import java.util.UUID;

public interface FileStorageService {

    String saveFile(MultipartFile file, UUID userUuid, FileDirectory fileDirectory, String fileFieldName);

    ResponseEntity<Resource> serveFile(String fileName, FileDirectory directory, boolean inline);
}