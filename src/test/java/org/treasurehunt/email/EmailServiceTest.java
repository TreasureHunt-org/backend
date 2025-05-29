package org.treasurehunt.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private EmailService emailService;

    @Mock
    private JavaMailSender javaMailSender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailService = new EmailService(javaMailSender);
    }

    @Test
    void sendSimpleEmail_ShouldSendEmailWithCorrectParameters() {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // Use ArgumentCaptor to capture the SimpleMailMessage passed to send()
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendSimpleEmail(to, subject, body);

        // Assert
        verify(javaMailSender).send(messageCaptor.capture());
        
        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage);
        assertArrayEquals(new String[]{to}, capturedMessage.getTo());
        assertEquals(subject, capturedMessage.getSubject());
        assertEquals(body, capturedMessage.getText());
    }

    @Test
    void sendSimpleEmail_WithMultipleRecipients_ShouldSendEmailToAll() {
        // Arrange
        String[] recipients = {"user1@example.com", "user2@example.com", "user3@example.com"};
        String to = String.join(",", recipients);
        String subject = "Multiple Recipients Test";
        String body = "This is a test email with multiple recipients";

        // Use ArgumentCaptor to capture the SimpleMailMessage passed to send()
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendSimpleEmail(to, subject, body);

        // Assert
        verify(javaMailSender).send(messageCaptor.capture());
        
        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage);
        assertEquals(to, capturedMessage.getTo()[0]); // The service doesn't split the recipients
        assertEquals(subject, capturedMessage.getSubject());
        assertEquals(body, capturedMessage.getText());
    }

    @Test
    void sendSimpleEmail_WithEmptySubject_ShouldSendEmailWithEmptySubject() {
        // Arrange
        String to = "test@example.com";
        String subject = ""; // Empty subject
        String body = "Test Body";

        // Use ArgumentCaptor to capture the SimpleMailMessage passed to send()
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendSimpleEmail(to, subject, body);

        // Assert
        verify(javaMailSender).send(messageCaptor.capture());
        
        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage);
        assertArrayEquals(new String[]{to}, capturedMessage.getTo());
        assertEquals(subject, capturedMessage.getSubject());
        assertEquals(body, capturedMessage.getText());
    }
}