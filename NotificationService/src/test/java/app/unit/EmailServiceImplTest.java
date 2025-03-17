package app.unit;

import app.dto.SingleReceiverRequest;
import app.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @InjectMocks
    private EmailServiceImpl emailService;

    @Mock
    private JavaMailSender emailSender;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "senderEmail", "test@company.com");
    }

    /**
     * Что проверяет
     * Email формируется корректно.
     * Email отправляется с правильными параметрами
     */
    @Test
    void sendTextEmail_ShouldSendEmail() {
        // Arrange
        SingleReceiverRequest request = new SingleReceiverRequest("user@example.com", "Test Subject", "Test Body");
        SimpleMailMessage expectedMessage = new SimpleMailMessage();
        expectedMessage.setFrom("test@company.com");
        expectedMessage.setTo("user@example.com");
        expectedMessage.setSubject("Test Subject");
        expectedMessage.setText("Test Body");

        // Act
        emailService.sendTextEmail(request);

        // Assert
        verify(emailSender, times(1)).send(refEq(expectedMessage));
    }
}
