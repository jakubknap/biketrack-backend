package pl.biketrack.mail;

import java.util.Map;

public interface MailMessage {

    String[] getReceivers();

    String getSubject();

    String getTemplateName();

    Map<String, Object> getVariables();
}