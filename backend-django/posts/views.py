from symtable import Class

from django.shortcuts import render
from rest_framework.exceptions import PermissionDenied
from rest_framework.generics import *
from rest_framework.parsers import MultiPartParser,FormParser
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.status import *
from rest_framework.views import APIView

from .permissions import *
from posts.models import *
from posts.serializers import *
from users.serializers import UserSerializer


class PostCreateView(CreateAPIView):
    queryset = Post.objects.all()
    serializer_class = PostSerializer
    permission_classes = [IsAuthenticated]

    def perform_create(self, serializer):
        serializer.save(author=self.request.user)

class PostListView(ListAPIView):
    queryset = Post.objects.all().order_by('-created_at')
    serializer_class = PostSerializer
    permission_classes = [IsAuthenticated]

class UserPostListView(ListAPIView):
    serializer_class = PostSerializer
    permission_classes = [IsAuthenticated]
    def get_queryset(self):
        user_id = self.kwargs.get('user_id')
        if user_id:
            user=get_object_or_404(User,id=user_id)
            return Post.objects.filter(author=user).order_by('-created_at')

        return Post.objects.filter(author=self.request.user).order_by('-created_at')

class PostDetailView(RetrieveUpdateDestroyAPIView):
    queryset = Post.objects.all()
    serializer_class = PostSerializer
    permission_classes = [IsAuthenticated, IsOwnerOrReadOnly]


class PostImageUploadView(APIView):
    permission_classes = [IsAuthenticated]
    parser_classes = (MultiPartParser, FormParser)

    def post(self, request, post_id):
        post = get_object_or_404(Post, pk=post_id)
        if post.author != request.user:
            return Response({"error": "Вы не можете добавлять изображения в чужие посты."},
                            status=HTTP_403_FORBIDDEN)

        image_file=request.FILES.get('image_path')
        if not image_file:
            return Response({"error": "Файл изображения не был загружен."},
                status=HTTP_400_BAD_REQUEST
            )

        serializer = PostImageSerializer(data={"image_path":image_file})

        if serializer.is_valid():
            serializer.save(post=post)
            return Response(serializer.data, status=HTTP_201_CREATED)

        return Response(serializer.errors, status=HTTP_400_BAD_REQUEST)

class PostImageDetailView(RetrieveUpdateDestroyAPIView):
    serializer_class = PostImageSerializer
    permission_classes = [IsAuthenticated]
    lookup_field = 'pk'

    def get_queryset(self):
        post_id = self.kwargs.get('post_id')
        return PostImage.objects.filter(post__pk=post_id)

    def perform_destroy(self, instance):

        post_id = self.kwargs.get('post_id')
        image_id=self.kwargs.get('pk')

        post=get_object_or_404(Post,pk=post_id)
        image=get_object_or_404(PostImage,pk=image_id,post=post)

        if post.author != self.request.user:
            raise PermissionDenied("Вы не можете удалять изображения чужих постов.")

        image.delete()

    def perform_update(self, serializer):
        post_id = self.kwargs.get('post_id')
        image_id=self.kwargs.get('pk')

        post=get_object_or_404(Post,pk=post_id)
        image=get_object_or_404(PostImage,pk=image_id,post=post)

        if post.author!= self.request.user:
            raise PermissionDenied("Вы не можете изменять изображения чужих постов.")

        serializer.save(post=post)



class PostImageListView(ListAPIView):
    serializer_class = PostImageSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        post_id = self.kwargs.get('post_id')
        if post_id:
            post=get_object_or_404(Post,id=post_id)
            return PostImage.objects.filter(post=post)


class CommentListCreateView(ListCreateAPIView):
    serializer_class = CommentSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        post_id = self.kwargs.get('post_id')
        post=get_object_or_404(Post,id=post_id)
        return Comment.objects.filter(post=post).order_by('-created_at')

    def perform_create(self, serializer):
        post_id = self.kwargs.get('post_id')
        post=get_object_or_404(Post,id=post_id)
        serializer.save(author=self.request.user,post=post)

class CommentDetailDeleteView(RetrieveDestroyAPIView):
    serializer_class = CommentSerializer
    permission_classes = [IsAuthenticated, IsOwnerOrReadOnly]

    def get_queryset(self):
        post_id = self.kwargs.get('post_id')
        return Comment.objects.filter(post__id=post_id)

    def perform_destroy(self, instance):
        if instance.author != self.request.user:
            raise PermissionDenied("Вы не можете удалять чужие комментарии.")
        instance.delete()


class LikeToggleView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request, post_id):
        post=get_object_or_404(Post,id=post_id)
        like,created=Like.objects.get_or_create(post=post,author=self.request.user)
        if not created:
            like.delete()
            return Response({"status":"unliked"},status=HTTP_200_OK)
        return Response({"status":"ok"},status=HTTP_201_CREATED)

class LikeListView(ListAPIView):
    serializer_class = LikeSerializer
    permission_classes = [IsAuthenticated]
    def get_queryset(self):
        post_id = self.kwargs.get('post_id')
        post=get_object_or_404(Post,id=post_id)
        return Like.objects.filter(post=post)


