from channels.generic.websocket import AsyncJsonWebsocketConsumer
import json
from django.utils import timezone
from channels.db import database_sync_to_async

from users.models import User


class NotificationsConsumer(AsyncJsonWebsocketConsumer):

    @database_sync_to_async
    def _update_last_seen(self, user_id: int) -> None:
        User.objects.filter(id=user_id).update(last_seen=timezone.now())
    async def connect(self):
        user = self.scope["user"]
        if user.is_authenticated:
            self.group_name = f"user-{user.id}"
            await self._update_last_seen(user.id)
            await self.channel_layer.group_add(self.group_name, self.channel_name)
            await self.accept()
        else:
            await self.close()

    async def disconnect(self, code):
        user = self.scope.get("user")
        if user and user.is_authenticated:
            await self._update_last_seen(user.id)
        if hasattr(self, "group_name"):
            await self.channel_layer.group_discard(self.group_name, self.channel_name)

    async def event_reminder(self, event: dict):
        await self.send(text_data=json.dumps(event, ensure_ascii=False))

    async def friend_request(self,data:dict):
        await self.send(text_data=json.dumps(data,ensure_ascii=False))
