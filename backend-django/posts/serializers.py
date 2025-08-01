from rest_framework import serializers

from .models import *


class PostImageSerializer(serializers.ModelSerializer):
    class Meta:
        model = PostImage
        fields = ['id', 'post', 'image_path', 'created_at', 'updated_at']
        read_only_fields = ['post']


class PostSerializer(serializers.ModelSerializer):
    images = PostImageSerializer(many=True, read_only=True)
    author = serializers.SerializerMethodField()
    author_id = serializers.SerializerMethodField()
    author_avatar_path = serializers.SerializerMethodField()
    likes = serializers.SerializerMethodField()
    is_liked = serializers.SerializerMethodField()

    class Meta:
        model = Post
        fields = ['id', 'content', 'author', 'author_id', 'author_avatar_path', 'images', 'likes', 'is_liked',
                  'created_at', 'updated_at', ]
        read_only_fields = ['author']

    def get_is_liked(self, obj):
        request = self.context.get('request')
        if not request:
            return False
        return getattr(
            obj,
            "is_liked",
            Like.objects.filter(post=obj, author=request.user).exists(),
        )

    def get_likes(self, obj):
        return getattr(obj, "likes_count", Like.objects.filter(post=obj).count())

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


class PostMessageSerializer(serializers.ModelSerializer):
    images = PostImageSerializer(many=True, read_only=True)
    author = serializers.SerializerMethodField()
    author_id = serializers.SerializerMethodField()
    likes = serializers.SerializerMethodField()

    class Meta:
        model = Post
        fields = ['id', 'content', 'author', 'author_id', 'images', 'likes', 'created_at', 'updated_at', ]
        read_only_fields = ['author']

    def get_likes(self, obj):
        return getattr(obj, "likes_count", Like.objects.filter(post=obj).count())

    def get_author(self, obj):
        return obj.author.nickname

    def get_author_id(self, obj):
        return obj.author.id


class CommentSerializer(serializers.ModelSerializer):
    author_nickname = serializers.CharField(source='author.nickname', read_only=True)

    class Meta:
        model = Comment
        fields = ['id', 'post', 'author', 'author_nickname', 'content', 'created_at']
        read_only_fields = ['author', 'author_nickname', 'id', 'created_at', 'post']


class LikeSerializer(serializers.ModelSerializer):
    author_nickname = serializers.CharField(source='author.nickname', read_only=True)

    class Meta:
        model = Like
        fields = ['id', 'author', 'author_nickname', 'post', 'created_at']
        read_only_fields = ['author', 'author_nickname', 'post']
