package pl.biketrack.mail.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.biketrack.mail.MailMessage;
import pl.biketrack.properties.MailProperties;
import pl.biketrack.util.MaskingUtil;

import java.util.Arrays;
import java.util.Objects;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private static final String MAIL_SUCCESSFULLY_SENT = "Mail successfully sent";
    private static final String ERROR_WHILE_SENDING_MAIL = "Error while sending mail: {}";

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;
    private final TemplateEngine templateEngine;

    @Async
    public void sendMail(MailMessage message) {
        sendHtml(message);
    }

    @Async
    public void sendPlainMail(String receiverEmail, String subject, String body) {
        sendPlain(new String[]{receiverEmail}, subject, body);
    }

    private void sendHtml(MailMessage message) {
        String[] to = message.getReceivers();
        String subject = message.getSubject();

        if (!isReadyToSend(to, subject)) {
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(mailProperties.getSenderMail());
            helper.setTo(to);
            mimeMessage.setSubject(subject);

            Context context = new Context();
            context.setVariables(message.getVariables());

            String htmlContent = templateEngine.process(message.getTemplateName(), context);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

            log.info(MAIL_SUCCESSFULLY_SENT);
        } catch (Exception ex) {
            log.error(ERROR_WHILE_SENDING_MAIL, ex.getMessage(), ex);
        }
    }

    private void sendPlain(String[] to, String subject, String mailBody) {
        if (!isReadyToSend(to, subject)) {
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false);
            helper.setFrom(mailProperties.getSenderMail());
            helper.setTo(to);
            message.setSubject(subject);
            helper.setText(mailBody, false);

            mailSender.send(message);

            log.info(MAIL_SUCCESSFULLY_SENT);
        } catch (Exception ex) {
            log.error(ERROR_WHILE_SENDING_MAIL, ex.getMessage(), ex);
        }
    }

    private boolean isReadyToSend(String[] to, String subject) {
        if (isEmpty(to) || Arrays.stream(to).anyMatch(Objects::isNull)) {
            log.error("The receiver of the e-mail was not detected. Mail will not be sent.");
            return false;
        }

        final String maskedEmails = String.join(", ", maskEmails(to));

        if (mailProperties.isMailingDisabled()) {
            log.error("Mail has NOT been sent - Mail sending is globally disabled. Mail to: [{}] | Subject: [{}]", maskedEmails, subject);
            return false;
        }

        log.info("Preparing to send mail to: [{}] | Subject: [{}]", maskedEmails, subject);
        return true;
    }

    private String[] maskEmails(String[] emails) {
        return Arrays.stream(emails)
                     .map(MaskingUtil::maskEmail)
                     .toArray(String[]::new);
    }
}