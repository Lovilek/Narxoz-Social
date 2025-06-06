from channels.generic.websocket import AsyncJsonWebsocketConsumer
import json
class NotificationsConsumer(AsyncJsonWebsocketConsumer):

    async def connect(self):
        user = self.scope["user"]
        if user.is_authenticated:
            self.group_name = f"user-{user.id}"
            await self.channel_layer.group_add(self.group_name, self.channel_name)
            await self.accept()
        else:
            await self.close()

    async def disconnect(self, code):
        if hasattr(self, "group_name"):
            await self.channel_layer.group_discard(self.group_name, self.channel_name)

    async def event_reminder(self, event: dict):
        await self.send(text_data=json.dumps(event, ensure_ascii=False))

    async def friend_request(self,data:dict):
        await self.send(text_data=json.dumps(data,ensure_ascii=False))
