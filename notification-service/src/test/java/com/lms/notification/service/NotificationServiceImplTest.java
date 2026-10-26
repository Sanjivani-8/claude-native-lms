package com.lms.notification.service;

import com.lms.notification.dto.NotificationRequest;
import com.lms.notification.dto.NotificationResponse;
import com.lms.notification.entity.Notification;
import com.lms.notification.entity.NotificationChannel;
import com.lms.notification.entity.NotificationType;
import com.lms.notification.repository.NotificationRepository;
import com.lms.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailSenderStub emailSenderStub;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;

    private Notification existingNotification;

    @BeforeEach
    void setUp() {
        existingNotification = Notification.builder()
                .id(1L)
                .userId(100L)
                .title("Assignment Graded")
                .message("Your assignment has been graded.")
                .type(NotificationType.GRADE)
                .channel(NotificationChannel.IN_APP)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void send_savesNotification_andSendsEmailWhenChannelIsEmail() {
        NotificationRequest request = NotificationRequest.builder()
                .userId(100L)
                .title("New Announcement")
                .message("Course starts tomorrow")
                .type(NotificationType.ANNOUNCEMENT)
                .channel(NotificationChannel.EMAIL)
                .build();

        Notification saved = Notification.builder()
                .id(2L)
                .userId(100L)
                .title("New Announcement")
                .message("Course starts tomorrow")
                .type(NotificationType.ANNOUNCEMENT)
                .channel(NotificationChannel.EMAIL)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        NotificationResponse response = notificationService.send(request);

        verify(notificationRepository, times(1)).save(notificationCaptor.capture());
        Notification captured = notificationCaptor.getValue();
        assertThat(captured.getUserId()).isEqualTo(100L);
        assertThat(captured.getTitle()).isEqualTo("New Announcement");
        assertThat(captured.getChannel()).isEqualTo(NotificationChannel.EMAIL);

        verify(emailSenderStub, times(1)).sendEmail(eq("100"), eq("New Announcement"), anyString());

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getChannel()).isEqualTo(NotificationChannel.EMAIL);
    }

    @Test
    void send_doesNotSendEmail_whenChannelIsInApp() {
        NotificationRequest request = NotificationRequest.builder()
                .userId(100L)
                .title("Enrollment Confirmed")
                .message("You are enrolled")
                .type(NotificationType.ENROLLMENT)
                .channel(NotificationChannel.IN_APP)
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(existingNotification);

        notificationService.send(request);

        verify(emailSenderStub, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void markAsRead_setsReadTrue_andReadAtTimestamp() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(existingNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(1L);

        assertThat(response.isRead()).isTrue();
        assertThat(response.getReadAt()).isNotNull();
        verify(notificationRepository, times(1)).save(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().isRead()).isTrue();
        assertThat(notificationCaptor.getValue().getReadAt()).isNotNull();
    }
}
