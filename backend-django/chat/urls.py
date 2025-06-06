from django.urls import path, include
from rest_framework.routers import DefaultRouter

from .views import *






urlpatterns = [
    path("allchats/",AllChatsListAPIView.as_view(),name="all-chats"),
    path("groups/",GroupListAPIView.as_view(),name="groups"),
    path("direct/",DirectListAPIView.as_view(),name="direct"),
    path("group/create/", GroupCreateAPIView.as_view(),name="group-create"),
    path("<str:chat_id>/add/",AddUserToGroupAPIView.as_view(),name="add-group-user"),
    path("<str:chat_id>/remove/",RemoveUserFromGroupAPIView.as_view(),name="remove-user"),
    path("<str:chat_id>/leave/",LeaveGroupAPIView.as_view(),name="leave-group"),
    path("<str:chat_id>/delete/",DeleteGroupAPIView.as_view(),name="delete-group"),
    path("<str:chat_id>/update/", UpdateGroupAPIView.as_view(), name="update-group"),
    path("<str:chat_id>/detail/",ChatDetailAPIView.as_view(),name="chat-detail"),
    path("<str:chat_id>/messages/",ChatMessageListAPIView.as_view(),name="chat-messages"),
    # http://127.0.0.1:8000/api/chats/chat_id/messages/?limit=3&before=message_id
    path("<str:chat_id>/message/<str:message_id>/delete/",DeleteMessageAPIView.as_view(),name="delete-message"),
    path("<str:chat_id>/message/<str:message_id>/edit/", EditMessageAPIView.as_view(), name="edit-message"),
    path("direct/<int:user_id>/",DirectChatView.as_view(),name="direct"),
    path("<str:chat_id>/read/",ChatMarkReadAPIView.as_view(),name="chat-read"),
    path("upload-file/", ChatFileUploadAPIView.as_view(), name="upload-file"),
    path("<str:chat_id>/share/",ShareAPIView.as_view(),name="share"),
]
