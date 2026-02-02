package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.MapperLayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.Notification;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.NotificationResponseModel;

public class NotificationMapper {

    public static NotificationResponseModel toResponseModel(Notification notification) {
        if (notification == null) {
            return null;
        }

        return new NotificationResponseModel(
                notification.getNotificationId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getCategory(),
                notification.getLink(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}
