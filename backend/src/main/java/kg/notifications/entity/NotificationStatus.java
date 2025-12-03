package kg.notifications.entity;

/**
 * Статусы уведомлений
 */
public enum NotificationStatus {
    PENDING,    // Принято в обработку
    SENDING,    // Отправляется
    SENT,       // Успешно отправлено
    DELIVERED,  // Подтверждена доставка
    FAILED,     // Ошибка отправки
    EXPIRED     // Истёк срок жизни
}
