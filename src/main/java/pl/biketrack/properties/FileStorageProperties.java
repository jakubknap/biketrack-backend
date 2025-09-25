package pl.biketrack.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "application.file")
public class FileStorageProperties {

    @NotBlank
    private String uploadPath;

    @NotNull
    private Long maxFileSize;

    @NotNull
    private Long maxTotalSize;

    @NotNull
    private Integer maxFilesPerRequest;

    public Path getUploadPath() {
        return Paths.get(uploadPath).toAbsolutePath().normalize();
    }
}