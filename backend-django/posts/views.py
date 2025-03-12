from django.shortcuts import render
from rest_framework.generics import *
from rest_framework.permissions import IsAuthenticated
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

