import json
from datetime import datetime
from bson import ObjectId
from django.utils import timezone

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
    def _save_text_message(self, chat_id: str, sender_id: int, text: str) -> Message:
        chat = Chat.objects.get(id=ObjectId(chat_id))
        if chat.unread_counters is None:
            chat.unread_counters = {}
        msg = Message(
            chat=chat,
            sender=sender_id,
            text=text,
            file_url=None,
            filename=None,
            created_at=datetime.utcnow(),
            read_by=[sender_id])
        msg.save()

        for uid in chat.members:
            if uid == sender_id:
                continue
            chat.unread_counters[str(uid)] = chat.unread_counters.get(str(uid), 0) + 1

        chat.save()

        return msg

    @database_sync_to_async
    def _save_file_message(self, chat_id: str, sender_id: int, file_url: str, filename: str) -> Message:
        chat = Chat.objects.get(id=ObjectId(chat_id))
        if chat.unread_counters is None:
            chat.unread_counters = {}

        msg = Message(
            chat=chat,
            sender=sender_id,
            text=None,
            file_url=file_url,
            filename=filename,
            created_at=datetime.utcnow(),
            read_by=[sender_id]
        )
        msg.save()
        for uid in chat.members:
            if uid == sender_id:
                continue
            chat.unread_counters[str(uid)] = chat.unread_counters.get(str(uid), 0) + 1

        chat.save()
        return msg

    @database_sync_to_async
    def _update_last_seen(self, user_id: int) -> None:
        User.objects.filter(id=user_id).update(
            last_seen=timezone.now()
        )

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

        await self._update_last_seen(user.id)
        await self.channel_layer.group_add(self.room_group_name, self.channel_name)
        await self.accept()

    async def disconnect(self, code):
        user = self.scope.get("user")
        if user and user.is_authenticated:
            await self._update_last_seen(user.id)
        await self.channel_layer.group_discard(self.room_group_name, self.channel_name)

    async def receive(self, text_data=None, bytes_data=None):

        try:
            data = json.loads(text_data)
        except json.JSONDecodeError:
            return

        user = self.scope["user"]
        if data.get("type") == "send":
            text = data.get("text", "").strip()
            if not text:
                return
            msg = await self._save_text_message(self.chat_id, user.id, text)
            payload = {
                "type": "chat.message",
                "message": {
                    "id": str(msg.id),
                    "sender": user.id,
                    "text": msg.text,
                    "file_url": None,
                    "filename": None,
                    "share_type": msg.share_type,
                    "share_id": msg.share_id,
                    "created_at": msg.created_at.isoformat(),
                }
            }
        elif data.get("type") == "file":
            file_url = data.get("file_url")
            filename = data.get("filename")
            if not file_url or not filename:
                return
            msg = await self._save_file_message(self.chat_id, user.id, file_url, filename)
            payload = {
                "type": "chat.file",
                "message": {
                    "id": str(msg.id),
                    "sender": user.id,
                    "text": None,
                    "file_url": msg.file_url,
                    "filename": msg.filename,
                    "share_type": msg.share_type,
                    "share_id": msg.share_id,
                    "created_at": msg.created_at.isoformat(),
                }
            }
        else:
            return

        await self.channel_layer.group_send(self.room_group_name, payload)

    async def chat_message(self, event):
        await self.send(text_data=json.dumps(event["message"]))

    async def chat_file(self, event):
        await self.send(text_data=json.dumps(event["message"]))
