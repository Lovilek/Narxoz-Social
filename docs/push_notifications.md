# Push-уведомления

Ниже описан общий процесс настройки push-сообщений через Firebase Cloud Messaging и Celery.

1. **Получение FCM-токена на клиенте**
   - В `FCMService` токен обновляется в `onNewToken` и выводится в логи.
2. **Отправка уведомлений**
   - Сервер инициирует уведомление через Firebase Cloud Messaging.
   - При получении сообщения `FCMService` отображает уведомление пользователю.

Для проверки списка уведомлений доступны API:
```
GET  /api/notifications/          # получить список уведомлений
POST /api/notifications/read/<id>/  # пометить уведомление прочитанным
```
и WebSocket канал `ws://<SERVER>/ws/notify/`.