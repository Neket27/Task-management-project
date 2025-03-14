package app.service.impl;

import app.aspect.annotation.CustomLogging;
import app.dto.SingleReceiverRequest;
import app.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@CustomLogging
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;

    @Value("${spring.mail.sender.email}")
    private String senderEmail;

    @Override
    public void sendTextEmail(SingleReceiverRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(request.receiver());
        message.setSubject(request.subject());
        message.setText(request.text());
        emailSender.send(message);
    }
}
