package com.lms.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stub email sender used because no real SMTP server is configured for this
 * environment/exercise. It simply logs what would have been sent.
 * <p>
 * To swap in a real email provider in production:
 * <pre>
 *   1. Add the "spring-boot-starter-mail" dependency.
 *   2. Configure spring.mail.* properties (host, port, username, password).
 *   3. Inject org.springframework.mail.javamail.JavaMailSender and replace
 *      the body of sendEmail(...) with a call to mailSender.send(message),
 *      building a SimpleMailMessage or MimeMessage from the given
 *      recipient, subject and body.
 * </pre>
 */
@Slf4j
@Component
public class EmailSenderStub {

    public void sendEmail(String toUserId, String subject, String body) {
        log.info("Simulated email to user {} | subject: {} | body: {}", toUserId, subject, body);
    }
}
