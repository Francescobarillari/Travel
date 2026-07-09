package it.unical.ea.Travel.Mappers.notification;

import it.unical.ea.dtos.notification.NotificationDto;
import it.unical.ea.Travel.Entities.notification.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "userId", source = "user.id")
    NotificationDto toDTO(Notification notification);

    List<NotificationDto> toDTOList(List<Notification> notifications);
}
