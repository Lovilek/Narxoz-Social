# chat/jwt_middleware.py
from urllib.parse import parse_qs
from rest_framework_simplejwt.authentication import JWTAuthentication
from rest_framework_simplejwt.exceptions import InvalidToken, AuthenticationFailed
from channels.db import database_sync_to_async

jwt_auth = JWTAuthentication()

class JWTAuthMiddleware:
    """
    ASGI-middleware для Channels.
    • Достаёт JWT из заголовка Authorization или query ?token=
    • Кладёт пользователя в scope["user"] (или оставляет AnonymousUser)
    """

    def __init__(self, inner):
        self.inner = inner

    async def __call__(self, scope, receive, send):
        headers = dict((k.decode(), v.decode()) for k, v in scope["headers"])
        token = None

        auth = headers.get("authorization")
        if auth and auth.lower().startswith("bearer "):
            token = auth.split(" ", 1)[1]

        if not token:
            qs = parse_qs(scope.get("query_string", b"").decode())
            token = qs.get("token", [None])[0]

        if token:
            try:
                validated = jwt_auth.get_validated_token(token)
                user = await database_sync_to_async(jwt_auth.get_user)(validated)
                scope["user"] = user
            except (InvalidToken, AuthenticationFailed):
                pass

        return await self.inner(scope, receive, send)
