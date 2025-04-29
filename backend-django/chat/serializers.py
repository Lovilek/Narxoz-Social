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
    unread=serializers.SerializerMethodField()
    avatar_url=serializers.SerializerMethodField()
    members=serializers.ListField(child=serializers.IntegerField())


    def get_unread(self,chat: Chat):
        request=self.context.get('request')
        if not request:
            return 0
        return chat.unread_counters.get(str(request.user.id), 0)


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
    name=serializers.CharField(allow_null=True)
    owner_id=serializers.IntegerField()
    members=serializers.ListField(child=serializers.IntegerField())
    avatar_url=serializers.CharField(allow_null=True)
    unread=serializers.SerializerMethodField()
    created_at=serializers.DateTimeField()
    def get_unread(self,chat: Chat):
        request=self.context.get('request')
        if not request:
            return 0
        return chat.unread_counters.get(str(request.user.id), 0)


class MessageSerializer(serializers.Serializer):
    id=serializers.CharField()
    text=serializers.CharField()
    sender=serializers.IntegerField()
    created_at=serializers.DateTimeField()