package pl.biketrack.mail.impl;

import pl.biketrack.mail.MailMessage;

import java.util.Map;

public record AccountActivationMail(String email, String nickname, String activationLink) implements MailMessage {

    @Override
    public String[] getReceivers() {
        return new String[]{email};
    }

    @Override
    public String getSubject() {
        return "BikeTrack - Aktywacja konta";
    }

    @Override
    public String getTemplateName() {
        return "account-activation";
    }

    @Override
    public Map<String, Object> getVariables() {
        return Map.of("nickname", nickname,
                      "activationLink", activationLink);
    }
}