import pytest
from django.contrib.auth import get_user_model
from rest_framework.test import APIClient
from asgiref.sync import async_to_sync

User = get_user_model()

@pytest.fixture(autouse=True)
def dummy_channel_layer(monkeypatch):
    class _Dummy:
        def group_send(self, group, msg):
            return None
    monkeypatch.setattr("friends.tasks.channel_layer", _Dummy())
    yield


@pytest.fixture
def api_client():
    return APIClient()


@pytest.fixture
def admin_user(db):
    u = User.objects.create_superuser(
        login="F00000001",
        full_name="Admin User",
        email="admin@test.com",
        nickname="admin",
        password="adminpass",
    )
    if hasattr(u, "is_policy_accepted"):
        u.is_policy_accepted = True
        u.save(update_fields=["is_policy_accepted"])
    return u


@pytest.fixture
def normal_user(db):
    u = User.objects.create_user(
        login="S00000001",
        full_name="Normal User",
        email="user@test.com",
        nickname="user",
        password="userpass",
    )
    if hasattr(u, "is_policy_accepted"):
        u.is_policy_accepted = True
        u.save(update_fields=["is_policy_accepted"])
    return u


@pytest.fixture
def auth_client(api_client, normal_user):
    resp = api_client.post(
        "/api/users/login/", {"login": normal_user.login, "password": "userpass"}, format="json"
    )
    api_client.credentials(HTTP_AUTHORIZATION=f'Bearer {resp.data["access"]}')
    return api_client


@pytest.fixture
def auth_admin_client(api_client, admin_user):
    resp = api_client.post(
        "/api/users/login/", {"login": admin_user.login, "password": "adminpass"}, format="json"
    )
    api_client.credentials(HTTP_AUTHORIZATION=f'Bearer {resp.data["access"]}')
    return api_client

@pytest.fixture(autouse=True)
def dummy_channel_layer(monkeypatch):
    class _Dummy:
        async def group_send(self, *args, **kwargs):
            return None
    monkeypatch.setattr("friends.tasks.channel_layer", _Dummy())
    yield
