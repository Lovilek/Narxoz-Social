from django.urls import path
from rest_framework_simplejwt.views import TokenRefreshView
from .views import *

urlpatterns = [
    path('', PostListView.as_view(), name='post-list'),  # Все посты
    path('create/', PostCreateView.as_view(), name='post-create'),  # Создание поста
    path('user/', UserPostListView.as_view(), name='my-posts'),  # Свои посты
    path('user/<int:user_id>/', UserPostListView.as_view(), name='user-posts'),  # Посты другого пользователя
    path('<int:pk>/', PostDetailView.as_view(), name='post-detail'),  # Детали поста
    path('image-upload/<int:post_id>/', PostImageUploadView.as_view(), name='image-upload'),  # Загрузка изображения
    path('<int:post_id>/image/<int:pk>/', PostImageDetailView.as_view(), name='image-detail'),
    # Изменение удаление и чтение одного изображения
    path('image-list/<int:post_id>/', PostImageListView.as_view(), name='image-list'),  # Все изображения к посту
    path('<int:post_id>/comments/', CommentListCreateView.as_view(), name='comment-list-create'),
    # Создание комментария к посту и чтение всех комментариев поста
    path('<int:post_id>/comments/<int:pk>/', CommentDetailDeleteView.as_view(), name='comment-detail-delete'),
    # Чтение одного коммента и удаление
    path('<int:post_id>/delete-comment-by-post/<int:pk>/', DeleteCommentByPostView.as_view(),
         name='delete-comment-by-post'),
    # Ответ на комментарий
    path('<int:post_id>/like/', LikeToggleView.as_view(), name='like-toggle'),  # Ставит лайк, а если лайк был удаляет
    path('<int:post_id>/likes/', LikeListView.as_view(), name='like-list'),  # Список лайков для поста
]
