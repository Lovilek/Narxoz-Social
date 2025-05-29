from django.urls import re_path
from .consumers import *
from events.routing import websocket_urlpatterns as notify_patterns


websocket_urlpatterns = [
    re_path(r'ws/chat/(?P<chat_id>[0-9a-f]{24})/$', ChatConsumer.as_asgi()),
] + notify_patterns
