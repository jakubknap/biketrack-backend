package pl.biketrack.converter;

import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PdfConverter implements DocumentConverter {

    private final SpringTemplateEngine templateEngine;

    public byte[] generatePdf(String templateName, Map<String, Object> variables) {
        try {
            log.info("Starting PDF generation from template: {}", templateName);

            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process(getTemplateDir() + templateName, context);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                HtmlConverter.convertToPdf(htmlContent, outputStream);

                log.info("PDF successfully generated ({} bytes)", outputStream.size());
                return outputStream.toByteArray();
            }

        } catch (Exception e) {
            log.error("Error while generating PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Error while generating PDF", e);
        }
    }

    @Override
    public String getTemplateDir() {
        return "report/";
    }
}