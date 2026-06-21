package com.edward.chat_system.infrastructure.mail;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MailServiceImpl implements MailService{
    JavaMailSender mailSender;
    TemplateEngine templateEngine; 

    @Override
    public void sendOtp(String to, String otpCode) {
        Context context = new Context();
        context.setVariable("otpCode", otpCode);
        context.setVariable("expiryMinutes", 5);

        sendHtmlMail(to, MailTemplate.OTP_VERIFICATION, context);
    }

    private void sendHtmlMail(String to, MailTemplate template, Context context) {
        try {
            String html = templateEngine.process(template.getTemplateName(), context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(template.getSubject());
            helper.setText(html, true); 

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }
}
