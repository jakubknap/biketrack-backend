package pl.biketrack.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "application.mail")
public class MailProperties {

    @NotNull
    private boolean isEnabled;

    @NotBlank
    private String senderMail;

    public boolean isMailingDisabled() {
        return !isEnabled;
    }
}