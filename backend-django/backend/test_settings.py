
from pathlib import Path
import mongomock

import mongoengine
_real_connect = mongoengine.connect


def _safe_connect(db=None,
                  alias=mongoengine.connection.DEFAULT_CONNECTION_NAME,
                  **kwargs):
    if alias in mongoengine.connection._connections:
        return mongoengine.connection.get_connection(alias)
    return _real_connect(db=db, alias=alias, **kwargs)


mongoengine.connect = _safe_connect

MOCK_HOST = (
    "mongodb://mongoadmin:mysecretpassword@mongo:27017/"
    "narxoz_social_chat?authSource=admin"
)
mongoengine.connect(
    alias="default",
    db="narxoz_social_chat",
    host=MOCK_HOST,
    mongo_client_class=mongomock.MongoClient,
)

BASE_DIR = Path(__file__).resolve().parent.parent
SECRET_KEY = "test-secret-key"
DEBUG = True
ALLOWED_HOSTS = []

INSTALLED_APPS = [
    "django.contrib.admin",
    "django.contrib.auth",
    "django.contrib.contenttypes",
    "django.contrib.sessions",
    "django.contrib.messages",
    "django.contrib.staticfiles",
    "rest_framework",
    "rest_framework.authtoken",
    "rest_framework_simplejwt.token_blacklist",
    "users",
    "posts",
    "events",
    "friends",
    "chat",
    "notifications",
    "complaints",

]

AUTH_USER_MODEL = "users.User"
ROOT_URLCONF = "backend.urls"
WSGI_APPLICATION = "backend.wsgi.application"

DATABASES = {
    "default": {
        "ENGINE": "django.db.backends.sqlite3",
        "NAME": BASE_DIR / "test_db.sqlite3",
    }
}

PASSWORD_HASHERS = ["django.contrib.auth.hashers.MD5PasswordHasher"]

STATIC_URL = "/static/"
MEDIA_URL = "/media/"

REST_FRAMEWORK = {
    "DEFAULT_AUTHENTICATION_CLASSES": [
        "rest_framework_simplejwt.authentication.JWTAuthentication",
    ],
    "DEFAULT_PERMISSION_CLASSES": [
        "rest_framework.permissions.AllowAny",
    ],
}

EMAIL_BACKEND = "django.core.mail.backends.locmem.EmailBackend"

CELERY_TASK_ALWAYS_EAGER = True
CELERY_TASK_EAGER_PROPAGATES = True

TIME_ZONE = "Asia/Almaty"
USE_TZ = True
