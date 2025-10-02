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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static pl.biketrack.common.enumerated.ResponseCode.E00000;
import static pl.biketrack.common.enumerated.ResponseCode.E07000;
import static pl.biketrack.common.enumerated.ResponseCode.E07001;
import static pl.biketrack.common.enumerated.ResponseCode.E07002;
import static pl.biketrack.common.enumerated.ResponseCode.E07003;

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
    public UUID saveFile(MultipartFile file, UUID userUuid, FileDirectory fileDirectory, String fileFieldName) {
        String originalFilename = file.getOriginalFilename();
        validateFileName(originalFilename, fileFieldName);

        String extension = getFileExtension(originalFilename);
        UUID fileUuid = UUID.randomUUID();
        String newFileName = fileUuid + "." + extension;

        Path userPath = fileStorageProperties.getUploadPath()
                                             .resolve(String.valueOf(userUuid))
                                             .resolve(fileDirectory.getPath());

        log.info("Start saving file: [{}] for user with UUID: [{}] in directory: [{}]", originalFilename, userUuid, fileDirectory.getPath());

        try {
            Files.createDirectories(userPath);
            Path targetPath = userPath.resolve(newFileName).toAbsolutePath().normalize();
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Successfully saved file: [{}] as: [{}] for user with UUID: [{}] in: [{}]", originalFilename, newFileName, userUuid, fileDirectory.getPath());
        } catch (Exception ex) {
            log.error("Error saving file: [{}] for user with UUID: [{}] in: [{}]: {}", originalFilename, userUuid, fileDirectory.getPath(), ex.getMessage(), ex);
            throw new ServiceException(E07000);
        }

        return fileUuid;
    }

    @Override
    public ResponseEntity<Resource> serveFile(UUID fileUuid, FileDirectory fileDirectory, boolean inline) {
        UUID loggedUserUUID = SecurityUtils.getLoggedUserUUID();

        log.info("Start serving file with UUID: [{}] for user with UUID: [{}] in: [{}] (mode: [{}])", fileUuid, loggedUserUUID, fileDirectory.getPath(), inline ? "inline" : "download");

        Resource resource = loadFileByUuid(loggedUserUUID, fileDirectory, fileUuid);
        String contentType = detectContentType(resource);

        log.info("Successfully loaded file: [{}] for user with UUID: [{}] in: [{}]", resource.getFilename(), loggedUserUUID, fileDirectory.getPath());

        String disposition = (inline ? "inline" : "attachment") + "; filename=\"" + resource.getFilename() + "\"";

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(contentType))
                             .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                             .body(resource);
    }

    @Override
    public void deleteFile(UUID fileUuid, FileDirectory fileDirectory) {
        UUID userUuid = SecurityUtils.getLoggedUserUUID();

        log.info("Start deleting file with UUID: [{}] for user [{}] in directory [{}]", fileUuid, userUuid, fileDirectory.getPath());

        Path userPath = fileStorageProperties.getUploadPath()
                                             .resolve(userUuid.toString())
                                             .resolve(fileDirectory.getPath());

        if (!Files.exists(userPath) || !Files.isDirectory(userPath)) {
            log.error("Directory not found for user with UUID: [{}] in: [{}]", userUuid, fileDirectory.getPath());
            throw new ServiceException(E07001);
        }

        try (Stream<Path> files = Files.list(userPath)) {
            Optional<Path> matchingFile = files.filter(path -> path.getFileName()
                                                                   .toString()
                                                                   .startsWith(fileUuid + "."))
                                               .findFirst();

            if (matchingFile.isEmpty()) {
                log.error("File not found for UUID: [{}], user with UUID: [{}] in: [{}]", fileUuid, userUuid, fileDirectory.getPath());
                throw new ServiceException(E07001);
            }

            Files.delete(matchingFile.get());
            log.info("Successfully deleted file with UUID: [{}] for user [{}] in directory [{}]", fileUuid, userUuid, fileDirectory.getPath());

        } catch (Exception ex) {
            log.error("Error deleting file for UUID: [{}], user with UUID: [{}] in: [{}]: {}", fileUuid, userUuid, fileDirectory.getPath(), ex.getMessage(), ex);
            throw new ServiceException(E07003);
        }

        log.info("Successfully deleted file: [{}]", fileUuid);
    }

    @Override
    public void deleteFiles(List<UUID> fileUuids, FileDirectory fileDirectory) {
        for (UUID uuid : fileUuids) {
            try {
                deleteFile(uuid, fileDirectory);
            } catch (ServiceException ex) {
                log.error("Could not delete file with UUID [{}] in [{}]. Skipping. Reason: {}", uuid, fileDirectory.getPath(), ex.getMessage());
            }
        }
    }

    private void validateFileName(String originalFileName, String fileFieldName) {
        if (isBlank(originalFileName)) {
            log.error("Invalid or missing original filename for field: [{}]", fileFieldName);
            throw new CustomValidationException(E00000, List.of(new BaseApiValidationError(fileFieldName, "file must have a name")));
        }
    }

    private Resource loadFileByUuid(UUID userUuid, FileDirectory fileDirectory, UUID fileUuid) {
        Path userPath = fileStorageProperties.getUploadPath()
                                             .resolve(userUuid.toString())
                                             .resolve(fileDirectory.getPath());

        if (!Files.exists(userPath) || !Files.isDirectory(userPath)) {
            log.error("Directory not found for user with UUID: [{}] in: [{}]", userUuid, fileDirectory.getPath());
            throw new ServiceException(E07001);
        }

        try (Stream<Path> files = Files.list(userPath)) {
            Optional<Path> matchingFile = files.filter(path -> path.getFileName()
                                                                   .toString()
                                                                   .startsWith(fileUuid + "."))
                                               .findFirst();

            if (matchingFile.isEmpty()) {
                log.error("File not found for UUID: [{}], user with UUID: [{}] in: [{}]", fileUuid, userUuid, fileDirectory.getPath());
                throw new ServiceException(E07001);
            }

            UrlResource resource = new UrlResource(matchingFile.get().toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.error("Could not read file for UUID: [{}], user with UUID: [{}] in: [{}]", fileUuid, userUuid, fileDirectory.getPath());
                throw new ServiceException(E07002);
            }

            return resource;
        } catch (Exception ex) {
            log.error("Error loading file for UUID: [{}], user with UUID: [{}] in: [{}]: {}", fileUuid, userUuid, fileDirectory.getPath(), ex.getMessage(), ex);
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
        } catch (Exception ex) {
            log.error("Could not detect MIME type for file: [{}]: {}", resource.getFilename(), ex.getMessage());
        }

        String ext = getFileExtension(resource.getFilename());
        return EXT_TO_MIME.getOrDefault(ext, "application/octet-stream");
    }

    private String getFileExtension(String fileName) {
        if (isBlank(fileName) || !fileName.contains(".")) {
            log.error("Invalid file name while extracting extension: [{}]", fileName);
            throw new ServiceException(E07002);
        }

        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}