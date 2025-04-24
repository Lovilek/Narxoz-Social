from rest_framework import serializers

from users.models import User
from .documents import *


class LastMessageSerializer(serializers.Serializer):
    text = serializers.CharField()
    sender = serializers.IntegerField()
    created_at = serializers.DateTimeField()


class ChatShortSerializer(serializers.Serializer):
    id=serializers.CharField()
    type=serializers.CharField()
    name=serializers.CharField(allow_null=True, required=False)
    last_message=serializers.SerializerMethodField()
    unread=serializers.IntegerField(default=0)
    avatar_url=serializers.SerializerMethodField()

    def get_last_message(self, chat: Chat):
        message=Message.objects.filter(chat=chat).order_by('-created_at').first()
        if message:
            return {
                'text': message.text,
                'sender': message.sender,
                'created_at': message.created_at,
           }
        return None

    def get_avatar_url(self, chat: Chat):
        request = self.context.get('request')
        if chat.type == 'group':
            return chat.avatar_url

        if chat.type == 'direct' and request:
            current_user_id=request.user.id
            other_user=[uid for uid in chat.members if uid!=current_user_id]
            if other_user:
                other_user=User.objects.get(id=other_user[0]).first()
                if other_user and other_user.avatar_url:
                    return other_user.avatar_url
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
    name=serializers.CharField(allow_null=True)
    owner_id=serializers.IntegerField()
    members=serializers.ListField(child=serializers.IntegerField())
    avatar_url=serializers.CharField(allow_null=True)
    created_at=serializers.DateTimeField()


class MessageSerializer(serializers.Serializer):
    id=serializers.CharField()
    text=serializers.CharField()
    sender=serializers.IntegerField()
    created_at=serializers.DateTimeField()