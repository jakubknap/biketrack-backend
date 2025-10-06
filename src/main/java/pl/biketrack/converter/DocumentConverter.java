package pl.biketrack.converter;

import java.util.Map;

public interface DocumentConverter {

    byte[] generatePdf(String templateName, Map<String, Object> variables);

    String getTemplateDir();
}