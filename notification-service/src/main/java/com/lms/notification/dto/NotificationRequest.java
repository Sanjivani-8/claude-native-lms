package com.lms.notification.dto;

import com.lms.notification.entity.NotificationChannel;
import com.lms.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "message is required")
    private String message;

    @Builder.Default
    private NotificationType type = NotificationType.SYSTEM;

    @Builder.Default
    private NotificationChannel channel = NotificationChannel.IN_APP;

    public NotificationType getType() {
        return type == null ? NotificationType.SYSTEM : type;
    }

    public NotificationChannel getChannel() {
        return channel == null ? NotificationChannel.IN_APP : channel;
    }
}
