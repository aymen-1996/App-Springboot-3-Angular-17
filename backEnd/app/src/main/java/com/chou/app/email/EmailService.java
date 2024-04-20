package com.chou.app.email;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import com.chou.app.user.TokenRepository;
import com.chou.app.user.User;
import com.chou.app.user.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


@Service
@RequiredArgsConstructor
@EnableAsync
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void send(
            String to,
            String username,
            String templateName,
            String confirmationUrl
    ) throws MessagingException {
        if (!StringUtils.hasLength(templateName)) {
            templateName = "confirm-email";
        }
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                MimeMessageHelper.MULTIPART_MODE_MIXED,
                StandardCharsets.UTF_8.name()
        );
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", username);
        properties.put("confirmationUrl", confirmationUrl);

        Context context = new Context();
        context.setVariables(properties);

        helper.setFrom("chouaibiaymen03@gmail.com");
        helper.setTo(to);
        helper.setSubject("Activated Account");

        String template = templateEngine.process(templateName, context);

        helper.setText(template, true);

        mailSender.send(mimeMessage);
    }


    public void sendEmail(String recipient, String subject, String body) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;

        try {
            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setTo(recipient);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public LocalDateTime getCheckDate() {
        return LocalDateTime.now().minusWeeks(1);
    }

    @Scheduled(cron = "${interval-in-cron}")
    @Async
    @Transactional
    public void DeleteUser() {
        LocalDateTime checkDate = getCheckDate();
        List<Long> userIdsToDelete = new ArrayList<>();

        for (User user : userRepository.findUserByCreatedDateBeforeAndEnabledIsFalse(checkDate)) {
            if (!user.isEnabled() && isMoreThanAWeekOld(user.getCreatedDate())) {
                tokenRepository.deleteTokensByUserId(user.getId());
                sendEmail(user.getEmail(), "Your account has been deleted", "Your account has been deleted.");
                userIdsToDelete.add(user.getId());
            }
        }

        if (!userIdsToDelete.isEmpty()) {
            userRepository.deleteUsersByIdIn(userIdsToDelete);
        }
    }


    private boolean isMoreThanAWeekOld(LocalDateTime createdDate) {
        LocalDateTime oneWeekLater = createdDate.plusWeeks(1);

        LocalDateTime currentDate = LocalDateTime.now();

        return currentDate.isAfter(oneWeekLater);
    }







}