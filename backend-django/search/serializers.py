

from rest_framework import serializers
from django.contrib.auth import get_user_model
from posts.models import Post
from chat.documents import Chat
from users.models import User


class ChatSearchSerializer(serializers.Serializer):
    id=serializers.CharField()
    type=serializers.CharField()
    name=serializers.SerializerMethodField()
    avatar_url=serializers.SerializerMethodField()

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

    def get_avatar_url(self, chat):
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


class UserSearchSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['id','full_name', 'email', 'nickname','avatar_path']



class PostSearchSerializer(serializers.ModelSerializer):
    author = serializers.SerializerMethodField()
    author_id = serializers.SerializerMethodField()
    author_avatar_path = serializers.SerializerMethodField()
    class Meta:
        model = Post
        fields = ['id', 'content', 'author','author_id','author_avatar_path']

    def get_author(self, obj):
        return obj.author.nickname

    def get_author_id(self, obj):
        return obj.author.id

    def get_author_avatar_path(self, obj):
        request = self.context.get('request')
        if obj.author.avatar_path:
            if request:
                return request.build_absolute_uri(obj.author.avatar_path.url)
            else:
                return obj.author.avatar_path.url
        return None


