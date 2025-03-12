from rest_framework import serializers

from .models import *

class PostImageSerializer(serializers.ModelSerializer):
    class Meta:
        model = PostImage
        fields = ['id', 'image_path', 'created_at', 'updated_at']



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
    class Meta:
        model = Comment
        fields = ['id', 'post', 'author', 'content', 'created_at']
        read_only_fields = ['author']

class LikeSerializer(serializers.ModelSerializer):
    class Meta:
        model = Like
        fields = ['id', 'author', 'post', 'created_at']
        read_only_fields = ['author']
