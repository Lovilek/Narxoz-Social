# chat/accept_all_origins_middleware.py
from channels.middleware import BaseMiddleware


class AcceptAllOriginsMiddleware(BaseMiddleware):

    async def __call__(self, scope, receive, send):
        return await super().__call__(scope, receive, send)
