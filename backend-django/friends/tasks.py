from celery import shared_task
from celery.utils.log import get_task_logger
from asgiref.sync import async_to_sync
from channels.layers import get_channel_layer

from notifications.utils import create_notification
from .models import FriendRequest

logger = get_task_logger(__name__)
channel_layer = get_channel_layer()


@shared_task(queue="push", bind=True, max_retries=3, default_retry_delay=60)
def send_friend_request_push(self, request_id: int) -> None:
    logger.info("\u2192 send_friend_request_push START for request_id=%s", request_id)
    try:

        fr = FriendRequest.objects.select_related("from_user", "to_user").get(pk=request_id)

    except FriendRequest.DoesNotExist:
        logger.error("    NOT FOUND request %s", request_id)
        return

    payload = {
        "type": "friend_request",
        "request": {
            "id": fr.pk,
            "from_user": {
                "id": fr.from_user.id,
                "nickname": fr.from_user.nickname,
                "full_name": fr.from_user.full_name
            },
            "created_at": fr.created_at.isoformat(),
            "status": fr.status,
        },
    }
    async_to_sync(channel_layer.group_send)(
        f"user-{fr.to_user.pk}", {"type": "friend_request", "data": payload}
    )
    create_notification(fr.to_user.pk, "friend_request", payload)
    logger.info("\u2192 send_friend_request_push FINISH for request_id=%s", request_id)
