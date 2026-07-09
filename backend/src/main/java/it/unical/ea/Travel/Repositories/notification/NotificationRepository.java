package it.unical.ea.Travel.Repositories.notification;

import it.unical.ea.Travel.Entities.notification.Notification;
import it.unical.ea.Travel.Entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUserAndIsReadOrderByCreatedAtDesc(User user, boolean isRead);
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
}
