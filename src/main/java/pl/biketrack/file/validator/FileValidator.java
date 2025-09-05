package pl.biketrack.file.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.biketrack.exception.dto.BaseApiValidationError;
import pl.biketrack.exception.exception.CustomValidationException;
import pl.biketrack.file.enumerated.FileType;
import pl.biketrack.properties.FileStorageProperties;

import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static pl.biketrack.common.enumerated.ResponseCode.E00000;
import static pl.biketrack.file.enumerated.FileType.IMAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileValidator {

    private static final Map<FileType, List<String>> ALLOWED_EXTENSIONS = Map.of(IMAGE, List.of("jpg", "png", "gif", "jpeg", "webp"));

    private final FileStorageProperties fileStorageProperties;

    public void validate(MultipartFile file, String fileFieldName, FileType expectedFileType) {

        if (isNull(file) || file.isEmpty()) {
            log.error("The file cannot be empty");
            throw new CustomValidationException(E00000, List.of(new BaseApiValidationError(fileFieldName, "must not be empty")));
        }

        String fileType = file.getContentType();
        if (isNull(fileType) || !isValidFileType(fileType, expectedFileType)) {
            log.error("Invalid file type. Expected: [{}], got: [{}]", expectedFileType, fileType);
            throw new CustomValidationException(E00000, List.of(new BaseApiValidationError(fileFieldName, "must be a valid file type")));
        }

//        long fileSize = file.getSize();
//        Long maxFileSize = fileStorageProperties.getMaxFileSize();
//        if (fileSize > maxFileSize) {
//            log.error("File is too large. File size: [{}], max allowed size: [{}]", fileSize, maxFileSize);
//            throw new CustomValidationException(E00000, List.of(new BaseApiValidationError(fileFieldName, "file is too large")));
//        }

        String fileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(fileName);
        List<String> allowedExtensions = ALLOWED_EXTENSIONS.get(expectedFileType);
        if (isNull(fileExtension) || isNull(allowedExtensions) || !allowedExtensions.contains(fileExtension)) {
            log.error("Invalid file extension: [{}]", fileExtension);
            throw new CustomValidationException(E00000, List.of(new BaseApiValidationError(fileFieldName, "not allowed file type")));
        }
    }

    private boolean isValidFileType(String fileType, FileType expectedFileType) {
        List<String> allowedExtensions = ALLOWED_EXTENSIONS.get(expectedFileType);

        if (allowedExtensions == null) {
            return false;
        }

        if (FileType.IMAGE == expectedFileType) {
            return fileType.startsWith("image/");
        }

        return true;
    }

    private String getFileExtension(String fileName) {
        if (isNull(fileName) || !fileName.contains(".")) {
            return null;
        }

        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}