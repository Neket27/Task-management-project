package app.integration;

import app.dto.SingleReceiverRequest;
import app.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.mail.sender.email=test@company.com"
})
class EmailServiceIT {

    @Autowired
    private EmailService emailService;

    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    void sendTextEmail_ShouldSendEmail() {
        // Arrange
        SingleReceiverRequest request = new SingleReceiverRequest(
                "receiver@example.com",
                "Test Subject",
                "Test email body"
        );

        // Act
        emailService.sendTextEmail(request);

        // Assert
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
