from rest_framework import serializers

from users.models import User
from .documents import *


class LastMessageSerializer(serializers.Serializer):
    text = serializers.CharField()
    file_url = serializers.CharField(allow_null=True)
    filename = serializers.CharField(allow_null=True)
    sender = serializers.IntegerField()
    sender_nickname = serializers.SerializerMethodField()
    created_at = serializers.DateTimeField()

    def get_sender_nickname(self, message: Message):
        user = User.objects.filter(id=message.sender).first()
        return user.nickname if user else None


class ChatShortSerializer(serializers.Serializer):
    id=serializers.CharField()
    type=serializers.CharField()
    name=serializers.SerializerMethodField()
    last_message=serializers.SerializerMethodField()
    unread=serializers.SerializerMethodField()
    avatar_url=serializers.SerializerMethodField()
    members=serializers.ListField(child=serializers.IntegerField())

    def get_name(self,chat: Chat):
        request = self.context.get("request")
        if chat.type=="group":
            return chat.name
        if chat.type=="direct" and request:
            me=request.user.id
            other_id = next((uid for uid in chat.members if uid != me), None)
            if other_id:
                other=User.objects.get(id=other_id)
                if other:
                    return other.nickname

        return None

    def get_unread(self,chat: Chat):
        request=self.context.get('request')
        if not request:
            return 0
        return chat.unread_counters.get(str(request.user.id), 0)


    def get_last_message(self, chat: Chat):
        message=Message.objects.filter(chat=chat).order_by('-created_at').first()
        if message:
            data={
                'text': message.text,
                'sender': message.sender,
                'created_at': message.created_at,
            }

            try:
                user=User.objects.get(id=message.sender)
                data["sender_nickname"]=user.nickname
            except User.DoesNotExist:
                data["sender_nickname"]=None

            return data

        return None

    def get_avatar_url(self, chat: Chat):
        request = self.context.get("request")

        if chat.type == "group" and chat.avatar_url:
            return request.build_absolute_uri(chat.avatar_url) if request else chat.avatar_url

        if chat.type == "direct" and request:
            me = request.user.id
            other_id = next((uid for uid in chat.members if uid != me), None)
            if other_id:
                other = User.objects.filter(id=other_id).first()
                if other and other.avatar_path:
                    url = other.avatar_path.url
                    return request.build_absolute_uri(url)
        return None


class GroupCreateSerializer(serializers.Serializer):
    name=serializers.CharField()
    members=serializers.ListField(child=serializers.IntegerField(min_value=1))
    avatar=serializers.ImageField(required=False)

    def validate_members(self, members):
        if len(set(members))<1:
            raise serializers.ValidationError("Должен быть еще 1 пользователь кроме владельца группы")
        return members


class ChatDetailSerializer(serializers.Serializer):
    id=serializers.CharField()
    type=serializers.CharField()
    name=serializers.SerializerMethodField()
    owner_id=serializers.IntegerField()
    members=serializers.ListField(child=serializers.IntegerField())
    avatar_url=serializers.CharField(allow_null=True)
    unread=serializers.SerializerMethodField()
    created_at=serializers.DateTimeField()

    def get_name(self,chat: Chat):
        request = self.context.get("request")
        if chat.type=="group":
            return chat.name
        if chat.type=="direct" and request:
            me=request.user.id
            other_id = next((uid for uid in chat.members if uid != me), None)
            if other_id:
                other=User.objects.get(id=other_id)
                if other:
                    return other.nickname

        return None

    def get_unread(self,chat: Chat):
        request=self.context.get('request')
        if not request:
            return 0
        return chat.unread_counters.get(str(request.user.id), 0)


class MessageSerializer(serializers.Serializer):
    id=serializers.CharField()
    text=serializers.CharField()
    file_url = serializers.CharField(allow_null=True)
    filename = serializers.CharField(allow_null=True)
    sender=serializers.IntegerField()
    sender_nickname=serializers.SerializerMethodField()
    created_at=serializers.DateTimeField()

    def get_sender_nickname(self, message: Message):
        user = User.objects.filter(id=message.sender).first()
        return user.nickname if user else None