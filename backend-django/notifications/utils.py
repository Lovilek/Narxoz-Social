from .models import Notification


def create_notification(user_id: int, type_: str, data: dict) -> None:
    Notification.objects.create(user_id=user_id, type=type_, data=data)
