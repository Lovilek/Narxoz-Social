from symtable import Class

from django.shortcuts import render
from rest_framework.exceptions import PermissionDenied
from rest_framework.generics import *
from rest_framework.parsers import MultiPartParser, FormParser
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.status import *
from rest_framework.views import APIView

from users.permissions import IsAcceptPrivacy
from .permissions import *
from posts.models import *
from posts.serializers import *
from users.serializers import UserSerializer
from django.db.models import Count, Exists, OuterRef


class PostCreateView(CreateAPIView):
    queryset = Post.objects.all()
    serializer_class = PostSerializer
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]

    def get_serializer_context(self):
        return {'request': self.request}

    def perform_create(self, serializer):
        serializer.save(author=self.request.user)


class PostListView(ListAPIView):
    serializer_class = PostSerializer
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]

    def get_queryset(self):
        friends = self.request.user.friends.all()
        return (
            Post.objects.filter(author__in=friends)
            .select_related("author")
            .prefetch_related("images")
            .annotate(
                likes_count=Count("likes", distinct=True),
                is_liked=Exists(
                    Like.objects.filter(author=self.request.user, post=OuterRef("pk"))
                ),
            )
            .order_by("-created_at")
        )

    def get_serializer_context(self):
        return {'request': self.request}


class UserPostListView(ListAPIView):
    serializer_class = PostSerializer
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]

    def get_queryset(self):
        user_id = self.kwargs.get('user_id')
        if user_id:
            user = get_object_or_404(User, id=user_id)
        else:
            user = self.request.user

        return (
            Post.objects.filter(author=user)
            .select_related("author")
            .prefetch_related("images")
            .annotate(
                likes_count=Count("likes", distinct=True),
                is_liked=Exists(
                    Like.objects.filter(author=self.request.user, post=OuterRef("pk"))
                ),
            )
            .order_by("-created_at")
        )

    def get_serializer_context(self):
        return {'request': self.request}


class PostDetailView(RetrieveUpdateDestroyAPIView):
    queryset = Post.objects.all()
    serializer_class = PostSerializer
    permission_classes = [IsAuthenticated, IsOwnerOrReadOnly, IsAcceptPrivacy]

    def get_serializer_context(self):
        return {'request': self.request}


class PostImageUploadView(APIView):
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]
    parser_classes = (MultiPartParser, FormParser)

    def post(self, request, post_id):
        post = get_object_or_404(Post, pk=post_id)
        if post.author != request.user:
            return Response({"error": "Вы не можете добавлять изображения в чужие посты."},
                            status=HTTP_403_FORBIDDEN)

        image_file = request.FILES.get('image_path')
        if not image_file:
            return Response({"error": "Файл изображения не был загружен."},
                            status=HTTP_400_BAD_REQUEST
                            )

        serializer = PostImageSerializer(data={"image_path": image_file})

        if serializer.is_valid():
            serializer.save(post=post)
            return Response(serializer.data, status=HTTP_201_CREATED)

        return Response(serializer.errors, status=HTTP_400_BAD_REQUEST)


class PostImageDetailView(RetrieveUpdateDestroyAPIView):
    serializer_class = PostImageSerializer
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]
    lookup_field = 'pk'

    def get_queryset(self):
        post_id = self.kwargs.get('post_id')
        return PostImage.objects.filter(post__pk=post_id)

    def perform_destroy(self, instance):

        post_id = self.kwargs.get('post_id')
        image_id = self.kwargs.get('pk')

        post = get_object_or_404(Post, pk=post_id)
        image = get_object_or_404(PostImage, pk=image_id, post=post)

        if post.author != self.request.user:
            return Response({"error": "Вы не можете удалять изображения чужих постов."}, status=403)

        image.delete()

    def perform_update(self, serializer):
        post_id = self.kwargs.get('post_id')
        image_id = self.kwargs.get('pk')

        post = get_object_or_404(Post, pk=post_id)
        image = get_object_or_404(PostImage, pk=image_id, post=post)

        if post.author != self.request.user:
            return Response({"error": "Вы не можете изменять изображения чужих постов."}, status=403)

        serializer.save(post=post)


class PostImageListView(ListAPIView):
    serializer_class = PostImageSerializer
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]

    def get_queryset(self):
        post_id = self.kwargs.get('post_id')
        if post_id:
            post = get_object_or_404(Post, id=post_id)
            return PostImage.objects.filter(post=post)


class CommentListCreateView(ListCreateAPIView):
    serializer_class = CommentSerializer
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]

    def get_queryset(self):
        post_id = self.kwargs.get("post_id")
        post = get_object_or_404(Post, id=post_id)
        return (
            Comment.objects.filter(post=post)
            .select_related("author")
            .order_by("-created_at")
        )

    def perform_create(self, serializer):
        post_id = self.kwargs.get('post_id')
        post = get_object_or_404(Post, id=post_id)
        serializer.save(author=self.request.user, post=post)


class CommentDetailDeleteView(RetrieveDestroyAPIView):
    serializer_class = CommentSerializer
    permission_classes = [IsAuthenticated, IsOwnerOrReadOnly, IsAcceptPrivacy]

    def get_queryset(self):
        post_id = self.kwargs.get("post_id")
        return Comment.objects.filter(post__id=post_id).select_related("author")

    def perform_destroy(self, instance):
        if instance.author != self.request.user:
            raise PermissionDenied("Вы не можете удалять чужие комментарии.")
        instance.delete()


class DeleteCommentByPostView(DestroyAPIView):
    serializer_class = CommentSerializer
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]
    lookup_url_kwarg = "pk"

    def get_object(self):
        post_id = self.kwargs.get('post_id')
        comment_id = self.kwargs.get('pk')
        post = get_object_or_404(Post, pk=post_id)

        if post.author != self.request.user:
            raise PermissionDenied("Вы не можете удалять комментарии в чужих постах.")

        comment = get_object_or_404(Comment, pk=comment_id, post=post)
        return comment

    def destroy(self, request, *args, **kwargs):
        instance = self.get_object()
        self.perform_destroy(instance)
        return Response(
            {"message": "Комментарий успешно удалён."},
            status=200
        )


class LikeToggleView(APIView):
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]

    def post(self, request, post_id):
        post = get_object_or_404(Post, id=post_id)
        like, created = Like.objects.get_or_create(post=post, author=self.request.user)
        if not created:
            like.delete()
            return Response({"status": "unliked"}, status=HTTP_200_OK)
        return Response({"status": "liked"}, status=HTTP_201_CREATED)


class LikeListView(ListAPIView):
    serializer_class = LikeSerializer
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]

    def get_queryset(self):
        post_id = self.kwargs.get('post_id')
        post = get_object_or_404(Post, id=post_id)
        return Like.objects.filter(post=post)
