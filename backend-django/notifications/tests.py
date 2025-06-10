import pytest
from django.urls import reverse
from rest_framework.test import APIClient
from django.conf import settings

# Create your tests here.
from users.models import User
from .models import Notification


@pytest.mark.django_db
def test_notification_str():
    user = User.objects.create_user(
        login="S00000001",
        full_name="Test User",
        email="user@example.com",
        nickname="user1",
        password="pass",
    )
    user.is_policy_accepted = True
    user.save()
    notif = Notification.objects.create(user=user, type="friend", data={})
    assert str(notif) == f"{user.nickname} - friend"


@pytest.mark.django_db
def test_list_only_unread():
    user = User.objects.create_user(
        login="S00000001",
        full_name="Test User",
        email="user@example.com",
        nickname="user1",
        password="pass",
    )
    user.is_policy_accepted = True
    user.save()
    settings.DATABASES["default"].setdefault("ATOMIC_REQUESTS", False)
    client = APIClient()
    client.force_authenticate(user)

    Notification.objects.create(user=user, type="t1", data={}, is_read=False)
    Notification.objects.create(user=user, type="t2", data={}, is_read=True)

    url = reverse("notification-list")
    resp = client.get(url)
    assert resp.status_code == 200
    assert len(resp.data["results"]) == 1
    assert resp.data["results"][0]["type"] == "t1"