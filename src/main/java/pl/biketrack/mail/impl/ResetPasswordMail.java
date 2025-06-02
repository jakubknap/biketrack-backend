package pl.biketrack.mail.impl;

import pl.biketrack.mail.MailMessage;

import java.util.Map;

public record ResetPasswordMail(String email, String nickname, String activationLink) implements MailMessage {

    @Override
    public String[] getReceivers() {
        return new String[]{email};
    }

    @Override
    public String getSubject() {
        return "BikeTrack - Resetowanie hasła";
    }

    @Override
    public String getTemplateName() {
        return "password-reset";
    }

    @Override
    public Map<String, Object> getVariables() {
        return Map.of("nickname", nickname,
                      "resetPasswordLink", activationLink);
    }
}