from django.urls import path
from rest_framework_simplejwt.views import TokenRefreshView
from .views import *

urlpatterns = [
    path('',PostListView.as_view(),name='post-list'), # Все посты
    path('create/',PostCreateView.as_view(),name='post-create'), # Создание поста
    path('user/',UserPostListView.as_view(),name='my-posts'),  # Свои посты
    path('user/<int:user_id>/', UserPostListView.as_view(), name='user-posts'), # Посты другого пользователя
    path('<int:pk>/',PostDetailView.as_view(),name='post-detail'), # Детали поста
    path('image-upload/<int:post_id>/',PostImageUploadView.as_view(),name='image-upload'),#Загрузка изображения
    path('<int:post_id>/image/<int:pk>/',PostImageDetailView.as_view(),name='image-detail'), #Изменение удаление и чтение одного изображения
    path('image-list/<int:post_id>/',PostImageListView.as_view(),name='image-list'), #Все изображения к посту
]
