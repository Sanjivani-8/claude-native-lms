package com.lms.notification.service;

import com.lms.notification.dto.NotificationRequest;
import com.lms.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    NotificationResponse send(NotificationRequest request);

    NotificationResponse getById(Long id);

    List<NotificationResponse> getByUser(Long userId);

    List<NotificationResponse> getUnreadByUser(Long userId);

    long getUnreadCount(Long userId);

    NotificationResponse markAsRead(Long id);

    void markAllAsRead(Long userId);

    void delete(Long id);
}
