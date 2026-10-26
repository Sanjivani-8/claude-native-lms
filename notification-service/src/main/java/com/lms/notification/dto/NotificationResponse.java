package com.lms.notification.dto;

import com.lms.notification.entity.NotificationChannel;
import com.lms.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private Long userId;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationChannel channel;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
