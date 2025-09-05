package pl.biketrack.file.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.biketrack.exception.dto.BaseApiValidationError;
import pl.biketrack.exception.exception.CustomValidationException;
import pl.biketrack.exception.exception.ServiceException;
import pl.biketrack.file.enumerated.FileDirectory;
import pl.biketrack.file.service.FileStorageService;
import pl.biketrack.properties.FileStorageProperties;
import pl.biketrack.security.util.SecurityUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static pl.biketrack.common.enumerated.ResponseCode.E00000;
import static pl.biketrack.common.enumerated.ResponseCode.E07000;
import static pl.biketrack.common.enumerated.ResponseCode.E07001;
import static pl.biketrack.common.enumerated.ResponseCode.E07002;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private static final Map<String, String> EXT_TO_MIME = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "gif", "image/gif",
            "webp", "image/webp"
                                                                 );

    private final FileStorageProperties fileStorageProperties;

    @Override
    public String saveFile(MultipartFile file, UUID userUuid, FileDirectory fileDirectory, String fileFieldName) {
        validateFileName(file.getOriginalFilename(), fileFieldName);
        String extension = getFileExtension(file.getOriginalFilename());

        String newFileName = UUID.randomUUID() + "." + extension;

        Path userPath = fileStorageProperties.getUploadPath()
                                             .resolve(String.valueOf(userUuid))
                                             .resolve(fileDirectory.getPath());

        try {
            Files.createDirectories(userPath);
            Path targetPath = userPath.resolve(newFileName).toAbsolutePath().normalize();
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            log.error("Error saving file: {}", ex.getMessage(), ex);
            throw new ServiceException(E07000);
        }

        return newFileName;
    }

    @Override
    public ResponseEntity<Resource> serveFile(String fileName, FileDirectory directory, boolean inline) {
        UUID loggedUserUUID = SecurityUtils.getLoggedUserUUID();
        Resource resource = loadFile(loggedUserUUID, directory, fileName);

        String contentType = detectContentType(resource);

        String disposition = (inline ? "inline" : "attachment") + "; filename=\"" + resource.getFilename() + "\"";

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(contentType))
                             .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                             .body(resource);
    }

    private void validateFileName(String originalFileName, String fileFieldName) {
        if (isBlank(originalFileName)) {
            log.error("Filename is null");
            throw new CustomValidationException(E00000, List.of(new BaseApiValidationError(fileFieldName, "file must have a name")));
        }
    }

    private Resource loadFile(UUID userUuid, FileDirectory fileDirectory, String fileName) {
        Path filePath = fileStorageProperties.getUploadPath()
                                             .resolve(userUuid.toString())
                                             .resolve(fileDirectory.getPath())
                                             .resolve(fileName)
                                             .toAbsolutePath()
                                             .normalize();

        if (!filePath.startsWith(fileStorageProperties.getUploadPath()
                                                      .resolve(userUuid.toString()))) {
            log.error("Invalid file path detected: {}", filePath);
            throw new ServiceException(E07002);
        }

        if (!Files.exists(filePath)) {
            log.error("File not found: [{}]", filePath);
            throw new ServiceException(E07001);
        }

        try {
            return new UrlResource(filePath.toUri());
        } catch (Exception ex) {
            log.error("Error loading file: {}", ex.getMessage(), ex);
            throw new ServiceException(E07002);
        }
    }

    private String detectContentType(Resource resource) {
        try {
            Path filePath = resource.getFile().toPath();

            String contentType = Files.probeContentType(filePath);

            if (nonNull(contentType)) {
                return contentType;
            }

        } catch (IOException ignored) { }

        String ext = getFileExtension(resource.getFilename());
        return EXT_TO_MIME.getOrDefault(ext, "application/octet-stream");
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}