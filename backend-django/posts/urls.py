from django.urls import path
from rest_framework_simplejwt.views import TokenRefreshView
from .views import *

urlpatterns = [
    path('',PostListView.as_view(),name='post-list'), # Все посты
    path('create/',PostCreateView.as_view(),name='post-create'), # Создание поста
    path('user/',UserPostListView.as_view(),name='my-posts'),  # Свои посты
    path('user/<int:user_id>/', UserPostListView.as_view(), name='user-posts'), # Посты другого пользователя
    path('<int:pk>/',PostDetailView.as_view(),name='post-detail'), # Детали поста

]
