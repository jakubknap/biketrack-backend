package pl.biketrack.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "application.frontend")
public class FrontendProperties {

    @NotBlank
    private String activationAccountUrl;

    public String prepareActivationLink(String activationToken) {
        return activationAccountUrl + "/" + activationToken;
    }
}