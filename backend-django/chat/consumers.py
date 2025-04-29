import json
from datetime import datetime
from bson import ObjectId

from channels.generic.websocket import AsyncWebsocketConsumer
from channels.db import database_sync_to_async
from django.conf import settings

from users.models import User
from .documents import Chat, Message


class ChatConsumer(AsyncWebsocketConsumer):


    @database_sync_to_async
    def _is_member(self, chat_id: str, user_id: int) -> bool:
        return Chat.objects(id=ObjectId(chat_id), members=user_id).first() is not None

    @database_sync_to_async
    def _save_message(self, chat_id: str, sender_id: int, text: str) -> Message:
        chat = Chat.objects.get(id=ObjectId(chat_id))
        if chat.unread_counters is None:
            chat.unread_counters = {}
        msg = Message(chat=chat, sender=sender_id, text=text, created_at=datetime.utcnow(),read_by=[sender_id])
        msg.save()

        for uid in chat.members:
            if uid == sender_id:
                continue
            chat.unread_counters[str(uid)] =chat.unread_counters.get(str(uid), 0) + 1

        chat.save()

        return msg



    async def connect(self):
        # print("CONNECT called, scope headers =", self.scope["headers"])   ###

        self.chat_id = self.scope["url_route"]["kwargs"]["chat_id"]
        self.room_group_name = f"chat_{self.chat_id}"
        user = self.scope["user"]

        if not user or not user.is_authenticated:
            await self.close(code=4000)
            return

        if not await self._is_member(self.chat_id, user.id):
            await self.close(code=4001)
            return

        await self.channel_layer.group_add(self.room_group_name, self.channel_name)
        await self.accept()

    async def disconnect(self, code):
        await self.channel_layer.group_discard(self.room_group_name, self.channel_name)


    async def receive(self, text_data):

        try:
            data = json.loads(text_data)
        except json.JSONDecodeError:
            return

        if data.get("type") != "send":
            return

        text = data.get("text", "").strip()
        if not text:
            return

        user = self.scope["user"]

        msg = await self._save_message(self.chat_id, user.id, text)

        await self.channel_layer.group_send(
            self.room_group_name,
            {
                "type": "chat.message",
                "message": {
                    "id": str(msg.id),
                    "sender": user.id,
                    "text": msg.text,
                    "created_at": msg.created_at.isoformat(),
                },
            },
        )


    async def chat_message(self, event):
        await self.send(text_data=json.dumps(event["message"]))
