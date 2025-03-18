from rest_framework import serializers

from .models import *

class PostImageSerializer(serializers.ModelSerializer):
    class Meta:
        model = PostImage
        fields = ['id','post', 'image_path', 'created_at', 'updated_at']
        read_only_fields = ['post']



class PostSerializer(serializers.ModelSerializer):
    images=PostImageSerializer(many=True,read_only=True)
    author = serializers.SerializerMethodField()
    class Meta:
        model = Post
        fields = ['id', 'content', 'author','images', 'created_at', 'updated_at']
        read_only_fields = ['author']

    def get_author(self, obj):
        return obj.author.nickname


class CommentSerializer(serializers.ModelSerializer):
    author_nickname=serializers.CharField(source='author.nickname',read_only=True)
    class Meta:
        model = Comment
        fields = ['id', 'post', 'author','author_nickname', 'content', 'created_at']
        read_only_fields = ['author','author_nickname','id','created_at','post']


class LikeSerializer(serializers.ModelSerializer):
    class Meta:
        model = Like
        fields = ['id', 'author', 'post', 'created_at']
        read_only_fields = ['author']
