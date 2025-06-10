from django.urls import path
from .consumers import NotificationsConsumer

websocket_urlpatterns = [
    path("ws/notify/", NotificationsConsumer.as_asgi()),
]
