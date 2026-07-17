package com.edward.chat_system.infrastructure.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Cấu hình thread pool riêng cho @Async.
 * Mặc định Spring sẽ tìm bean tên "taskExecutor" để xử lý các method @Async.
 * Nếu không có, nó fallback về SimpleAsyncTaskExecutor — tạo thread mới mỗi lần gọi,
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MailServiceImpl implements MailService {
    JavaMailSender mailSender;
    TemplateEngine templateEngine;

    @Async
    @Override
    @Retryable(value = MailException.class, maxRetries = 3, delay = 500)
    public void sendOtp(String to, String otpCode, MailTemplate template) {
        Context context = new Context();
        context.setVariable("otpCode", otpCode);
        context.setVariable("expiryMinutes", 5);

        try {
            String html = templateEngine.process(template.getTemplateName(), context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(template.getSubject());
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new MailSendException("Failed to send email to " + to, e);
        }
    }
}
