# backend/asgi.py
import os, django
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "backend.settings")
django.setup()

from django.core.asgi import get_asgi_application
from channels.routing import ProtocolTypeRouter, URLRouter
from chat.jwt_middleware import JWTAuthMiddleware
import chat.routing

application = ProtocolTypeRouter(
    {
        "http": get_asgi_application(),
        "websocket": JWTAuthMiddleware(
            URLRouter(chat.routing.websocket_urlpatterns)
        ),
    }
)