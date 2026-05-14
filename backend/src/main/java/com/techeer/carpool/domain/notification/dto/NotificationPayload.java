package com.techeer.carpool.domain.notification.dto;

import com.techeer.carpool.domain.notification.type.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class NotificationPayload {
    private NotificationType type;
    private String message;
    private Map<String, Object> data;
}
